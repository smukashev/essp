package kz.bsbnb.usci.eav.model.base.impl.value;

import java.util.Date;

public class BaseSetIntegerValue extends BaseSetValue<Integer> {
    public BaseSetIntegerValue(long id, long creditorId, Date reportDate, Integer value,
                               boolean closed, boolean last) {
        super(id, creditorId, reportDate, value, closed, last);
    }
}
