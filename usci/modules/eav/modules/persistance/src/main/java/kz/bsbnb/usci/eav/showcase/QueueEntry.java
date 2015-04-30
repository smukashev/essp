package kz.bsbnb.usci.eav.showcase;

import kz.bsbnb.usci.eav.manager.IBaseEntityManager;
import kz.bsbnb.usci.eav.model.base.IBaseEntity;
import java.io.Serializable;

public class QueueEntry implements Serializable {
    private final static long serialVersionUID = 1L;

    private IBaseEntity baseEntitySaving;
    private IBaseEntity baseEntityLoaded;
    private IBaseEntity baseEntityApplied;
    private Long scId = null;

    public Long getScId() {
        return scId;
    }

    public QueueEntry setScId(Long scId) {
        this.scId = scId;
        return this;
    }

    transient IBaseEntityManager entityManager;

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
