package kz.bsbnb.usci.eav.persistance.dao.impl.apply;

import kz.bsbnb.usci.eav.model.base.IBaseValue;
import kz.bsbnb.usci.eav.model.persistable.IPersistable;
import kz.bsbnb.usci.eav.persistance.dao.IBaseValueDao;
import kz.bsbnb.usci.eav.persistance.dao.pool.IPersistableDaoPool;
import org.springframework.beans.factory.annotation.Autowired;

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

    @Autowired
    private IPersistableDaoPool persistableDaoPool;

    private int historical = 0;
    private Boolean with = false;
    private IBaseValue withPersistable;
    private Class withPersistableDaoClass;

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

    public HistoricalBaseValueDSLFactory withValueDao(IBaseValue persistable, Class<?> persistableDaoClass) {
        this.with = true;
        this.withPersistable = persistable;
        this.withPersistableDaoClass = persistableDaoClass;
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

        IBaseValueDao valueDao = memorizingTool.valueDao;

        if (with)
            valueDao = (IBaseValueDao) persistableDaoPool
                    .getPersistableDao(withPersistable.getClass(), withPersistableDaoClass);

        switch (historical) {
            case (HISTORICAL_EXISTING):
                result = valueDao.getExistingBaseValue(from);
                break;
            case (HISTORICAL_CLOSED):
                result = valueDao.getClosedBaseValue(from);
                break;
            case (HISTORICAL_PREVIOUS):
                result = valueDao.getPreviousBaseValue(from);
                break;
            case (HISTORICAL_NEXT):
                result = valueDao.getNextBaseValue(from);
                break;
            case (HISTORICAL_LAST):
                result = valueDao.getLastBaseValue(from);
                break;
        }

        if (result == null) return this;

        IBaseValue rs = (IBaseValue) result;

        if (id != null) rs.setId(id);
        if (date != null) rs.setRepDate(new Date(date.getTime()));
        if (value != null) rs.setValue(value);
        if (closed != null) rs.setClosed(closed);
        if (last != null) rs.setLast(last);

        super.execute();

        return this;

    }

    public IBaseValue result() {
        if (result == null) this.execute();
        return (IBaseValue) result;
    }

}



