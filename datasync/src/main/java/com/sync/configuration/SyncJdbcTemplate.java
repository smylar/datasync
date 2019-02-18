package com.sync.configuration;

import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;

import lombok.Getter;

/**
 * Extension of JdbcTemplate to store the syncUser so it can be excluded from queries
 * 
 * @author paul.brandon
 *
 */
public class SyncJdbcTemplate extends JdbcTemplate {
    @Getter
    private final String syncUser;
    
    @Getter
    private final String name;
    
    public SyncJdbcTemplate(DataSource ds, String syncUser, String name) {
        super(ds);
        this.syncUser = syncUser.toLowerCase();
        this.name = name;
    }
}
