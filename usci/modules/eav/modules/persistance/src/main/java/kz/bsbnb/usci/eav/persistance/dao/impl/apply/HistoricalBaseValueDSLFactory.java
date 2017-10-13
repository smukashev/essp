package kz.bsbnb.usci.eav.persistance.dao.impl.apply;

import kz.bsbnb.usci.eav.model.base.IBaseEntity;
import kz.bsbnb.usci.eav.model.base.IBaseSet;
import kz.bsbnb.usci.eav.model.base.IBaseValue;
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
    private final int HISTORICAL_BASE = 6;

    private int historical = 0;
    private Boolean with = false;
    private IBaseValue withPersistable;
    private Class withPersistableDaoClass;

    public HistoricalBaseValueDSLFactory(ApplyHistoryFactory memorizingTool) {
        super(memorizingTool);
        this.parent = false;
        this.attribute = false;
    }

    public HistoricalBaseValueDSLFactory fromBase() {
        this.result = null;
        historical = HISTORICAL_BASE;
        return this;
    }

    public HistoricalBaseValueDSLFactory fromBase(IBaseValue from) {
        this.result = from;
        historical = HISTORICAL_BASE;
        return this;
    }

    public HistoricalBaseValueDSLFactory fromBase(IBaseSet from) {
        this.result = from;
        historical = HISTORICAL_BASE;
        return this;
    }

    public HistoricalBaseValueDSLFactory fromBase(IBaseEntity from) {
        this.result = from;
        historical = HISTORICAL_BASE;
        return this;
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
            valueDao = (IBaseValueDao) memorizingTool.persistableDaoPool
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
            case (HISTORICAL_BASE):
                break;
        }

        if (result == null) return this;

        if (result instanceof IBaseValue) {
            IBaseValue rs = (IBaseValue) result;
            if (id != null) rs.setId(id);
            if (date != null) rs.setRepDate(new Date(date.getTime()));
            if (value != null) rs.setValue(value);
            if (closed != null) rs.setClosed(closed);
            if (last != null) rs.setLast(last);
        } else if (result instanceof IBaseEntity) {
            IBaseEntity rs = (IBaseEntity) result;
            if (id != null) rs.setId(id);
        } else if (result instanceof IBaseSet) {
            IBaseSet rs = (IBaseSet) result;
            if (id != null) rs.setId(id);
            if (last != null) rs.setLast(last);
        }

        super.execute();

        return this;

    }

    public IBaseValue result() {
        if (result == null) this.execute();
        if (result instanceof IBaseValue)
            return (IBaseValue) result;
        return null;
    }

}



