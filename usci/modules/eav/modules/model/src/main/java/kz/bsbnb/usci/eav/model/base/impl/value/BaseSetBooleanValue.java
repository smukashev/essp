package kz.bsbnb.usci.eav.model.base.impl.value;

import kz.bsbnb.usci.eav.model.base.IBaseValue;
import kz.bsbnb.usci.eav.model.base.impl.BaseValue;

import java.util.Date;

public class BaseSetBooleanValue extends BaseValue<Boolean> implements IBaseValue<Boolean> {
    public BaseSetBooleanValue(long id, long creditorId, Date reportDate, Boolean value,
                               boolean closed, boolean last) {
        super(id, creditorId, reportDate, value, closed, last);
    }
}
