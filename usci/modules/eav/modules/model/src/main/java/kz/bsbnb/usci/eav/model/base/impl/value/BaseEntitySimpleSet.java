package kz.bsbnb.usci.eav.model.base.impl.value;

import kz.bsbnb.usci.eav.model.Batch;
import kz.bsbnb.usci.eav.model.base.IBaseSet;
import kz.bsbnb.usci.eav.model.base.IBaseValue;
import kz.bsbnb.usci.eav.model.base.impl.BaseValue;

import java.util.Date;

/**
 * @author alexandr.motov
 */
public class BaseEntitySimpleSet extends BaseValue<IBaseSet> implements IBaseValue<IBaseSet> {

    public BaseEntitySimpleSet(long id, Batch batch, long index, Date reportDate, IBaseSet value, boolean closed, boolean last) {
        super(id, batch, index, reportDate, value, closed, last);
    }

    public BaseEntitySimpleSet(Batch batch, long index, IBaseSet value) {
        super(batch, index, value);
    }

}
