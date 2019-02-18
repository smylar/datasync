package com.sync.repository;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.sync.configuration.DatabaseContextHolder;
import com.sync.configuration.SyncJdbcTemplate;
import com.sync.data.Audited;
import com.sync.data.Column;
import com.sync.data.Id;
import com.sync.data.Table;
import com.sync.mappers.AuditMapper;

import lombok.extern.slf4j.Slf4j;

/**
 * Generic handler for syncing specific tables
 * 
 * @author paul.brandon
 *
 * @param <T>
 */
@Slf4j
public class SyncRepository<T> {
    
    private static final String AND = " and ";
    
    @Autowired
    @Qualifier("jdbcTemplateFirst")
    private SyncJdbcTemplate firstTemplate;
    
    @Autowired
    @Qualifier("jdbcTemplateSecond")
    private SyncJdbcTemplate secondTemplate;
    @Autowired
    private AuditMapper<T> mapper;
    
    private static final String SELECT_UNSYNCED = 
            "select l.* "
            +"from %s l "
            +"where change_synced=? "
            +"and lower(change_user) != ? "
            +"order by change_time asc";
    
    private static final String SELECT_UNSYNCED_COUNT_BY_ACCOUNT_AFTER_DATE = 
            "select count(*) from %s "
            +"where lower(change_user) != ? "
            +"and change_time > ? and %s ";
    
    private static final String UPDATE_SYNC_STATUS = "update %s set change_synced=? where change_synced=? and %s";
    
    private static final String INSERT = "insert into %s (%s) values (%s)";
    
    private static final String UPDATE = "update %s set %s where %s";
    
    private static final String DELETE = "delete from %s where %s"; 
    
    private final String tableName;
    private final String logTableName;
    private final String groupName;
    private final int groupOrder;
    private final TreeMap<String,Field> fields = new TreeMap<>();
    private final TreeMap<String,Field> keyFields = new TreeMap<>();

    /**
     * Constructor - Retrieves information from the entity type about where to sync etc.
     * 
     * @param clazz
     */
    public SyncRepository(Class<T> clazz) {
        //may want to throw something specific instead of NPE if annotation not present
        Table def = clazz.getAnnotation(Table.class);
        tableName = String.format("%s.%s", def.schema(), def.name());
        logTableName = String.format("%s.%s", def.schema(), def.log());
        groupName = "".equals(def.group()) ? getClass().getName() : def.group();
        groupOrder = def.groupOrder();
        
        Field[] entityFields = clazz.getDeclaredFields();
        
        for (int i = 0 ; i < entityFields.length ; i++) {
            if (!Modifier.isTransient(entityFields[i].getModifiers())) {
                entityFields[i].setAccessible(true);
                Column colDef = entityFields[i].getDeclaredAnnotation(Column.class);
    
                String fieldName = colDef != null ? colDef.name() : entityFields[i].getName();
                (entityFields[i].getDeclaredAnnotation(Id.class) != null ? keyFields : fields).put(fieldName, entityFields[i]);
            }
        }
    }
    
    /**
     * 
     * Retrieve the name of the group this repository is part of
     * 
     * @return The group
     */
    public String getGroupName() {
        return this.groupName;
    }
    
    /**
     * Retrieve the group order, higher numbers should be processed after lower numbers
     * 
     * @return The ordering number
     */
    public Integer getGroupOrder() {
        return this.groupOrder;
    }
    
    /**
     * Retrieve list of all log entries that are still to be synced in the source database
     * 
     * @return Records to be synced in date order
     */
    public List<Audited<T>> getLatestUnsynced() {
        return getTemplate().query(selectUnsynced(), new Object[]{false,getTemplate().getSyncUser()}, mapper);
    }
    
