package com.sync.repository;

import org.springframework.stereotype.Repository;

import com.sync.data.Account;

/**
 * For syncing data for the accounts table
 * 
 * @author paul.brandon
 *
 */
@Repository
public class AccountRepository extends SyncRepository<Account> {

    public AccountRepository() {
        super(Account.class);
    }
}
