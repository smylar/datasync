package com.sync.mappers;

import java.sql.ResultSet;
import java.sql.SQLException;
import org.springframework.jdbc.core.RowMapper;

import com.sync.data.Audited;

/**
 * Map entity from data table and its audit information from the log table
 * 
 * @author paul.brandon
 *
 * @param <T>
 */
public abstract class AuditMapper<T> implements RowMapper<Audited<T>> {

    @Override
    public Audited<T> mapRow(ResultSet rs, int rowNum) throws SQLException {
        return Audited.<T>builder()
                      .changeDate(rs.getTimestamp("CHANGE_TIME").toLocalDateTime())
                      .changeAction(rs.getString("CHANGE_ACTION"))
                      .changeSynced(rs.getBoolean("CHANGE_SYNCED"))
                      .changeUser(rs.getString("CHANGE_USER"))
                      .changeVersion(rs.getInt("CHANGE_VERSION"))
                      .record(mapRowImpl(rs, rowNum))
                      .build();
                      
       
    }
    
    protected abstract T mapRowImpl(ResultSet rs, int rowNum) throws SQLException;

}
