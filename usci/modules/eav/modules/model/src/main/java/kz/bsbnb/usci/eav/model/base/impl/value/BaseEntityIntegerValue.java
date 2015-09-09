package kz.bsbnb.usci.eav.model.base.impl.value;

import kz.bsbnb.usci.eav.model.base.IBaseValue;
import kz.bsbnb.usci.eav.model.base.impl.BaseValue;

import java.util.Date;

public class BaseEntityIntegerValue extends BaseValue<Integer> implements IBaseValue<Integer> {
    public BaseEntityIntegerValue(long id, long creditorId, Date reportDate, Integer value,
                                  boolean closed, boolean last) {
        super(id, creditorId, reportDate, value, closed, last);
    }
}
