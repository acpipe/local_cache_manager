package hello;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;

@Service
@Slf4j
public final class InterCacheService {
    private static final long MAX_DELTA_IN_MILLIS = 100L;
    private static final long MIN_DELTA_IN_MILLIS = 50L;
    private static final long RETRY_INTERVAL_IN_MILLIS = 100L;

    @Autowired
    private List<BaseCacheUpdateJob> cacheUpdateJobs;

    @PostConstruct
    private void start() {
        log.info("inter cache: ============== start inter cache service. ==============");

        ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(cacheUpdateJobs.size());

        Map<ExecutionPhase, List<BaseCacheUpdateJob>> allJobs =
                cacheUpdateJobs.stream().collect(Collectors.groupingBy(BaseCacheUpdateJob::executionPhase));

        for (ExecutionPhase executionPhase : ExecutionPhase.values()) {
            log.info("inter cache: start tasks at Phase {}", executionPhase);

            List<BaseCacheUpdateJob> currentJobs = allJobs.get(executionPhase);
            if (CollectionUtils.isEmpty(currentJobs)) {
                log.info("inter cache: no tasks at Phase {}", executionPhase);
                continue;
            }

            CountDownLatch completeTaskLatch = new CountDownLatch(currentJobs.size());

            long initialDelayDelta = getDeltaInMillis(currentJobs.size());
            long initialDelayOffset = 0;
            for (BaseCacheUpdateJob job : currentJobs) {
                JobWorker worker = new JobWorker(job, completeTaskLatch);
                long period = TimeUnit.SECONDS.toMillis(job.getPeriodInSecond());
                scheduledExecutorService.scheduleAtFixedRate(worker, initialDelayOffset, period, TimeUnit.MILLISECONDS);
                log.info("add task={}, period={}s, initialDelay={}ms", job.name(), job.getPeriodInSecond(), initialDelayOffset);

                initialDelayOffset += initialDelayDelta;
            }

            waitForCacheReady(completeTaskLatch, executionPhase);

            log.info("inter cache: complete tasks at Phase {}", executionPhase);
        }

        log.info("inter cache: ============== complete inter cache service. ==============");
    }

    private void waitForCacheReady(CountDownLatch completeTaskLatch, ExecutionPhase executionPhase) {
        try {
            completeTaskLatch.await();
        } catch (InterruptedException e) {
            log.error("inter cache: Waiting for all updating task complete failure at Phase " + executionPhase, e);
        }
    }

    private long getDeltaInMillis(int taskCount) {
        long delta = TimeUnit.SECONDS.toMillis(1) / taskCount;
        delta = Math.min(delta, MAX_DELTA_IN_MILLIS);
        delta = Math.max(delta, MIN_DELTA_IN_MILLIS);
        return delta;
    }

    private static class JobWorker implements Runnable {
        private boolean firstLoad = true;
        private BaseCacheUpdateJob job;
        private CountDownLatch countDownLatch;

        JobWorker(BaseCacheUpdateJob job, CountDownLatch countDownLatch) {
            this.job = job;
            this.countDownLatch = countDownLatch;
        }

        @Override
        public void run() {
            StopWatch stopWatch = new StopWatch();
            stopWatch.start();

            boolean ready = false;
            do {
                try {
                    ready = job.update();

                    if (firstLoad && !ready && job.isEssential()) {
                        log.warn("inter cache: FAILED to load {}, retrying", job.name());
                        Thread.sleep(RETRY_INTERVAL_IN_MILLIS);
                    }
                } catch (Exception e) {
                    log.warn("inter cache: cache update failure! job = {}, error = {}", job.name(), ExceptionUtils.getStackTrace(e));
                }
            } while (firstLoad && !ready && job.isEssential());

            stopWatch.stop();

            if (ready) {
                log.info("inter cache: success to update {}, duration = {}ms", job.name(), stopWatch.getTime());
            } else {
                log.error("inter cache: failed to update {}, firstLoad = {}", job.name(), firstLoad);
            }

            if (firstLoad) {
                firstLoad = false;
                countDownLatch.countDown();
                countDownLatch = null;
            }
        }
    }
}
