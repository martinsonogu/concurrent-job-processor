package com.portfolio.jobprocessor;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {

    private static final int WORKER_COUNT = 3;
    private static final int JOB_COUNT = 8;

    public static void main(String[] args) throws InterruptedException {
        JobQueue jobQueue = new JobQueue();
        ExecutorService pool = Executors.newFixedThreadPool(WORKER_COUNT);

        for (int i = 1; i <= WORKER_COUNT; i++) {
            pool.submit(new Worker("worker-" + i, jobQueue));
        }

        AtomicInteger failCounter = new AtomicInteger(0);
        for (int i = 1; i <= JOB_COUNT; i++) {
            final int jobNumber = i;
            Runnable task = () -> {
                simulateWork();
                if (jobNumber % 3 == 0 && failCounter.getAndIncrement() < 2) {
                    throw new RuntimeException("simulated transient failure");
                }
                System.out.println("   -> did the actual work for job " + jobNumber);
            };
            jobQueue.submit(new Job(task, 3));
        }

        Thread.sleep(5000);

        System.out.println("\nShutting down...");
        pool.shutdownNow();
        boolean terminated = pool.awaitTermination(5, TimeUnit.SECONDS);
        System.out.println("All workers stopped cleanly: " + terminated);

        printSummary(jobQueue);
    }

    private static void simulateWork() {
        try {
            Thread.sleep(300);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private static void printSummary(JobQueue jobQueue) {
        System.out.println("\n--- Job Summary ---");
        for (Job job : jobQueue.allJobs()) {
            System.out.println(job);
        }
    }
}