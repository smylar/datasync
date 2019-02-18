package com.sync.data;

import lombok.Builder;
import lombok.Value;

/**
 * The analysis codes entity
 * 
 * @author paul.brandon
 *
 */
@Value
@Builder
@Table(schema="test", name="analysis_codes", log="analysis_codes_log", group="main")
public class AnalysisCode {
    @Id
    @Column(name="analysis_code")
    private String analysisCode;
    private String description;
}
