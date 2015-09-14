package kz.bsbnb.usci.eav.model.base.impl.value;

import java.util.Date;

public class BaseSetDoubleValue extends BaseSetValue<Double> {
    public BaseSetDoubleValue(long id, long creditorId, Date reportDate, Double value, boolean closed, boolean last) {
        super(id, creditorId, reportDate, value, closed, last);
    }
}
