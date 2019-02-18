package com.sync.mappers;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.stereotype.Component;

import com.sync.data.Account;

/**
 * Map an row from accounts to an object
 * 
 * @author paul.brandon
 *
 */
@Component
public class AccountMapper extends AuditMapper<Account> {

    @Override
    protected Account mapRowImpl(ResultSet rs, int rowNum) throws SQLException {
        return Account.builder()
                       .account(rs.getString("ACCOUNT"))
                       .description(rs.getString("DESCRIPTION"))
                       .accountGroup(rs.getString("ACCOUNTGROUP"))
                       .build();
    }

}
