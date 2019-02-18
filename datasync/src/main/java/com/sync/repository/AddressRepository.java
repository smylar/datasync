package com.sync.repository;

import org.springframework.stereotype.Repository;

import com.sync.data.Address;

/**
 * For syncing data for the addresses table
 * 
 * @author paul.brandon
 *
 */
@Repository
public class AddressRepository extends SyncRepository<Address> {

    public AddressRepository() {
        super(Address.class);
    }
}
