package com.sync.data;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Value;

/**
 * Stores an entity with its audit information retrieved from the log table
 * 
 * @author paul.brandon
 *
 * @param <T>
 */
@Value
@Builder
public class Audited<T> {
    
    private LocalDateTime changeDate;
    private String changeUser;
    private String changeAction;
    private int changeVersion;
    private boolean changeSynced;
    private T record;

}
