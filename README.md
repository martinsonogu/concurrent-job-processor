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
HTTP request
│
▼
JobController ──► JobQueue (PriorityBlockingQueue)
│
┌─────────┼─────────┐
▼ ▼ ▼
Worker 1 Worker 2 Worker 3
│ │ │
└─────────┼─────────┘
▼
CsvOrderTask
(reads CSV, sums revenue,
writes summary file)


`WorkerPool` starts the worker threads automatically when the app boots
(via Spring's `@PostConstruct`) and shuts them down cleanly on exit
(`@PreDestroy`). `JobQueue` is a single shared, thread-safe component
injected into both the controller and the workers.

## Run it

Start the API server:

```bash
mvn spring-boot:run
```

The server starts on `http://localhost:8080`.

### Submit a job

```bash
curl -X POST http://localhost:8080/jobs \
  -H "Content-Type: application/json" \
  -d '{"filePath": "data/incoming/orders_1.csv", "priority": 5}'
```

Returns immediately with a job id — the job runs in the background:

```json
{"jobId": "cb59922f", "status": "PENDING"}
```

### Check a job's status

```bash
curl http://localhost:8080/jobs/cb59922f
```

### List all jobs

```bash
curl http://localhost:8080/jobs
```

### List permanently failed jobs

```bash
curl http://localhost:8080/jobs/dead-letter
```

## Run the tests

```bash
mvn test
```

Two tests prove the concurrency guarantees, not just that the code runs:

- **`WorkerRetryTest`** — a job whose task always fails retries exactly
  `maxAttempts` times, then correctly lands in the dead-letter queue
- **`NoDoubleProcessingTest`** — submits 100 jobs to 5 concurrent workers
  and asserts every job runs **exactly once**: none lost, none processed
  twice

## Project structure

src/main/java/com/portfolio/jobprocessor/
├── JobProcessorApplication.java # Spring Boot entry point
├── JobController.java # REST endpoints (POST/GET /jobs)
├── WorkerPool.java # starts/stops workers with the app lifecycle
├── Job.java # a unit of work; tracks status, attempts, priority
├── JobStatus.java # PENDING / RUNNING / DONE / FAILED
├── JobQueue.java # thread-safe priority queue + dead-letter queue
├── Worker.java # pulls jobs, executes, retries with backoff
└── CsvOrderTask.java # the actual work: CSV → revenue summary

src/test/java/com/portfolio/jobprocessor/
├── WorkerRetryTest.java
└── NoDoubleProcessingTest.java

data/
├── incoming/ # sample CSV files to process
└── processed/ # summary files get written here



## Engineering decisions & trade-offs

- **PriorityBlockingQueue over a plain FIFO queue**: jobs need to jump the
  line by urgency, not just submission order. `Job implements Comparable`
  so the queue knows how to order them — no manual sorting logic needed.
- **AtomicReference/AtomicInteger on Job fields** instead of `synchronized`
  methods: status and attempt count are simple single-value updates, so
  lock-free atomics are enough and avoid unnecessary contention between
  threads.
- **A separate dead-letter queue** rather than just marking jobs `FAILED`
  in place: permanent failures need to be easy to find and inspect
  without scanning every job ever submitted — a pattern borrowed from
  real message/job queue systems.
- **Exponential backoff without jitter (yet)**: a simple version first.
  Production systems typically add random jitter to avoid many workers
  retrying in lockstep after a shared failure — noted here as a known
  next improvement rather than left silently undone.
- **Dependency injection over manual wiring**: `WorkerPool` and
  `JobController` both simply ask for a `JobQueue` in their constructor;
  Spring guarantees they get the same shared instance. This replaced
  manually constructing and passing objects around in a plain `main()`
  method.
- **shutdownNow() instead of shutdown()**: interrupts workers mid-`take()`
  so the pool actually terminates instead of blocking forever waiting for
  an empty queue — paired with `@PreDestroy` so this happens automatically
  when the Spring app shuts down, not just when a console app exits.

## Known limitations / next steps

- [ ] Jobs are stored in memory only — restarting the app loses job history
- [ ] No jitter on retry backoff (see above)
- [ ] No authentication on the API endpoints
- [ ] No pagination on `GET /jobs` — fine at small scale, would need it
      for a large job history
- [ ] Could add persistence (Postgres) so job state survives a restart
