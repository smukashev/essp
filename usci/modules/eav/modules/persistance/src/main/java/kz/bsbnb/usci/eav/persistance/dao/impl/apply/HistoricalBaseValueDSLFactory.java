package kz.bsbnb.usci.eav.persistance.dao.impl.apply;

import kz.bsbnb.usci.eav.model.base.IBaseValue;

import java.sql.Date;

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
        this.parent = false;
        this.attribute = false;
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

    public HistoricalBaseValueDSLFactory execute() {

        switch (historical) {
            case (HISTORICAL_EXISTING):
                result = memorizingTool.valueDao.getExistingBaseValue(from);
                break;
            case (HISTORICAL_CLOSED):
                result = memorizingTool.valueDao.getClosedBaseValue(from);
                break;
            case (HISTORICAL_PREVIOUS):
                result = memorizingTool.valueDao.getPreviousBaseValue(from);
                break;
            case (HISTORICAL_NEXT):
                result = memorizingTool.valueDao.getNextBaseValue(from);
                break;
            case (HISTORICAL_LAST):
                result = memorizingTool.valueDao.getLastBaseValue(from);
                break;
        }

        if (result == null) return this;

        if (id != null) result.setId(id);
        if (date != null) result.setRepDate(new Date(date.getTime()));
        if (value != null) result.setValue(value);
        if (closed != null) result.setClosed(closed);
        if (last != null) result.setLast(last);

        super.execute();

        return this;

    }

    public IBaseValue result() {
        if (result == null) this.execute();
        return result;
    }

}



