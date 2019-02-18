package com.sync.mappers;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.stereotype.Component;

import com.sync.data.Address;

/**
 * Maps an address row to an entity
 * 
 * @author paul.brandon
 *
 */
@Component
public class AddressMapper extends AuditMapper<Address> {
    
    @Override
    protected Address mapRowImpl(ResultSet rs, int rowNum) throws SQLException {
        return Address.builder()
                       .addressId(rs.getLong("YEP_ADDR_ID"))
                       .addressType(rs.getString("ADDRESSTYPE"))
                       .description(rs.getString("DESCRIPTION"))
                       .street(rs.getString("STREET"))
                       .street2(rs.getString("STREET2"))
                       .towncity(rs.getString("TOWNCITY"))
                       .county(rs.getString("COUNTY"))
                       .postcode(rs.getString("POSTCODE"))
                       .country(rs.getString("COUNTRY"))
                       .status(rs.getString("STATUS"))
                       .statusUpdated(rs.getTimestamp("STATUS_UPDATED"))
                       .build();
    }
}
