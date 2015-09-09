package kz.bsbnb.usci.eav.model.base.impl.value;

import kz.bsbnb.usci.eav.model.base.IBaseValue;
import kz.bsbnb.usci.eav.model.base.impl.BaseValue;

import java.util.Date;

public class BaseEntityDateValue extends BaseValue<Date> implements IBaseValue<Date> {
    public BaseEntityDateValue(long id, long creditorId, Date reportDate, Date value,
                               boolean closed, boolean last) {
        super(id, creditorId, reportDate, value, closed, last);
    }
}
