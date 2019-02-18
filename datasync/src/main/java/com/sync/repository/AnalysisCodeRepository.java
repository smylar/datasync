package com.sync.repository;

import org.springframework.stereotype.Repository;

import com.sync.data.AnalysisCode;

/**
 * For syncing data for the analysis_codes table 
 * 
 * @author paul.brandon
 *
 */
@Repository
public class AnalysisCodeRepository extends SyncRepository<AnalysisCode> {
    
    public AnalysisCodeRepository() {
        super(AnalysisCode.class);
    }
}
