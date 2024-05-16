package com.ariel.findfriendbackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @author Ariel
 */
@SpringBootApplication
@EnableScheduling
public class FindFriendBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(FindFriendBackendApplication.class, args);
    }

}
