package kz.bsbnb.usci.eav.model.base.impl.value;

import kz.bsbnb.usci.eav.model.base.IBaseEntity;
import kz.bsbnb.usci.eav.model.persistable.IPersistable;

import java.util.Date;

public class BaseSetComplexValue extends BaseSetValue<IBaseEntity> implements IPersistable {
    private static final long serialVersionUID = 247372847;

    public BaseSetComplexValue(long id, long creditorId, Date reportDate, IBaseEntity value,
                               boolean closed, boolean last) {
        super(id, creditorId, reportDate, value, closed, last);
    }
}
