package kz.bsbnb.usci.eav.model.base.impl.value;

import kz.bsbnb.usci.eav.model.Batch;
import kz.bsbnb.usci.eav.model.base.IBaseValue;
import kz.bsbnb.usci.eav.model.base.impl.BaseValue;

import java.util.Date;

public class BaseSetDoubleValue extends BaseSetValue<Double> {
    public BaseSetDoubleValue(long id, long creditorId, Batch batch, long index, Date reportDate, Double value, boolean closed, boolean last) {
        super(id, creditorId, batch, index, reportDate, value, closed, last);
    }

}
