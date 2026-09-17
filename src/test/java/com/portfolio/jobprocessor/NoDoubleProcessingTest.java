package com.portfolio.jobprocessor;

import org.junit.jupiter.api.Test;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import static org.junit.jupiter.api.Assertions.*;

class NoDoubleProcessingTest {

    @Test
    void noJobIsProcessedTwiceOrLost() throws InterruptedException {
        JobQueue queue = new JobQueue();
        int jobCount = 100;
        ConcurrentHashMap<String, AtomicInteger> executionCounts = new ConcurrentHashMap<>();

        for (int i = 0; i < jobCount; i++) {
            AtomicInteger counter = new AtomicInteger(0);
            Job job = new Job(counter::incrementAndGet, 1);
            executionCounts.put(job.getId(), counter);
            queue.submit(job);
        }

        ExecutorService pool = Executors.newFixedThreadPool(5);
        for (int i = 0; i < 5; i++) {
            pool.submit(new Worker("w" + i, queue));
        }

        Thread.sleep(2000);
        pool.shutdownNow();
        pool.awaitTermination(2, TimeUnit.SECONDS);

        assertEquals(jobCount, executionCounts.size(), "should have exactly jobCount jobs tracked");

        for (AtomicInteger count : executionCounts.values()) {
            assertEquals(1, count.get(), "each job should run exactly once, no more, no less");
        }
    }
}