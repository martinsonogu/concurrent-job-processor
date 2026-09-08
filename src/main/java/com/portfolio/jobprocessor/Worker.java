package com.portfolio.jobprocessor;

public class Worker implements Runnable {

    private final String name;
    private final JobQueue jobQueue;

    public Worker(String name, JobQueue jobQueue) {
        this.name = name;
        this.jobQueue = jobQueue;
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            Job job;
            try {
                job = jobQueue.take();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }

            executeWithRetry(job);
        }
        System.out.println("[" + name + "] shutting down");
    }

    private void executeWithRetry(Job job) {
        while (job.getAttempts() < job.getMaxAttempts()) {
            int attemptNumber = job.incrementAndGetAttempts();
            job.setStatus(JobStatus.RUNNING);

            try {
                System.out.println("[" + name + "] running " + job + " (attempt " + attemptNumber + ")");
                job.getTask().run();
                job.setStatus(JobStatus.DONE);
                System.out.println("[" + name + "] completed " + job);
                return;
            } catch (Exception e) {
                System.out.println("[" + name + "] attempt " + attemptNumber + " failed for "
                        + job.getId() + ": " + e.getMessage());

                if (attemptNumber >= job.getMaxAttempts()) {
                    job.setStatus(JobStatus.FAILED);
                    System.out.println("[" + name + "] giving up on " + job);
                    return;
                }

                backoff(attemptNumber);
            }
        }
    }

    private void backoff(int attemptNumber) {
        long delayMs = (long) (200 * Math.pow(2, attemptNumber - 1));
        try {
            Thread.sleep(delayMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}