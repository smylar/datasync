package com.sync.configuration;

/**
 * Store which database is currently active for the current thread
 * 
 * @author paul.brandon
 *
 */
public class DatabaseContextHolder {
    public enum Database {
        FIRST,
        SECOND
    }
    
    private static final ThreadLocal<Database> contextHolder = new ThreadLocal<>();
    
    public static void setDBType(Database db) {
        contextHolder.set(db);
    }
    
    public static Database getDBType() {
        return contextHolder.get();
    }
}
