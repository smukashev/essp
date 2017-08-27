package kz.bsbnb.usci.eav.persistance.dao.impl.apply;

import kz.bsbnb.usci.eav.model.base.IBaseValue;
import kz.bsbnb.usci.eav.model.meta.IMetaType;

import java.util.Date;

/**
 * Created by emles on 23.08.17
 */
public class BaseValueDSLFactory {

    protected final int PERSISTANCE_INSERTED = 1;
    protected final int PERSISTANCE_UPDATED = 2;
    protected final int PERSISTANCE_DELETED = 3;
    protected final int PERSISTANCE_PROCESSED = 4;
    protected final int PERSISTANCE_OPTIMIZER = 5;

    protected ApplyHistoryFactory memorizingTool;

    protected IBaseValue from;

    protected Long id;
    protected Date date;
    protected Object value;
    protected Boolean closed;
    protected Boolean last;

    protected Boolean parent = false;
    protected Boolean attribute = false;
    protected Integer persistence = 0;

    protected IBaseValue result;

    public BaseValueDSLFactory(ApplyHistoryFactory memorizingTool) {
        this.memorizingTool = memorizingTool;
    }

    public BaseValueDSLFactory from(IBaseValue from) {
        this.result = from;
        return this;
    }

    public BaseValueDSLFactory id(Long id) {
        this.id = id;
        return this;
    }

    public BaseValueDSLFactory idFrom(IBaseValue value) {
        this.id = value.getId();
        return this;
    }

    public BaseValueDSLFactory date(Date date) {
        this.date = date;
        return this;
    }

    public BaseValueDSLFactory dateFrom(IBaseValue value) {
        this.date = value.getRepDate();
        return this;
    }

    public BaseValueDSLFactory value(Object value) {
        this.value = value;
        return this;
    }

    public BaseValueDSLFactory castedValue(IBaseValue value) {
        this.value = memorizingTool.castedValue(value);
        return this;
    }

    public BaseValueDSLFactory newValue(IBaseValue value) {
        this.value = memorizingTool.newValue(value);
        return this;
    }

    public BaseValueDSLFactory closed(Boolean closed) {
        this.closed = closed;
        return this;
    }

    public BaseValueDSLFactory closedFrom(IBaseValue value) {
        this.closed = value.isClosed();
        return this;
    }

    public BaseValueDSLFactory last(Boolean last) {
        this.last = last;
        return this;
    }

    public BaseValueDSLFactory lastFrom(IBaseValue value) {
        this.last = value.isLast();
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

    public BaseValueDSLFactory optimizer() {
        this.persistence = PERSISTANCE_OPTIMIZER;
        return this;
    }

    public BaseValueDSLFactory execute() {

        block:
        {

            switch (persistence) {
                case (PERSISTANCE_INSERTED):
                    memorizingTool.baseEntityManager.registerAsInserted(result);
                    break;
                case (PERSISTANCE_UPDATED):
                    memorizingTool.baseEntityManager.registerAsUpdated(result);
                    break;
                case (PERSISTANCE_DELETED):
                    memorizingTool.baseEntityManager.registerAsDeleted(result);
                    break;
                case (PERSISTANCE_PROCESSED):
                    memorizingTool.baseEntityManager.registerProcessedBaseEntity(memorizingTool.baseEntityApplied);
                    break;
                case (PERSISTANCE_OPTIMIZER):
                    memorizingTool.baseEntityManager.addOptimizerEntity(memorizingTool.baseEntityApplied);
                    break block;
            }

            if (parent) {
                result.setBaseContainer(memorizingTool.baseEntityApplied);
                result.setMetaAttribute(memorizingTool.metaAttribute);
            }

            if (attribute) memorizingTool.baseEntityApplied.put(memorizingTool.metaAttribute.getName(), result);

        }

        return this;

    }

    public IBaseValue result() {
        if (result == null) this.execute();
        return result;
    }

}



