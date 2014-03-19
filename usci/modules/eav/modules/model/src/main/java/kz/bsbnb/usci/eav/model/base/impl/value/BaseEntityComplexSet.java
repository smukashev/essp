package kz.bsbnb.usci.eav.model.base.impl.value;

import kz.bsbnb.usci.eav.model.Batch;
import kz.bsbnb.usci.eav.model.base.IBaseSet;
import kz.bsbnb.usci.eav.model.base.IBaseValue;
import kz.bsbnb.usci.eav.model.base.impl.BaseValue;

import java.util.Date;

/**
 * Created by Alexandr.Motov on 18.03.14.
 */
public class BaseEntityComplexSet extends BaseValue<IBaseSet> implements IBaseValue<IBaseSet> {

    public BaseEntityComplexSet(long id, Batch batch, long index, Date reportDate, IBaseSet value, boolean closed, boolean last) {
        super(id, batch, index, reportDate, value, closed, last);
    }

}
