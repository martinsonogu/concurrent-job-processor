package com.portfolio.jobprocessor;

import java.util.Collection;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.PriorityBlockingQueue;

public class JobQueue {

    private final BlockingQueue<Job> queue = new PriorityBlockingQueue<>();
    private final ConcurrentHashMap<String, Job> registry = new ConcurrentHashMap<>();
    private final Queue<Job> deadLetterQueue = new ConcurrentLinkedQueue<>();

    public void submit(Job job) {
        registry.put(job.getId(), job);
        queue.add(job);
    }

    public Job take() throws InterruptedException {
        return queue.take();
    }

    public void sendToDeadLetter(Job job) {
        deadLetterQueue.add(job);
    }

    public Collection<Job> getDeadLetterJobs() {
        return deadLetterQueue;
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