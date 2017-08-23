package kz.bsbnb.usci.eav.persistance.dao.impl;

import kz.bsbnb.usci.eav.model.base.IBaseValue;
import kz.bsbnb.usci.eav.model.base.impl.BaseValueFactory;
import kz.bsbnb.usci.eav.model.meta.IMetaValue;
import kz.bsbnb.usci.eav.model.meta.impl.MetaContainerTypes;
import kz.bsbnb.usci.eav.model.type.DataTypes;

import java.util.Date;

/**
 * Created by emles on 22.08.17
 */
public class BaseValueDSLFactory {

    private final int PERSISTANCE_INSERTED = 1;
    private final int PERSISTANCE_UPDATED = 2;
    private final int PERSISTANCE_DELETED = 3;
    private final int PERSISTANCE_PROCESSED = 4;

    private ApplyHistoryFactory memorizingTool;

    private IBaseValue from = null;

    private Long id;
    private Date date;
    private Boolean closed;
    private Boolean last;

    private Boolean parent = true;
    private Boolean attribute = false;
    private Integer persistence = 0;

    private IBaseValue value = null;

    public BaseValueDSLFactory(ApplyHistoryFactory memorizingTool) {
        this.memorizingTool = memorizingTool;
    }

    public BaseValueDSLFactory initialize() {
        return this;
    }

    public BaseValueDSLFactory from(IBaseValue from) {
        this.from = from;
        return this;
    }

    public BaseValueDSLFactory from(IBaseValue from, Boolean closed, Boolean last) {
        this.from = from;
        this.closed = closed;
        this.last = last;
        return this;
    }

    public BaseValueDSLFactory from(Long id, Date date, Boolean closed, Boolean last) {
        this.id = id;
        this.date = date;
        this.closed = closed;
        this.last = last;
        return this;
    }

    public BaseValueDSLFactory id(Long id) {
        this.id = id;
        return this;
    }

    public BaseValueDSLFactory date(Date date) {
        this.date = date;
        return this;
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

    @Override
    public BaseValueDSLFactory clone() throws CloneNotSupportedException {

        BaseValueDSLFactory cloneValue = new BaseValueDSLFactory(memorizingTool).initialize();

        cloneValue.from = this.from;

        cloneValue.id = this.id;
        cloneValue.date = this.date;
        cloneValue.closed = this.closed;
        cloneValue.last = this.last;

        cloneValue.parent = this.parent;
        cloneValue.attribute = this.attribute;
        cloneValue.persistence = this.persistence;

        return cloneValue;

    }

    public BaseValueDSLFactory create() {

        value = BaseValueFactory.create(
                MetaContainerTypes.META_CLASS,
                memorizingTool.metaType,
                id == null
                        ? (from == null ? 0L : from.getId())
                        : id,
                memorizingTool.creditorId,
                new Date((
                        date == null
                                ? (from == null ? new Date() : from.getRepDate())
                                : date
                ).getTime()),
                castedValue(memorizingTool.metaValue, memorizingTool.baseValueSaving),
                closed == null
                        ? from != null && from.isClosed()
                        : closed,
                last == null
                        ? from != null && from.isLast()
                        : last);

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

    public IBaseValue value() {
        if (value == null) this.create();
        return value;
    }

    private Object castedValue(IMetaValue metaValue, IBaseValue baseValue) {
        return metaValue.getTypeCode() == DataTypes.DATE
                ? new Date(((Date) baseValue.getValue()).getTime())
                : baseValue.getValue();
    }

}



