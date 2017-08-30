package kz.bsbnb.usci.eav.persistance.dao.impl.apply;

import kz.bsbnb.usci.eav.model.base.IBaseContainer;
import kz.bsbnb.usci.eav.model.base.IBaseEntity;
import kz.bsbnb.usci.eav.model.base.IBaseSet;
import kz.bsbnb.usci.eav.model.base.IBaseValue;
import kz.bsbnb.usci.eav.model.meta.IMetaValue;
import kz.bsbnb.usci.eav.model.persistable.IPersistable;

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
    protected IPersistable parentFrom = null;
    protected Boolean container = false;
    protected IBaseValue containerFrom = null;
    protected IBaseContainer containerValue = null;
    protected Boolean attribute = false;
    protected IPersistable attributeBase;
    protected Integer persistence = 0;

    protected IPersistable result;

    public BaseValueDSLFactory(ApplyHistoryFactory memorizingTool) {
        this.memorizingTool = memorizingTool;
    }

    public BaseValueDSLFactory from() {
        this.result = null;
        return this;
    }

    public BaseValueDSLFactory from(IBaseValue from) {
        this.result = from;
        return this;
    }

    public BaseValueDSLFactory from(IBaseSet from) {
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

    public BaseValueDSLFactory castedValue(IMetaValue metaValue, IBaseValue value) {
        this.value = memorizingTool.castedValue(metaValue, value);
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

    public BaseValueDSLFactory parentFrom(IBaseValue value) {
        this.parent = true;
        this.parentFrom = value;
        return this;
    }

    public BaseValueDSLFactory parentFrom(IBaseSet value) {
        this.parent = true;
        this.parentFrom = value;
        return this;
    }

    public BaseValueDSLFactory container(Boolean containing) {
        this.container = containing;
        return this;
    }

    public BaseValueDSLFactory containerFrom(IBaseValue value) {
        this.container = true;
        this.containerFrom = value;
        return this;
    }

    public BaseValueDSLFactory containerValue(IBaseContainer value) {
        this.container = true;
        this.containerValue = value;
        return this;
    }

    public BaseValueDSLFactory attribute(Boolean attribute) {
        this.attribute = attribute;
        return this;
    }

    public BaseValueDSLFactory attribute(IBaseSet base) {
        this.attributeBase = base;
        this.attribute = true;
        return this;
    }

    public BaseValueDSLFactory attribute(IBaseEntity base) {
        this.attributeBase = base;
        this.attribute = true;
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
                if (parentFrom == null) {
                    ((IBaseValue) result).setBaseContainer(memorizingTool.baseEntityApplied);
                    ((IBaseValue) result).setMetaAttribute(memorizingTool.metaAttribute);
                } else {
                    if (parentFrom instanceof IBaseValue) {
                        ((IBaseValue) result).setBaseContainer(((IBaseValue) parentFrom).getBaseContainer());
                        ((IBaseValue) result).setMetaAttribute(((IBaseValue) parentFrom).getMetaAttribute());
                    } else if (parentFrom instanceof IBaseSet) {
                        ((IBaseValue) result).setBaseContainer(((IBaseSet) parentFrom));
                        ((IBaseValue) result).setMetaAttribute(memorizingTool.metaAttribute);
                    }

                }
            }

            if (container) {
                if (containerFrom != null) {
                    ((IBaseValue) result).setBaseContainer(containerFrom.getBaseContainer());
                } else if (containerValue != null) {
                    ((IBaseValue) result).setBaseContainer(containerValue);
                } else {
                    ((IBaseValue) result).setBaseContainer(memorizingTool.baseEntityApplied);
                }
            }

            if (attribute) {
                if (attributeBase != null) {
                    if (attributeBase instanceof IBaseSet) {
                        ((IBaseSet) attributeBase).put((IBaseValue) result);
                    } else if (attributeBase instanceof IBaseEntity) {
                        ((IBaseEntity) attributeBase).put(memorizingTool.metaAttribute.getName(), (IBaseValue) result);
                    }
                } else
                    memorizingTool.baseEntityApplied.put(memorizingTool.metaAttribute.getName(), (IBaseValue) result);
            }

        }

        return this;

    }

    public IBaseValue result() {
        if (result == null) this.execute();
        if (result instanceof IBaseValue)
            return (IBaseValue) result;
        return null;
    }

    public BaseValueDSLFactory result(IPersistable result) {
        this.result = result;
        return this;
    }

}



