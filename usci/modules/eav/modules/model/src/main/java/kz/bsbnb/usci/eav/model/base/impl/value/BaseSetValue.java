package kz.bsbnb.usci.eav.model.base.impl.value;

import kz.bsbnb.usci.eav.model.Batch;
import kz.bsbnb.usci.eav.model.base.IBaseSetValue;
import kz.bsbnb.usci.eav.model.base.IBaseValue;
import kz.bsbnb.usci.eav.model.base.impl.BaseValue;

import java.util.Date;

/**
 * @author alexandr.motov
 */
public class BaseSetValue<T> extends BaseValue<T> implements IBaseValue<T>, IBaseSetValue<T> {

    private HistoryType historyType = HistoryType.RESTRICTED_BY_ENTITY;

    public BaseSetValue(long id, Batch batch, long index, Date reportDate, T value, boolean closed, boolean last) {
        super(id, batch, index, reportDate, value, closed, last);
    }

    public HistoryType getHistoryType() {
        return historyType;
    }

    public void setHistoryType(HistoryType historyType) {
        this.historyType = historyType;
    }



}
