# Concurrent Job Processor

A multi-threaded background job processing system with a REST API on top,
built to demonstrate core Java concurrency (not just Spring CRUD): thread
pools, a thread-safe priority queue, retry logic with exponential backoff,
dead-letter handling, and graceful shutdown.

## What it does

- Exposes a REST API (Spring Boot) to submit jobs and check their status
- A pool of worker threads pulls jobs from a shared, thread-safe priority
  queue and executes them concurrently
- Each job does real work: reading a CSV of orders and calculating total
  revenue, writing a summary file — not simulated work
- Failed jobs retry automatically with exponential backoff, up to a max
  attempt count, then land in a dedicated dead-letter queue
- Higher-priority jobs are served before lower-priority ones, regardless
  of submission order
- Shutdown is graceful: workers finish their current job and exit cleanly

## Architecture
