package kz.bsbnb.usci.eav.model.base.impl.value;

import kz.bsbnb.usci.eav.model.Batch;
import kz.bsbnb.usci.eav.model.base.IBaseValue;
import kz.bsbnb.usci.eav.model.base.impl.BaseValue;

import java.util.Date;

public class BaseSetStringValue extends BaseSetValue<String> {
    public BaseSetStringValue(long id, long creditorId, Batch batch, long index, Date reportDate, String value,
                              boolean closed, boolean last) {
        super(id, creditorId, batch, index, reportDate, value, closed, last);
    }

    public BaseSetStringValue(long creditorId, Batch batch, long index, String value) {
        super(creditorId, batch, index, value);
    }

}
