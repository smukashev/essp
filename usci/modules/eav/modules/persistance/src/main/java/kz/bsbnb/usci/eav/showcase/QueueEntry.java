package kz.bsbnb.usci.eav.showcase;

import kz.bsbnb.usci.eav.manager.IBaseEntityManager;
import kz.bsbnb.usci.eav.model.base.IBaseEntity;

/**
 * Created by almaz on 6/18/14.
 */
public class QueueEntry {

    IBaseEntity baseEntitySaving;
    IBaseEntity baseEntityLoaded;
    IBaseEntity baseEntityApplied;
    IBaseEntityManager entityManager;

    public IBaseEntity getBaseEntitySaving() {
        return baseEntitySaving;
    }

    public QueueEntry setBaseEntitySaving(IBaseEntity baseEntitySaving) {
        this.baseEntitySaving = baseEntitySaving;
        return this;
    }

    public IBaseEntity getBaseEntityLoaded() {
        return baseEntityLoaded;
    }

    public QueueEntry setBaseEntityLoaded(IBaseEntity baseEntityLoaded) {
        this.baseEntityLoaded = baseEntityLoaded;
        return this;
    }

    public IBaseEntity getBaseEntityApplied() {
        return baseEntityApplied;
    }

    public QueueEntry setBaseEntityApplied(IBaseEntity baseEntityApplied) {
        this.baseEntityApplied = baseEntityApplied;
        return this;
    }

    public IBaseEntityManager getEntityManager() {
        return entityManager;
    }

    public QueueEntry setEntityManager(IBaseEntityManager entityManager) {
        this.entityManager = entityManager;
        return this;
    }
}
