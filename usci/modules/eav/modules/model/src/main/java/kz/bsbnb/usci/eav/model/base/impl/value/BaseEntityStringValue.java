package kz.bsbnb.usci.eav.model.base.impl.value;

import kz.bsbnb.usci.eav.model.base.IBaseValue;
import kz.bsbnb.usci.eav.model.base.impl.BaseValue;

import java.util.Date;

public class BaseEntityStringValue extends BaseValue<String> implements IBaseValue<String> {
    public BaseEntityStringValue(long id, long creditorId, Date reportDate, String value,
                                 boolean closed, boolean last) {
        super(id, creditorId, reportDate, value, closed, last);
    }
}
