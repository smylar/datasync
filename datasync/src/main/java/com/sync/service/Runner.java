package com.sync.service;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.sync.repository.SyncRepository;

import lombok.extern.slf4j.Slf4j;

/**
 * Schedule the synchronisation process to run periodically
 * 
 * @author paul.brandon
 *
 */
@Service
@Slf4j
public class Runner implements InitializingBean, DisposableBean {

    @Value("${schedule.delaySeconds:60}")
    private int delaySeconds;
    
    @Autowired
    private SyncManager syncManager;
    
    @Autowired
    private RepositoryList repList;
    
    private ScheduledExecutorService executor = null;

    @Override
    public void afterPropertiesSet() throws Exception {
        start();
    }
    
    /**
     * Stop the executor, spring context will remain
     */
    public void stop() {
        log.warn("Stop called");
        if (executor != null) {
            executor.shutdown();
            executor = null;
        }
    }
    
    /**
     * Start a new executor if one isn't running
     */
    public void start() {
        log.info("Start called");
        if (delaySeconds > -1 && executor == null) {
            executor = Executors.newSingleThreadScheduledExecutor();
            executor.scheduleWithFixedDelay(this::sync, 30, delaySeconds, TimeUnit.SECONDS);
        }
    }

    @Override
    public void destroy() throws Exception {
        log.warn("Shutdown called");
        stop();
    }
    
    
    /**
     * Sync all repositories present in the spring context
     * Repositories are processed by grouping in a <s>parallel</s> serial manner
     * <br/><br/>
     * Have tested repList.getRepositoryGroupList().parallelStream().forEach(this::syncGroup);<br/>
     * Which seems to work with @Transactional(propagation = Propagation.REQUIRES_NEW) though it mentions it
     * suspends existing transactions while it runs another, so may not be much point.
     * The way to do it in parallel is probably having multiple app instances syncing different groups
     * <br/><br/>
     * Leaving single threaded as safer, can revisit later if required.
     */
    public void sync() {
        log.info("Sync started");
        repList.getRepositoryGroupList().forEach(this::syncGroup);
        log.info("Sync complete");
    }
    
    private void syncGroup(List<SyncRepository<?>> group) {
        try {
            syncManager.doSyncGroup(group);
        } catch (Exception e) {
            log.error("Sync error", e);
        }
    }
    
}
