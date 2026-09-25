package com.portfolio.jobprocessor;

import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.Map;

/**
 * Exposes the job system over HTTP. This is what turns the project from
 * "a program you run" into "an application you send requests to."
 */
@RestController
@RequestMapping("/jobs")
public class JobController {

    private final JobQueue jobQueue;

    public JobController(JobQueue jobQueue) {
        this.jobQueue = jobQueue;
    }

    /**
     * POST /jobs
     * Body (JSON): { "filePath": "data/incoming/orders_1.csv", "priority": 0 }
     * Submits a new CSV-processing job and immediately returns its id,
     * without waiting for it to finish — the job runs in the background.
     */
    @PostMapping
    public Map<String, String> submitJob(@RequestBody SubmitJobRequest request) {
        Runnable task = new CsvOrderTask(request.filePath(), "data/processed");
        Job job = new Job(task, 3, request.priority());
        jobQueue.submit(job);
        return Map.of("jobId", job.getId(), "status", job.getStatus().toString());
    }

    /**
     * GET /jobs/{id}
     * Check the current status of a specific job by its id.
     */
    @GetMapping("/{id}")
    public Job getJob(@PathVariable String id) {
        Job job = jobQueue.getStatus(id);
        if (job == null) {
            throw new RuntimeException("No job found with id " + id);
        }
        return job;
    }

    /**
     * GET /jobs
     * List every job the system has seen, with its current status.
     */
    @GetMapping
    public Collection<Job> getAllJobs() {
        return jobQueue.allJobs();
    }

    /**
     * GET /jobs/dead-letter
     * List only the jobs that permanently failed.
     */
    @GetMapping("/dead-letter")
    public Collection<Job> getDeadLetterJobs() {
        return jobQueue.getDeadLetterJobs();
    }

    // A small record just to describe the shape of the JSON someone
    // sends in a POST /jobs request body.
    public record SubmitJobRequest(String filePath, int priority) {}
}