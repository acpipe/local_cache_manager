package hello;

import java.util.concurrent.TimeUnit;

public abstract class BaseCacheUpdateJob {

    protected static final long PERIOD_TWO_SECOND = 2;
    protected static final long PERIOD_ONE_MINUTE = TimeUnit.MINUTES.toSeconds(1);
    protected static final long PERIOD_TEN_MINUTE = TimeUnit.MINUTES.toSeconds(10);
    protected static final long PERIOD_THIRTY_MINUTE = TimeUnit.MINUTES.toSeconds(30);
    protected static final long PERIOD_ONE_HOUR = TimeUnit.HOURS.toSeconds(1);
    protected static final long PERIOD_EIGHT_HOUR = TimeUnit.HOURS.toSeconds(8);
    protected static final long PERIOD_ONE_DAY = TimeUnit.DAYS.toSeconds(1);

    public String name() {
        return this.getClass().getSimpleName();
    }

    public long getPeriodInSecond() {
        return PERIOD_ONE_HOUR;
    }

    /**
     * Returns true if our service should NOT be up before this job is loaded successfully.
     */
    public boolean isEssential() {
        return false;
    }

    public ExecutionPhase executionPhase() {
        return ExecutionPhase.ONE;
    }

    public abstract boolean update();
}
