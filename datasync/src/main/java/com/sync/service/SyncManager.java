package com.sync.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.sync.configuration.DatabaseContextHolder;
import com.sync.data.Audited;
import com.sync.repository.SyncRepository;

import lombok.extern.slf4j.Slf4j;

/**
 * Defines the synchronisation process
 * 
 * @author paul.brandon
 *
 */
@Service
@Slf4j
@Transactional(propagation = Propagation.REQUIRES_NEW)
public class SyncManager {
    
    public void doSyncGroup(List<SyncRepository<?>> group) {

            group.forEach(rep -> { 
                sync(DatabaseContextHolder.Database.FIRST, DatabaseContextHolder.Database.SECOND, rep);
                sync(DatabaseContextHolder.Database.SECOND, DatabaseContextHolder.Database.FIRST, rep);
                
            });
    }
    
    private <T> void sync(DatabaseContextHolder.Database from, DatabaseContextHolder.Database to, SyncRepository<T> repository) {
            DatabaseContextHolder.setDBType(from);
            List<Audited<T>> fromResult = repository.getLatestUnsynced();
            log.info("{} Found {} records to sync from {}", repository.getClass().getName(), fromResult.size(), repository.getTemplate().getName());
            DatabaseContextHolder.setDBType(to);
            List<T> processed = fromResult.stream()
                                            .peek(acc -> syncObject(acc, repository))
                                            .map(Audited::getRecord)
                                            .collect(Collectors.toList());
            
            DatabaseContextHolder.setDBType(from);
            processed.forEach(repository::updateSyncStatus);
    }
    
    private <T> void syncObject(Audited<T> object, SyncRepository<T> rep) {
        if (rep.getUnsyncedCountAfter(object) == 0) {
            switch (object.getChangeAction().toUpperCase()) {
            case "UPDATE": rep.update(object.getRecord());
                           break;
            case "INSERT": rep.insert(object.getRecord());
                           break;
            case "DELETE": rep.delete(object.getRecord());
                           break;
            default:       break;
        }
        }
        
    }

}
