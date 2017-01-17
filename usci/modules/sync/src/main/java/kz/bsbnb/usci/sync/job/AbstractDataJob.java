package kz.bsbnb.usci.sync.job;

import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;
import kz.bsbnb.usci.sync.job.impl.ProcessJob;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author k.tulbassiyev
 */
public abstract class AbstractDataJob extends Thread {
    /* List of entities to process */
    protected final List<BaseEntity> entities = new ArrayList<>();

    /* Jobs to process entities */
    protected final List<ProcessJob> processingJobs = new ArrayList<>();

    /* Number of sleeps until SLEEP_TIME_LONG */
    protected final int SKIP_TIME_MAX = 10;

    /* Counter to SKIP_TIME_MAX */
    protected volatile int skip_count = 0;

    /* Inactive sleep time */
    protected final int SLEEP_TIME_NORMAL = 100;

    /* Sleep time after SKIP_TIME_MAX */
    protected final int SLEEP_TIME_LONG = 500;

    /* Number of processed entities to show stats */
    protected final int STAT_INTERVAL = 1000;

    protected Set<Long> batches = new HashSet<>();

    protected Set<Long> finishedCreditors = new HashSet<>();

    public final synchronized void addAll(List<BaseEntity> entities) {
        for (BaseEntity entity : entities) {
            if(entity.isLastInBatch())
                batches.add(entity.getBatchId());
        }

        this.entities.addAll(entities);
    }

    public int getQueueSize() {
        return entities.size();
    }

    public Set<Long> getFinishedCreditors(){
        synchronized (this) {
            Set<Long> ret = new HashSet<>(finishedCreditors);
            finishedCreditors.clear();
            return ret;
        }
    }
}
