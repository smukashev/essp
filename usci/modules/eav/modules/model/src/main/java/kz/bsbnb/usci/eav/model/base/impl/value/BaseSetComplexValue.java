package kz.bsbnb.usci.eav.model.base.impl.value;

import kz.bsbnb.usci.eav.model.Batch;
import kz.bsbnb.usci.eav.model.base.IBaseEntity;
import kz.bsbnb.usci.eav.model.base.IBaseValue;
import kz.bsbnb.usci.eav.model.base.impl.BaseValue;

import java.util.Date;

public class BaseSetComplexValue extends BaseSetValue<IBaseEntity> {
    public BaseSetComplexValue(long id, long creditorId, Batch batch, long index, Date reportDate, IBaseEntity value,
                               boolean closed, boolean last) {
        super(id, creditorId, batch, index, reportDate, value, closed, last);
    }

    public BaseSetComplexValue(long creditorId, Batch batch, long index, IBaseEntity value) {
        super(creditorId, batch, index, value);
    }

}
