package com.alper.worldcup;

import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableScheduling
public class WorldCupApplication {

    public static void main(String[] args) {
        SpringApplication.run(WorldCupApplication.class, args);
    }
}
