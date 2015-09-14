package kz.bsbnb.usci.eav.model.base.impl.value;

import java.util.Date;

public class BaseSetDateValue extends BaseSetValue<Date> {
    public BaseSetDateValue(long id, long creditorId, Date reportDate, Date value,
                            boolean closed, boolean last) {
        super(id, creditorId, reportDate, value, closed, last);
    }
}
