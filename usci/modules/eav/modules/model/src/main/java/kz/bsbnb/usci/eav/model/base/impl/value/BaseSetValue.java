package kz.bsbnb.usci.eav.model.base.impl.value;

import kz.bsbnb.usci.eav.model.base.IBaseSetValue;
import kz.bsbnb.usci.eav.model.base.IBaseValue;
import kz.bsbnb.usci.eav.model.base.impl.BaseValue;

import java.util.Date;

public class BaseSetValue<T> extends BaseValue<T> implements IBaseValue<T>, IBaseSetValue<T> {
    public BaseSetValue(long id, long creditorId, Date reportDate, T value, boolean closed, boolean last) {
        super(id, creditorId, reportDate, value, closed, last);
    }
}
