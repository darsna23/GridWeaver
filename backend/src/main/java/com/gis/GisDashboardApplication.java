package com.gis;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class GisDashboardApplication {
    public static void main(String[] args) {
        SpringApplication.run(GisDashboardApplication.class, args);
    }
}