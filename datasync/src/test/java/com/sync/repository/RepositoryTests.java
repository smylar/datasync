package com.sync.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.util.ReflectionTestUtils;

import com.sync.configuration.DatabaseContextHolder;
import com.sync.configuration.SyncJdbcTemplate;
import com.sync.data.Column;
import com.sync.data.Id;
import com.sync.data.Table;
import com.sync.repository.SyncRepository;
import com.sync.service.RepositoryList;

@RunWith(SpringJUnit4ClassRunner.class)
public class RepositoryTests {
    
    @Mock
    private SyncJdbcTemplate template;
    
    private SyncRepository<TestData1> testRep = new SyncRepository<>(TestData1.class);
    private SyncRepository<TestData2> testRep2 = new SyncRepository<>(TestData2.class);
    
    @Before
    public void setup() {
        DatabaseContextHolder.setDBType(DatabaseContextHolder.Database.FIRST);
        ReflectionTestUtils.setField(testRep, "firstTemplate", template);
        ReflectionTestUtils.setField(testRep2, "firstTemplate", template);
    }
    
    @Test
    public void testRepositoryListGrouping() throws Exception {
        SyncRepository<TestData1> rep1 = new SyncRepository<>(TestData1.class);
        SyncRepository<TestData2> rep2 = new SyncRepository<>(TestData2.class);
        SyncRepository<TestData3> rep3 = new SyncRepository<>(TestData3.class);
        List<SyncRepository<?>> repList = new ArrayList<>();
        repList.add(rep1);
        repList.add(rep2);
        repList.add(rep3);
        RepositoryList list = new RepositoryList();
        ReflectionTestUtils.setField(list, "repList", repList);
        list.afterPropertiesSet();
        List<List<SyncRepository<?>>> repGroupList = list.getRepositoryGroupList();
        
        assertEquals(2, repGroupList.size());
        for(List<SyncRepository<?>> group : repGroupList) {
            if (group.size() == 1) {
                assertTrue(group.get(0) == rep2);
            } else {
                assertTrue(group.get(0) == rep3);
                assertTrue(group.get(1) == rep1);
            }
        }      
    }
    
    @Test
    public void testInsertBuild() {
        testRep.insert(getTestData());
        Object[] params = new Object[]{1,"some data"};
        verify(template, times(1)).update("insert into test.table (id,data) values (?,?)", params);
    }
    
    @Test
    public void testUpdateBuild() {
        testRep.update(getTestData());
        Object[] params = new Object[]{"some data",1};
        verify(template, times(1)).update("update test.table set data=? where id=?", params);
    }
    
    @Test
    public void testUpdateBuildMultipleIdsAndColumns() {
        testRep2.update(getTestData2());
        Object[] params = new Object[]{"some data","more data",1,2};
        verify(template, times(1)).update("update test.table set data_alias=?,more=? where id=? and id2=?", params);
    }
    
    @Test
    public void testDeleteBuild() {
        testRep.delete(getTestData());
        Object[] params = new Object[]{1};
        verify(template, times(1)).update("delete from test.table where id=?", params);
    }
    
    @Test
    public void testSyncStatusBuild() {
        testRep.updateSyncStatus(getTestData());
        Object[] params = new Object[]{true, false, 1};
        verify(template, times(1)).update("update test.table_log set change_synced=? where change_synced=? and id=?", params);
    }
    
    private TestData1 getTestData() {
        TestData1 data = new TestData1();
        data.id=1;
        data.data="some data";
        return data;
    }
    
    private TestData2 getTestData2() {
        TestData2 data = new TestData2();
        data.id=1;
        data.id2=2;
        data.data="some data";
        data.more="more data";
        return data;
    }
    
    @Table(schema="test", name="table", log="table_log", group="test", groupOrder=1)
    private static class TestData1 {
        @Id
        private int id;
        private String data;
    }
    
    @Table(schema="test", name="table", log="table_log")
    private static class TestData2 {
        @Id
        private int id;
        @Id
        private int id2;
        private String more;
        @Column(name="data_alias")
        private String data;
        
    }
    
    @Table(schema="test", name="table", log="table_log", group="test")
    private static class TestData3 {
        @Id
        private int id;
        private String data;
    }

}
