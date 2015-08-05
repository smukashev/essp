package kz.bsbnb.usci.eav.model.base.impl.value;

import kz.bsbnb.usci.eav.model.Batch;
import kz.bsbnb.usci.eav.model.base.IBaseValue;
import kz.bsbnb.usci.eav.model.base.impl.BaseValue;

import java.util.Date;

public class BaseEntityStringValue extends BaseValue<String> implements IBaseValue<String> {
    public BaseEntityStringValue(long id, long creditorId, Batch batch, long index, Date reportDate, String value,
                                 boolean closed, boolean last) {
        super(id, creditorId, batch, index, reportDate, value, closed, last);
    }

    public BaseEntityStringValue(long creditorId, Batch batch, long index, String value) {
        super(creditorId, batch, index, value);
    }

}
