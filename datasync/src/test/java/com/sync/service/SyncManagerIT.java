package com.sync.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.doThrow;

import java.util.ArrayList;
import java.util.List;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.sync.Application;
import com.sync.configuration.TestDataSourceConfiguration;
import com.sync.repository.AccountRepository;
import com.sync.repository.AnalysisCodeRepository;
import com.sync.repository.SyncRepository;
import com.sync.service.RepositoryList;
import com.sync.service.Runner;

import bitronix.tm.BitronixTransactionManager;
import bitronix.tm.TransactionManagerServices;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT,
                classes = { Application.class, TestDataSourceConfiguration.class })
@TestPropertySource({"classpath:/application-test.properties"})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class SyncManagerIT {
    
    @Autowired
    private Runner runner;
    
    @Autowired
    private AccountRepository accRep;
    
    @Autowired
    private AnalysisCodeRepository analysisRep;
    
    @MockBean
    private RepositoryList repositoryList;
    
    @Autowired
    @Qualifier("jdbcTemplateFirst")
    private JdbcTemplate auroraTemplate;
    
    @Autowired
    @Qualifier("jdbcTemplateSecond")
    private JdbcTemplate oracleTemplate;
    
    private static final String IDS = "select account from test.accounts";
    
    private static final String SYNC_COUNT = "select count(*) from test.accounts_log where change_synced=?";
    

    public void setup(SyncRepository<?> rep) {
        List<List<SyncRepository<?>>> repList = new ArrayList<>();
        List<SyncRepository<?>> group = new ArrayList<>();
        group.add(rep);
        repList.add(group);

        when(repositoryList.getRepositoryGroupList()).thenReturn(repList);
    }
    
    @Test
    public void t1_test2wayTransfer() {
        setup(accRep);
        
        List<Integer> accounts = oracleTemplate.queryForList(IDS, Integer.class);
        assertEquals(1, accounts.size());
        assertTrue(accounts.contains(2));
        assertEquals(0, oracleTemplate.queryForObject(SYNC_COUNT, new Object[]{1}, Integer.class), 0);
        
        accounts = auroraTemplate.queryForList(IDS, Integer.class);
        assertEquals(1, accounts.size());
        assertTrue(accounts.contains(1));
        assertEquals(0, auroraTemplate.queryForObject(SYNC_COUNT, new Object[]{true}, Integer.class), 0);
        
        runner.sync();
        
        accounts = oracleTemplate.queryForList(IDS, Integer.class);
        assertEquals(2, accounts.size());
        assertTrue(accounts.contains(1));
        assertTrue(accounts.contains(2));
        assertEquals(1, oracleTemplate.queryForObject(SYNC_COUNT, new Object[]{1}, Integer.class), 0);
        
        accounts = auroraTemplate.queryForList(IDS, Integer.class);
        assertEquals(2, accounts.size());
        assertTrue(accounts.contains(1));
        assertTrue(accounts.contains(2));
        assertEquals(1, auroraTemplate.queryForObject(SYNC_COUNT, new Object[]{true}, Integer.class), 0);
             
    }
    
    @Test
    public void t2_testConflictingUpdate() {
        setup(analysisRep);
        
        runner.sync();

        // check if data was not updated from aurora
        String description = oracleTemplate.queryForObject("SELECT description from test.analysis_codes WHERE analysis_code = 'a'", String.class);
        assertEquals("test - updated oracle", description);


        // change to aurora and check if data was updated
        description = auroraTemplate.queryForObject("SELECT description from test.analysis_codes WHERE analysis_code = 'a'", String.class);
        assertEquals("test - updated oracle", description);

    }
    
    @Test
    public void t3_testGroupFailDoesNotAffectAnother() throws Exception {
        BitronixTransactionManager tm = TransactionManagerServices.getTransactionManager();
        tm.begin();
        auroraTemplate.execute("insert into test.analysis_codes_log values (sysdate, 'testuser', 'INSERT', 1, false, 'b', 'fail test 1')");
        auroraTemplate.execute("insert into test.accounts_log values (sysdate-1, 'testuser', 'INSERT', 0, false, '10', 'fail test 2', 'test')");
        tm.commit();
        List<List<SyncRepository<?>>> repList = new ArrayList<>();
        List<SyncRepository<?>> group = new ArrayList<>();
        List<SyncRepository<?>> group2 = new ArrayList<>();
        AccountRepository accountRep = mock(AccountRepository.class);
        doThrow(new RuntimeException("test")).when(accountRep).getLatestUnsynced();
        group.add(accRep); //this one should get rolled back by failure in next repository, as it is in the same group
        group.add(accountRep);
        group2.add(analysisRep); //this one should be committed
        
        repList.add(group);
        repList.add(group2);
             
        when(repositoryList.getRepositoryGroupList()).thenReturn(repList);
        
        runner.sync();    
        
        assertEquals(1, oracleTemplate.queryForObject("select count(*) from test.analysis_codes where analysis_code='b'", Integer.class), 0);
        assertEquals(0, oracleTemplate.queryForObject("select count(*) from test.accounts where account = '10'", Integer.class), 0);
    }
    
    @Test
    public void t4_testDelete() throws Exception {
        BitronixTransactionManager tm = TransactionManagerServices.getTransactionManager();
        tm.begin();
        auroraTemplate.execute("insert into test.analysis_codes_log values (sysdate, 'testuser', 'DELETE', 2, false, 'a', 'delete test')");
        tm.commit();
        
        setup(analysisRep);
        
        assertEquals(1, oracleTemplate.queryForObject("select count(*) from test.analysis_codes where analysis_code = 'a'", Integer.class), 0);
        
        runner.sync();
        
        assertEquals(0, oracleTemplate.queryForObject("select count(*) from test.analysis_codes where analysis_code = 'a'", Integer.class), 0);
    }

}