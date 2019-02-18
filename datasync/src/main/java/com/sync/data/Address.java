package com.sync.data;

import java.sql.Timestamp;

import lombok.Builder;
import lombok.Value;

/**
 * Data entity storing address information
 * 
 * @author paul.brandon
 *
 */
@Value
@Builder
@Table(schema="test", name="addresses", log="addresses_log", group="main")
public class Address {
    
    @Id
    @Column(name="addr_id")
    private long addressId;
    
    private String addressType;
    
    private String description;
    
    private String street;
    
    private String street2;
    
    private String towncity;
    
    private String county;
    
    private String postcode;
    
    private String country;
    
    private String status;
    
    @Column(name="status_updated")
    private Timestamp statusUpdated;
}
