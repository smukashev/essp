package kz.bsbnb.usci.eav.model.base.impl.value;

import kz.bsbnb.usci.eav.model.Batch;
import kz.bsbnb.usci.eav.model.base.IBaseSetValue;
import kz.bsbnb.usci.eav.model.base.IBaseValue;
import kz.bsbnb.usci.eav.model.base.impl.BaseValue;

import java.util.Date;

public class BaseSetValue<T> extends BaseValue<T> implements IBaseValue<T>, IBaseSetValue<T> {

    private HistoryType historyType = HistoryType.RESTRICTED_BY_ENTITY;

    public BaseSetValue(long id, long creditorId, Batch batch, long index, Date reportDate, T value,
                        boolean closed, boolean last) {
        super(id, creditorId, batch, index, reportDate, value, closed, last);
    }

    public BaseSetValue(long creditorId, Batch batch, long index, T value) {
        super(creditorId, batch, index, value);
    }

    public HistoryType getHistoryType() {
        return historyType;
    }

    public void setHistoryType(HistoryType historyType) {
        this.historyType = historyType;
    }
}
