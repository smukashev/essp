package kz.bsbnb.usci.eav.model.base.impl.value;

import kz.bsbnb.usci.eav.model.base.IBaseSet;
import kz.bsbnb.usci.eav.model.base.IBaseValue;
import kz.bsbnb.usci.eav.model.base.impl.BaseValue;

import java.util.Date;

public class BaseEntitySimpleSet extends BaseValue<IBaseSet> implements IBaseValue<IBaseSet> {
    public BaseEntitySimpleSet(long id, long creditorId, Date reportDate, IBaseSet value,
                               boolean closed, boolean last) {
        super(id, creditorId, reportDate, value, closed, last);
    }
}
