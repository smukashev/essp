package kz.bsbnb.usci.eav.model.base.impl.value;

import kz.bsbnb.usci.eav.model.base.IBaseEntity;
import kz.bsbnb.usci.eav.model.base.IBaseValue;
import kz.bsbnb.usci.eav.model.base.impl.BaseValue;

import java.util.Date;

public class BaseEntityComplexValue extends BaseValue<IBaseEntity> implements IBaseValue<IBaseEntity> {

    public BaseEntityComplexValue(long id, long creditorId, Date reportDate,
                                  IBaseEntity value, boolean closed, boolean last) {
        super(id, creditorId, reportDate, value, closed, last);
    }
}
