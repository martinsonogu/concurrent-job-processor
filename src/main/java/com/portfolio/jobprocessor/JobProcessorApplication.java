package com.portfolio.jobprocessor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the Spring Boot application. This replaces Main.java's
 * old role of manually starting things — Spring now manages the lifecycle
 * of our beans (JobQueue, WorkerPool) instead of us wiring it by hand.
 */
@SpringBootApplication
public class JobProcessorApplication {
    public static void main(String[] args) {
        SpringApplication.run(JobProcessorApplication.class, args);
    }
}