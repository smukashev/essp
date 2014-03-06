package kz.bsbnb.usci.sync.job;

import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;
import kz.bsbnb.usci.sync.job.impl.ProcessJob;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * @author k.tulbassiyev
 */
public abstract class AbstractDataJob extends AbstractJob {
    protected final List<BaseEntity> entities
            = Collections.synchronizedList(new ArrayList<BaseEntity>());

    protected final List<BaseEntity> entitiesInProcess
            = Collections.synchronizedList(new ArrayList<BaseEntity>());

    protected final List<ProcessJob> processingJobs = new ArrayList<ProcessJob>();

    protected final int SLEEP_TIME_NORMAL = 1000;
    protected final int SLEEP_TIME_LONG = 5000;
    protected final int SKIP_TIME_MAX = 10;
    protected final int MAX_THREAD = 42;
    protected final int MIN_THREAD = 16;
    protected final int WATCH_INTERVAL = 1000;
    protected final int THREAD_INCREMENT_STEP = 1;
    protected final double THRESHOLD = 0.1;
    protected int currentThread = ((MAX_THREAD + MIN_THREAD) / 2);
    protected boolean autoChooseThreshold = false;

    protected volatile int skip_count = 0;

    public final synchronized void addAll(List<BaseEntity> entities) {
        this.entities.addAll(entities);
    }

    public int getQueueSize() {
        return entities.size();
    }

    public int getCurrentThread() {
        return currentThread;
    }

    public void setCurrentThread(int currentThread) {
        this.currentThread = currentThread;
    }

    public void setAutoChooseThreshold(boolean autoChooseThreshold) {
        this.autoChooseThreshold = autoChooseThreshold;
    }
}
