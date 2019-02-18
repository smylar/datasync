package com.sync.mappers;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.stereotype.Component;

import com.sync.data.AnalysisCode;

/**
 * Map an row from analysis_codes to an object
 * 
 * @author paul.brandon
 *
 */
@Component
public class AnalysisCodeMapper extends AuditMapper<AnalysisCode> {

    @Override
    protected AnalysisCode mapRowImpl(ResultSet rs, int rowNum) throws SQLException {
        return AnalysisCode.builder()
                       .analysisCode(rs.getString("ANALYSIS_CODE"))
                       .description(rs.getString("DESCRIPTION"))
                       .build();
    }

}
