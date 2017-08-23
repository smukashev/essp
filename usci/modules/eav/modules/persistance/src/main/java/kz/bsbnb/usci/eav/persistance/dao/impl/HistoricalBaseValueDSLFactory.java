package kz.bsbnb.usci.eav.persistance.dao.impl;

import kz.bsbnb.usci.eav.model.base.IBaseValue;

/**
 * Created by emles on 23.08.17
 */
public class HistoricalBaseValueDSLFactory {

    private final int HISTORICAL_EXISTING = 1;
    private final int HISTORICAL_CLOSED = 2;
    private final int HISTORICAL_PREVIOUS = 3;
    private final int HISTORICAL_NEXT = 4;
    private final int HISTORICAL_LAST = 5;

    private final int PERSISTANCE_INSERTED = 1;
    private final int PERSISTANCE_UPDATED = 2;
    private final int PERSISTANCE_DELETED = 3;
    private final int PERSISTANCE_PROCESSED = 4;

    private ApplyHistoryFactory memorizingTool;

    private IBaseValue from;
    private int historical = 0;

    private Boolean closed;
    private Boolean last;

    private Boolean parent = true;
    private Boolean attribute = false;
    private Integer persistence = 0;

    private IBaseValue value;

    public HistoricalBaseValueDSLFactory(ApplyHistoryFactory memorizingTool) {
        this.memorizingTool = memorizingTool;
    }

    public HistoricalBaseValueDSLFactory fromExisting(IBaseValue from) {
        this.from = from;
        historical = HISTORICAL_EXISTING;
        return this;
    }

    public HistoricalBaseValueDSLFactory fromClosed(IBaseValue from) {
        this.from = from;
        historical = HISTORICAL_CLOSED;
        return this;
    }

    public HistoricalBaseValueDSLFactory fromPrevious(IBaseValue from) {
        this.from = from;
        historical = HISTORICAL_PREVIOUS;
        return this;
    }

    public HistoricalBaseValueDSLFactory fromNext(IBaseValue from) {
        this.from = from;
        historical = HISTORICAL_NEXT;
        return this;
    }

    public HistoricalBaseValueDSLFactory fromLast(IBaseValue from) {
        this.from = from;
        historical = HISTORICAL_LAST;
        return this;
    }

    public HistoricalBaseValueDSLFactory closed(Boolean closed) {
        this.closed = closed;
        return this;
    }

    public HistoricalBaseValueDSLFactory last(Boolean last) {
        this.last = last;
        return this;
    }

    public HistoricalBaseValueDSLFactory parent(Boolean parent) {
        this.parent = parent;
        return this;
    }

    public HistoricalBaseValueDSLFactory attribute(Boolean attribute) {
        this.attribute = attribute;
        return this;
    }

    public HistoricalBaseValueDSLFactory inserted() {
        this.persistence = PERSISTANCE_INSERTED;
        return this;
    }

    public HistoricalBaseValueDSLFactory updated() {
        this.persistence = PERSISTANCE_UPDATED;
        return this;
    }

    public HistoricalBaseValueDSLFactory deleted() {
        this.persistence = PERSISTANCE_DELETED;
        return this;
    }

    public HistoricalBaseValueDSLFactory processed() {
        this.persistence = PERSISTANCE_PROCESSED;
        return this;
    }

    public HistoricalBaseValueDSLFactory create() {

        switch (historical) {
            case (HISTORICAL_EXISTING):
                value = memorizingTool.valueDao.getExistingBaseValue(from);
                break;
            case (HISTORICAL_CLOSED):
                value = memorizingTool.valueDao.getClosedBaseValue(from);
                break;
            case (HISTORICAL_PREVIOUS):
                value = memorizingTool.valueDao.getPreviousBaseValue(from);
                break;
            case (HISTORICAL_NEXT):
                value = memorizingTool.valueDao.getNextBaseValue(from);
                break;
            case (HISTORICAL_LAST):
                value = memorizingTool.valueDao.getLastBaseValue(from);
                break;
        }

        if (value == null) return this;

        if (closed != null) value.setClosed(closed);
        if (last != null) value.setLast(last);

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

}



