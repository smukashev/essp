package kz.bsbnb.usci.eav.persistance.dao.impl.apply;

import kz.bsbnb.usci.eav.model.base.IBaseValue;

/**
 * Created by emles on 23.08.17
 */
public class HistoricalBaseValueDSLFactory extends BaseValueDSLFactory {

    private final int HISTORICAL_EXISTING = 1;
    private final int HISTORICAL_CLOSED = 2;
    private final int HISTORICAL_PREVIOUS = 3;
    private final int HISTORICAL_NEXT = 4;
    private final int HISTORICAL_LAST = 5;

    private int historical = 0;

    public HistoricalBaseValueDSLFactory(ApplyHistoryFactory memorizingTool) {
        super(memorizingTool);
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

    public HistoricalBaseValueDSLFactory clone() throws CloneNotSupportedException {

        HistoricalBaseValueDSLFactory cloneValue = new HistoricalBaseValueDSLFactory(memorizingTool);

        cloneValue.from = this.from;
        cloneValue.historical = this.historical;

        cloneValue.closed = this.closed;
        cloneValue.last = this.last;

        cloneValue.parent = this.parent;
        cloneValue.attribute = this.attribute;
        cloneValue.persistence = this.persistence;

        return cloneValue;

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

        super.create();

        return this;

    }

    public IBaseValue value() {
        if (value == null) this.create();
        return value;
    }

}