    /**
     * Check if entity ID has any newer entries in the target database
     * 
     * @param rec
     * @return Count of records after date in the given record
     */
    public int getUnsyncedCountAfter(Audited<T> rec) {
        List<Object> params = asList(getTemplate().getSyncUser(), Date.from(rec.getChangeDate().toInstant(ZoneOffset.UTC)));
        addFieldValues(params, rec.getRecord(), keyFields);
        return getTemplate().queryForObject(selectUnsyncedCountByAccountAfterDate(), 
                                        params.toArray(), 
                                        Integer.class);
    }
    
    /**
     * Flag records as synced in the log table
     * 
     * @param rec
     */
    public void updateSyncStatus(T rec) {
        List<Object> params = asList(true, false);
        addFieldValues(params, rec, keyFields);
        getTemplate().update(updateSyncStatus(), params.toArray());
    }
    
    /**
     * Insert a record into the data table
     * 
     * @param rec
     */
    public void insert(T rec) {
        List<Object> params = new ArrayList<>();
        addFieldValues(params, rec, keyFields);
        addFieldValues(params, rec, fields);
        getTemplate().update(getInsertSql(), params.toArray());
    }
     
    /**
     * Update a record in the data table
     * 
     * @param rec
     */
    public void update(T rec) {
        List<Object> params = new ArrayList<>();
        addFieldValues(params, rec, fields);
        addFieldValues(params, rec, keyFields);
        getTemplate().update(getUpdateSql(), params.toArray());
    }
    
    /**
     * Delete a record in the data table
     * 
     * @param rec
     */
    public void delete(T rec) {
        List<Object> params = new ArrayList<>();
        addFieldValues(params, rec, keyFields);
        getTemplate().update(getDeleteSql(), params.toArray());
    }
    
    /**
     * Get currently active template
     * 
     * @return
     */
    public SyncJdbcTemplate getTemplate() {
        return DatabaseContextHolder.getDBType() == DatabaseContextHolder.Database.FIRST ? firstTemplate : secondTemplate;
    }
    
    protected String selectUnsynced() {
        return String.format(SELECT_UNSYNCED, logTableName);
    }
    
    private String selectUnsyncedCountByAccountAfterDate() {
        return String.format(SELECT_UNSYNCED_COUNT_BY_ACCOUNT_AFTER_DATE, logTableName, String.join(AND, queryPlaceholders(keyFields)));
    }
    
    private String updateSyncStatus() {
        return String.format(UPDATE_SYNC_STATUS, logTableName, String.join(AND, queryPlaceholders(keyFields)));
    }
    
    private String getInsertSql() {
        String fieldNames = String.join(",", keyFields.navigableKeySet()) + "," + String.join(",", fields.navigableKeySet());
        return String.format(INSERT, tableName, fieldNames, insertPlaceholders(keyFields.size() + fields.size()));
    }
    
    private String getUpdateSql() {
        String fieldNames = String.join(",", queryPlaceholders(fields));
        
        String keyNames = String.join(AND, queryPlaceholders(keyFields));
        
        return String.format(UPDATE, tableName, fieldNames, keyNames);
    }
    
    private String getDeleteSql() {      
        String keyNames = String.join(AND, queryPlaceholders(keyFields));
        
        return String.format(DELETE, tableName, keyNames);
    }
    
    private String insertPlaceholders(int num) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0 ; i < num ; i++) {
            if (i > 0) {
                builder.append(",");
            }
            builder.append("?");
        }
        return builder.toString();
    }
    
    private List<String> queryPlaceholders(TreeMap<String, Field> fieldList) {
        return fieldList.navigableKeySet().stream()
                                    .map(k -> String.format("%s=?", k))
                                    .collect(Collectors.toList());
    }
    
    private void addFieldValues(List<Object> list, T rec, TreeMap<String, Field> fieldList) {
        
        fieldList.values().forEach(v -> {
            try {
                list.add(v.get(rec));
            } catch (Exception e) {
                log.error("Entity error:", e);
            }
        });
    }
    
    private List<Object> asList(Object...params) {
        List<Object> list = new ArrayList<>();
        for (int i = 0 ; i < params.length ; i++) {
            list.add(params[i]);
        }
        return list;
    }
}