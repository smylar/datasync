package com.sync.data;

import lombok.Builder;
import lombok.Value;

/**
 * The account entity
 * 
 * @author paul.brandon
 *
 */
@Value
@Builder
@Table(schema="test", name="accounts", log="accounts_log", group="main")
public class Account {
    @Id
    private String account;
    private String description;
    private String accountGroup;
}
