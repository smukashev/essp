package kz.bsbnb.usci.eav.model.base.impl.value;

import kz.bsbnb.usci.eav.model.Batch;
import kz.bsbnb.usci.eav.model.base.IBaseValue;
import kz.bsbnb.usci.eav.model.base.impl.BaseValue;

import java.util.Date;

public class BaseSetDateValue extends BaseSetValue<Date> {
    public BaseSetDateValue(long id, long creditorId, Batch batch, long index, Date reportDate, Date value,
                            boolean closed, boolean last) {
        super(id, creditorId, batch, index, reportDate, value, closed, last);
    }

}
