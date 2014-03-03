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
    protected final int MAX_THREAD = 32;

    protected volatile int skip_count = 0;

    public final synchronized void addAll(List<BaseEntity> entities) {
        this.entities.addAll(entities);
    }

    public int getQueueSize() {
        return entities.size();
    }
}
