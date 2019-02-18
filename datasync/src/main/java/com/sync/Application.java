package com.sync;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * A spring batch application that handles the SIM data sync functionality.
 */
@SpringBootApplication
public class Application {

    /**
     * Main entry point
     *
     * @param args args passed from command line
     */
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

}
