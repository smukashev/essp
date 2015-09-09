package kz.bsbnb.usci.eav.model.base.impl.value;

import java.util.Date;

public class BaseSetStringValue extends BaseSetValue<String> {
    public BaseSetStringValue(long id, long creditorId, Date reportDate, String value,
                              boolean closed, boolean last) {
        super(id, creditorId, reportDate, value, closed, last);
    }
}
