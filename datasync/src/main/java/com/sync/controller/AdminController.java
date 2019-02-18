package com.sync.controller;

import java.util.Collections;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.sync.service.Runner;

/**
 * Basic command and control interface
 * 
 * @author paul.brandon
 *
 */
@RestController
@RequestMapping("/admin")
public class AdminController {
    
    private static final String RESPONSE = "response";

    @Autowired
    private Runner runner;
    
    @Autowired
    private ConfigurableApplicationContext context;
    
    @RequestMapping(value="/shutdown", method = RequestMethod.PUT)
    @ResponseBody
    public Map<String,String> shutdown() {
        context.close();
        return Collections.singletonMap(RESPONSE, "Shutting down");
    }
    
    @RequestMapping(value="/pause", method = RequestMethod.PUT)
    @ResponseBody
    public Map<String,String> pause() {
        runner.stop();
        return Collections.singletonMap(RESPONSE, "Process pausing");
    }
    
    @RequestMapping(value="/start", method = RequestMethod.PUT)
    @ResponseBody
    public Map<String,String> start() {
        runner.start();
        return Collections.singletonMap(RESPONSE, "Process starting");
    }
}
