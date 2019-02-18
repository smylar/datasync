package com.sync.mappers;

import static org.mockito.Mockito.when;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.mock;
import org.junit.Test;

import com.sync.data.Account;
import com.sync.data.Address;
import com.sync.data.AnalysisCode;
import com.sync.data.Audited;
import com.sync.mappers.AccountMapper;
import com.sync.mappers.AddressMapper;
import com.sync.mappers.AnalysisCodeMapper;
import com.sync.mappers.AuditMapper;

public class MapperTests {
    
    @Test
    public void testAccountMapper() throws SQLException {
        ResultSet rs = mock(ResultSet.class);
        when(rs.getString("ACCOUNT")).thenReturn("account");
        when(rs.getString("DESCRIPTION")).thenReturn("description");
        when(rs.getString("ACCOUNTGROUP")).thenReturn("accountgroup");
        
        Account account = new AccountMapper().mapRowImpl(rs, 1);
        
        assertEquals("account", account.getAccount());
        assertEquals("description", account.getDescription());
        assertEquals("accountgroup", account.getAccountGroup());
    }
    
    @Test
    public void testAddressMapper() throws SQLException {
        LocalDateTime date1 = LocalDateTime.now();
        Timestamp t1 = Timestamp.from(date1.toInstant(ZoneOffset.UTC));
        ResultSet rs = mock(ResultSet.class);
        when(rs.getLong("YEP_ADDR_ID")).thenReturn(7L);
        when(rs.getString("ADDRESSTYPE")).thenReturn("type");
        when(rs.getString("DESCRIPTION")).thenReturn("desc");
        when(rs.getString("STREET")).thenReturn("street1");
        when(rs.getString("STREET2")).thenReturn("street2");
        when(rs.getString("TOWNCITY")).thenReturn("city");
        when(rs.getString("COUNTY")).thenReturn("county");
        when(rs.getString("POSTCODE")).thenReturn("postcode");
        when(rs.getString("COUNTRY")).thenReturn("country");
        when(rs.getString("STATUS")).thenReturn("state");
        when(rs.getTimestamp("STATUS_UPDATED")).thenReturn(t1);
        
        Address address = new AddressMapper().mapRowImpl(rs, 1);
        
        assertEquals(7L, address.getAddressId());
        assertEquals("type", address.getAddressType());
        assertEquals("desc", address.getDescription());
        assertEquals("street1", address.getStreet());
        assertEquals("street2", address.getStreet2());
        assertEquals("city", address.getTowncity());
        assertEquals("county", address.getCounty());
        assertEquals("postcode", address.getPostcode());
        assertEquals("country", address.getCountry());
        assertEquals("state", address.getStatus());
        assertEquals(t1, address.getStatusUpdated());
        
    }
    
    @Test
    public void testAnalysisCodeMapper() throws SQLException {
        ResultSet rs = mock(ResultSet.class);
        when(rs.getString("ANALYSIS_CODE")).thenReturn("code");
        when(rs.getString("DESCRIPTION")).thenReturn("description");
        
        AnalysisCode code = new AnalysisCodeMapper().mapRowImpl(rs, 1);
        
        assertEquals("code", code.getAnalysisCode());
        assertEquals("description", code.getDescription());
    }
    
    @Test
    public void testAuditMapper() throws SQLException {
        LocalDateTime date1 = LocalDateTime.now();
        Timestamp t1 = Timestamp.from(date1.toInstant(ZoneOffset.UTC));
        ResultSet rs = mock(ResultSet.class);
        when(rs.getTimestamp("CHANGE_TIME")).thenReturn(t1);
        when(rs.getString("CHANGE_ACTION")).thenReturn("INSERT");
        when(rs.getBoolean("CHANGE_SYNCED")).thenReturn(false);
        when(rs.getString("CHANGE_USER")).thenReturn("user");
        when(rs.getInt("CHANGE_VERSION")).thenReturn(1);
        
        AuditMapper<String> mapper = new AuditMapper<String>() {

            @Override
            protected String mapRowImpl(ResultSet rs, int rowNum) throws SQLException {
                return "RECORD";
            }
            
        };
        Audited<String> audit = mapper.mapRow(rs, 1);
        assertEquals("RECORD", audit.getRecord());
        assertEquals(date1, audit.getChangeDate());
        assertEquals("INSERT", audit.getChangeAction());
        assertFalse(audit.isChangeSynced());
        assertEquals("user", audit.getChangeUser());
        assertEquals(1, audit.getChangeVersion());
    }

    
}
