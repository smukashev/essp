package kz.bsbnb.usci.eav.model.base.impl.value;

import kz.bsbnb.usci.eav.model.Batch;
import kz.bsbnb.usci.eav.model.base.IBaseValue;
import kz.bsbnb.usci.eav.model.base.impl.BaseValue;

import java.util.Date;

/**
 * @author alexandr.motov
 */
public class BaseEntityDoubleValue extends BaseValue<Double> implements IBaseValue<Double> {

    public BaseEntityDoubleValue(long id, Batch batch, long index, Date reportDate, Double value, boolean closed, boolean last) {
        super(id, batch, index, reportDate, value, closed, last);
    }

}
