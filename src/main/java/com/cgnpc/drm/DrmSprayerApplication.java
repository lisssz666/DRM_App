package com.cgnpc.drm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class DrmSprayerApplication {
    public static void main(String[] args) {
        SpringApplication.run(DrmSprayerApplication.class, args);
    }
}