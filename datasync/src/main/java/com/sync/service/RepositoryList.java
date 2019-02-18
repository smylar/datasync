package com.sync.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.sync.repository.SyncRepository;

/**
 * Get list of repositories and group together by group name, grouped repositories will be on the same transaction
 * 
 * @author paul.brandon
 *
 */
@Component
public class RepositoryList implements InitializingBean {
    
    @Autowired
    private List<SyncRepository<?>> repList;
    
    private List<List<SyncRepository<?>>> repGroupList;

    @Override
    public void afterPropertiesSet() throws Exception {
        Map<String,List<SyncRepository<?>>> repMap = new HashMap<>();
        repList.forEach(r -> repMap.computeIfAbsent(r.getGroupName(), k -> new ArrayList<SyncRepository<?>>()).add(r) );
        repGroupList = repMap.values().stream()
                                      .peek(list -> list.sort((r1,r2) -> r1.getGroupOrder().compareTo(r2.getGroupOrder()) ))
                                      .collect(Collectors.toList());
        
    }
    
    public List<List<SyncRepository<?>>> getRepositoryGroupList() {
        return repGroupList;
    }

}
