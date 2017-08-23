package kz.bsbnb.usci.eav.persistance.dao.impl.apply;

import kz.bsbnb.usci.eav.model.base.IBaseValue;

/**
 * Created by emles on 23.08.17
 */
public abstract class BaseValueDSLFactory {

    protected final int PERSISTANCE_INSERTED = 1;
    protected final int PERSISTANCE_UPDATED = 2;
    protected final int PERSISTANCE_DELETED = 3;
    protected final int PERSISTANCE_PROCESSED = 4;

    protected ApplyHistoryFactory memorizingTool;

    protected IBaseValue from;

    protected Boolean closed;
    protected Boolean last;

    protected Boolean parent = true;
    protected Boolean attribute = false;
    protected Integer persistence = 0;

    protected IBaseValue value;

    public BaseValueDSLFactory(ApplyHistoryFactory memorizingTool) {
        this.memorizingTool = memorizingTool;
    }

    public BaseValueDSLFactory closed(Boolean closed) {
        this.closed = closed;
        return this;
    }

    public BaseValueDSLFactory last(Boolean last) {
        this.last = last;
        return this;
    }

    public BaseValueDSLFactory parent(Boolean parent) {
        this.parent = parent;
        return this;
    }

    public BaseValueDSLFactory attribute(Boolean attribute) {
        this.attribute = attribute;
        return this;
    }

    public BaseValueDSLFactory inserted() {
        this.persistence = PERSISTANCE_INSERTED;
        return this;
    }

    public BaseValueDSLFactory updated() {
        this.persistence = PERSISTANCE_UPDATED;
        return this;
    }

    public BaseValueDSLFactory deleted() {
        this.persistence = PERSISTANCE_DELETED;
        return this;
    }

    public BaseValueDSLFactory processed() {
        this.persistence = PERSISTANCE_PROCESSED;
        return this;
    }

    public BaseValueDSLFactory create() {

        if (parent) {
            value.setBaseContainer(memorizingTool.baseEntityApplied);
            value.setMetaAttribute(memorizingTool.metaAttribute);
        }

        if (attribute) memorizingTool.baseEntityApplied.put(memorizingTool.metaAttribute.getName(), value);

        switch (persistence) {
            case (PERSISTANCE_INSERTED):
                memorizingTool.baseEntityManager.registerAsInserted(value);
                break;
            case (PERSISTANCE_UPDATED):
                memorizingTool.baseEntityManager.registerAsUpdated(value);
                break;
            case (PERSISTANCE_DELETED):
                memorizingTool.baseEntityManager.registerAsDeleted(value);
                break;
            case (PERSISTANCE_PROCESSED):
                memorizingTool.baseEntityManager.registerProcessedBaseEntity(memorizingTool.baseEntityApplied);
                break;
        }

        return this;

    }

    public abstract IBaseValue value();

}



