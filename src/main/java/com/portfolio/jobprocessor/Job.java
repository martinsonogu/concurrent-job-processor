package com.portfolio.jobprocessor;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class Job implements Comparable<Job> {

    private final String id;
    private final Runnable task;
    private final int maxAttempts;
    private final int priority;

    private final AtomicReference<JobStatus> status = new AtomicReference<>(JobStatus.PENDING);
    private final AtomicInteger attempts = new AtomicInteger(0);

    public Job(Runnable task, int maxAttempts) {
        this(task, maxAttempts, 0); // default: normal priority
    }

    public Job(Runnable task, int maxAttempts, int priority) {
        this.id = UUID.randomUUID().toString().substring(0, 8);
        this.task = task;
        this.maxAttempts = maxAttempts;
        this.priority = priority;
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

    public int getPriority() {
        return priority;
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
    public int compareTo(Job other) {
        return Integer.compare(other.priority, this.priority);
    }

    @Override
    public String toString() {
        return "Job{id=" + id + ", status=" + status.get() + ", attempts=" + attempts.get()
                + ", priority=" + priority + "}";
    }
}