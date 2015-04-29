package kz.bsbnb.usci.eav.model.base.impl.value;

import kz.bsbnb.usci.eav.model.Batch;
import kz.bsbnb.usci.eav.model.base.IBaseEntity;
import kz.bsbnb.usci.eav.model.base.IBaseValue;
import kz.bsbnb.usci.eav.model.base.impl.BaseValue;

import java.util.Date;

/**
 * @author alexandr.motov
 */
public class BaseSetComplexValue extends BaseSetValue<IBaseEntity> {

    public BaseSetComplexValue(long id, Batch batch, long index, Date reportDate, IBaseEntity value, boolean closed, boolean last) {
        super(id, batch, index, reportDate, value, closed, last);
    }

    public BaseSetComplexValue(Batch batch, long index, IBaseEntity value) {
        super(batch, index, value);
    }

}
