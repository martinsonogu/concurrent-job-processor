package com.portfolio.jobprocessor;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Main {

    private static final int WORKER_COUNT = 3;
    private static final String INCOMING_DIR = "data/incoming";
    private static final String PROCESSED_DIR = "data/processed";

    public static void main(String[] args) throws InterruptedException {
        JobQueue jobQueue = new JobQueue();

        File incomingDir = new File(INCOMING_DIR);
        File[] csvFiles = incomingDir.listFiles((dir, name) -> name.endsWith(".csv"));

        if (csvFiles != null) {
            for (File csvFile : csvFiles) {
                Runnable task = new CsvOrderTask(csvFile.getPath(), PROCESSED_DIR);
                jobQueue.submit(new Job(task, 3, 0)); // priority 0 = normal
            }
        }

        Runnable urgentTask = () -> System.out.println("   -> handled urgent alert!");
        jobQueue.submit(new Job(urgentTask, 3, 10)); // priority 10 = urgent

        System.out.println("All jobs submitted. Starting workers now...\n");

        ExecutorService pool = Executors.newFixedThreadPool(WORKER_COUNT);
        for (int i = 1; i <= WORKER_COUNT; i++) {
            pool.submit(new Worker("worker-" + i, jobQueue));
        }

        Thread.sleep(3000);

        System.out.println("\nShutting down...");
        pool.shutdownNow();
        boolean terminated = pool.awaitTermination(5, TimeUnit.SECONDS);
        System.out.println("All workers stopped cleanly: " + terminated);

        printSummary(jobQueue);
    }

    private static void printSummary(JobQueue jobQueue) {
        System.out.println("\n--- Job Summary ---");
        for (Job job : jobQueue.allJobs()) {
            System.out.println(job);
        }
    }
}