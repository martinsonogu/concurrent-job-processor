package com.portfolio.jobprocessor;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class Job {

    private final String id;
    private final Runnable task;
    private final int maxAttempts;

    private final AtomicReference<JobStatus> status = new AtomicReference<>(JobStatus.PENDING);
    private final AtomicInteger attempts = new AtomicInteger(0);

    public Job(Runnable task, int maxAttempts) {
        this.id = UUID.randomUUID().toString().substring(0, 8);
        this.task = task;
        this.maxAttempts = maxAttempts;
    }
    public String getId() {
        return id;
    }

    public Runnable getTask() {
        return task;
    }

    public int getMaxAttempts() {
        return maxAttempts;
    }

    public JobStatus getStatus() {
        return status.get();
    }

    public void setStatus(JobStatus newStatus) {
        status.set(newStatus);
    }

    public int incrementAndGetAttempts() {
        return attempts.incrementAndGet();
    }

    public int getAttempts() {
        return attempts.get();
    }

    @Override
    public String toString() {
        return "Job{id=" + id + ", status=" + status.get() + ", attempts=" + attempts.get() + "}";
    }
}