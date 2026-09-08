package com.portfolio.jobprocessor;

import java.util.Collection;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

public class JobQueue {

    private final BlockingQueue<Job> queue = new LinkedBlockingQueue<>();
    private final ConcurrentHashMap<String, Job> registry = new ConcurrentHashMap<>();

    public void submit(Job job) {
        registry.put(job.getId(), job);
        queue.add(job);
    }

    public Job take() throws InterruptedException {
        return queue.take();
    }

    public Job getStatus(String jobId) {
        return registry.get(jobId);
    }

    public Collection<Job> allJobs() {
        return registry.values();
    }

    public int pendingCount() {
        return queue.size();
    }
}