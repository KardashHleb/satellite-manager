package com.satellite.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.r2dbc.R2dbcAutoConfiguration;
import org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration;

@SpringBootApplication(exclude = {
        R2dbcAutoConfiguration.class,
        ValidationAutoConfiguration.class
})
public class SatelliteServerApplication {

    public static void main(String[] args) {
        System.setProperty("file.encoding", "UTF-8");
        System.setProperty("console.encoding", "UTF-8");
        SpringApplication.run(SatelliteServerApplication.class, args);
    }
}
