package kz.bsbnb.usci.eav.model.base.impl.value;

import kz.bsbnb.usci.eav.model.base.IBaseValue;
import kz.bsbnb.usci.eav.model.base.impl.BaseValue;

import java.util.Date;

public class BaseEntityDoubleValue extends BaseValue<Double> implements IBaseValue<Double> {
    public BaseEntityDoubleValue(long id, long creditorId, Date reportDate, Double value,
                                 boolean closed, boolean last) {
        super(id, creditorId, reportDate, value, closed, last);
    }
}
