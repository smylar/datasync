package com.sync.configuration;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.jta.bitronix.BitronixXADataSourceWrapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import oracle.jdbc.xa.client.OracleXADataSource;
import org.postgresql.xa.PGXADataSource;

import javax.sql.DataSource;

/**
 * DataSource configuration
 */
@EnableTransactionManagement
@Configuration
@ConditionalOnProperty(value="mode", havingValue="prod", matchIfMissing=true)
public class DataSourceConfiguration {
    
    /**
     * Oracle DataSource instance.
     *
     * @return primary DataSource
     * @throws Exception 
     */
    @Bean(name="oracle")
    public DataSource oracleDataSource(@Value("${oracle.datasource.url}") String url, 
                                       @Value("${oracle.datasource.username}") String user, 
                                       @Value("${oracle.datasource.password}") String pass) throws Exception {
        
        OracleXADataSource ds = new OracleXADataSource();
        ds.setURL(url);
        ds.setUser(user);
        ds.setPassword(pass);
        BitronixXADataSourceWrapper wrapper = new BitronixXADataSourceWrapper();
        return wrapper.wrapDataSource(ds);
    }

    /**
     * Create new Aurora DataSource instance.
     *
     * @return Aurora DataSource
     * @throws Exception 
     */
    @Bean(name="aurora")
    public DataSource auroraDataSource(@Value("${aurora.datasource.url}") String url, 
                                       @Value("${aurora.datasource.username}") String user, 
                                       @Value("${aurora.datasource.password}") String pass) throws Exception {
        
        PGXADataSource ds = new PGXADataSource();
        ds.setURL(url);
        ds.setUser(user);
        ds.setPassword(pass);
        BitronixXADataSourceWrapper wrapper = new BitronixXADataSourceWrapper();
        return wrapper.wrapDataSource(ds);
    }
    
    @Bean("jdbcTemplateFirst")
    public SyncJdbcTemplate jdbcTemplateFirst(@Qualifier("aurora") DataSource dataSource, @Value("${aurora.datasource.username}") String user) {
        return new SyncJdbcTemplate(dataSource, user, "AURORA");
    }

    @Bean("jdbcTemplateSecond")
    public SyncJdbcTemplate jdbcTemplateSecond(@Qualifier("oracle") DataSource dataSource, @Value("${oracle.datasource.username}") String user) {   
        return new SyncJdbcTemplate(dataSource, user, "ORACLE");
    }
}
