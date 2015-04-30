package kz.bsbnb.usci.eav.model.base.impl.value;

import kz.bsbnb.usci.eav.model.Batch;
import kz.bsbnb.usci.eav.model.base.IBaseValue;
import kz.bsbnb.usci.eav.model.base.impl.BaseValue;

import java.util.Date;

/**
 * @author alexandr.motov
 */
public class BaseSetStringValue extends BaseSetValue<String> {

    public BaseSetStringValue(long id, Batch batch, long index, Date reportDate, String value, boolean closed, boolean last) {
        super(id, batch, index, reportDate, value, closed, last);
    }

    public BaseSetStringValue(Batch batch, long index, String value) {
        super(batch, index, value);
    }

}
