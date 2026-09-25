package com.portfolio.jobprocessor;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Starts the worker threads when the application boots, and shuts them
 * down cleanly when the application stops. Spring calls @PostConstruct
 * automatically right after this component is created, and @PreDestroy
 * right before the app shuts down — we don't call these ourselves.
 */
@Component
public class WorkerPool {

    private static final int WORKER_COUNT = 3;

    private final JobQueue jobQueue;
    private ExecutorService pool;

    // Spring sees JobQueue is also a @Component and automatically hands
    // us the same shared instance here — this is called "dependency
    // injection": we ask for what we need in the constructor, Spring
    // provides it, we don't create it ourselves with `new`.
    public WorkerPool(JobQueue jobQueue) {
        this.jobQueue = jobQueue;
    }

    @PostConstruct
    public void start() {
        pool = Executors.newFixedThreadPool(WORKER_COUNT);
        for (int i = 1; i <= WORKER_COUNT; i++) {
            pool.submit(new Worker("worker-" + i, jobQueue));
        }
        System.out.println("WorkerPool started with " + WORKER_COUNT + " workers.");
    }

    @PreDestroy
    public void stop() throws InterruptedException {
        System.out.println("Shutting down WorkerPool...");
        pool.shutdownNow();
        pool.awaitTermination(5, TimeUnit.SECONDS);
    }
}