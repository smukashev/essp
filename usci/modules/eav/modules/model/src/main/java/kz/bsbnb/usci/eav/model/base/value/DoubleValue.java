package kz.bsbnb.usci.eav.model.base.value;

import kz.bsbnb.usci.eav.model.Batch;
import kz.bsbnb.usci.eav.model.base.IBaseValue;
import kz.bsbnb.usci.eav.model.base.impl.BaseValue;

import java.util.Date;

/**
 * @author alexandr.motov
 */
public class DoubleValue extends BaseValue<Boolean> implements IBaseValue<Boolean> {

    public DoubleValue(long id, Batch batch, long index, Date reportDate, Boolean value, boolean closed, boolean last) {
        super(id, batch, index, reportDate, value, closed, last);
    }

}
