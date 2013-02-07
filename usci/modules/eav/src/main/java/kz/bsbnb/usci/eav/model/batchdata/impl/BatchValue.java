package kz.bsbnb.usci.eav.model.batchdata.impl;

import kz.bsbnb.usci.eav.model.Batch;
import kz.bsbnb.usci.eav.model.batchdata.IBatchValue;

/**
 * @author a.motov
 */
public class BatchValue implements IBatchValue {

    /**
     * Contains information about the origin of this value.
     */
    private Batch batch;

    /**
     * The value can be a simple type, an array or a complex type.
     */
    private Object value;

    /**
     * Initializes batch value with a batch information and value.
     * @param batch information about the origin of this value.
     * @param value the value. May be is null.
     */
    public BatchValue(Batch batch, Object value) {
        if (batch == null) {
            throw new IllegalArgumentException("Batch is null. Initialization of the data values ​​is not possible.");
        }
        this.batch = batch;
        this.value = value;
    }

    @Override
    public Batch getBatch() {
        return batch;
    }

    @Override
    public Object getValue() {
        return value;
    }

}
