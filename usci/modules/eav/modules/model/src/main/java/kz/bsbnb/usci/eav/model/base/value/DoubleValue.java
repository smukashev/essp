package kz.bsbnb.usci.eav.model.base.value;

import kz.bsbnb.usci.eav.model.base.IBaseValue;
import kz.bsbnb.usci.eav.model.base.impl.BaseValue;

import java.util.Date;

public class DoubleValue extends BaseValue<Boolean> implements IBaseValue<Boolean> {

    public DoubleValue(long id, Date reportDate, Boolean value, boolean closed, boolean last) {
        super(id, reportDate, value, closed, last);
    }
}
