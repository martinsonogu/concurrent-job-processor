package com.portfolio.jobprocessor;

import org.junit.jupiter.api.Test;
import java.util.concurrent.atomic.AtomicInteger;
import static org.junit.jupiter.api.Assertions.*;

class WorkerRetryTest {

    @Test
    void jobRetriesExactlyMaxAttemptsBeforeFailing() throws InterruptedException {
        JobQueue queue = new JobQueue();
        AtomicInteger executionCount = new AtomicInteger(0);

        Runnable alwaysFails = () -> {
            executionCount.incrementAndGet();
            throw new RuntimeException("always fails");
        };

        Job job = new Job(alwaysFails, 3);
        queue.submit(job);

        Thread workerThread = new Thread(new Worker("test-worker", queue));
        workerThread.start();

        Thread.sleep(2000);

        workerThread.interrupt();
        workerThread.join(1000);

        assertEquals(3, executionCount.get(), "task should run exactly maxAttempts times");
        assertEquals(JobStatus.FAILED, job.getStatus(), "job should end up FAILED");
        assertEquals(1, queue.getDeadLetterJobs().size(), "job should land in the dead letter queue");
    }
}