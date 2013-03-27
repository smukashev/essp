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

    protected final List<ProcessJob> processJobs = new ArrayList<ProcessJob>();
    protected final List<ProcessJob> waitingJobs = new ArrayList<ProcessJob>();

    protected final int SLEEP_TIME_NORMAL = 1000;
    protected final int SLEEP_TIME_LONG = 5000;
    protected final int SKIP_TIME_MAX = 10;
    protected final int MAX_THREAD = 30;

    protected volatile int skip_count = 0;

    protected BaseEntity getClearEntity() {
        Iterator<BaseEntity> iterator = entities.iterator();

        while(iterator.hasNext()) {
            BaseEntity entity = iterator.next();

            if(isClear(entity)) {
                iterator.remove();
                return entity;
            }
        }

        return null;
    }

    protected boolean isClear(BaseEntity baseEntity) {
        for (BaseEntity entity : entitiesInProcess)
            if(hasCrossLine(baseEntity, entity))
                return false;

        return true;
    }

    protected boolean hasCrossLine(BaseEntity entity1, BaseEntity entity2) {
        // todo: implement
        /*if(entity1.equals(entity2))
            return true;*/

        return false;
    }

    public void addAll(List<BaseEntity> entities) {
        this.entities.addAll(entities);
    }
}
