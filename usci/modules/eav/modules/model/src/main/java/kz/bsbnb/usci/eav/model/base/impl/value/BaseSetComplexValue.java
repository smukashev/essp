package kz.bsbnb.usci.eav.model.base.impl.value;

import kz.bsbnb.usci.eav.model.base.IBaseEntity;

import java.util.Date;

public class BaseSetComplexValue extends BaseSetValue<IBaseEntity> {
    public BaseSetComplexValue(long id, long creditorId, Date reportDate, IBaseEntity value,
                               boolean closed, boolean last) {
        super(id, creditorId, reportDate, value, closed, last);
    }
}
