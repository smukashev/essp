package kz.bsbnb.usci.eav.model.batchdata.impl;

import kz.bsbnb.usci.eav.model.Batch;
import kz.bsbnb.usci.eav.model.batchdata.IGenericBatchValue;

/**
 * @author a.motov
 */
public class GenericBatchValue<T>
        implements IGenericBatchValue<T> {

    private long index;

    /**
     * Contains information about the origin of this value.
     */
    private Batch batch;

    /**
     * The value can be a simple type, an array or a complex type.
     */
    private T value;

    /**
     * Initializes batch value with a batch information, index and value.
     * @param batch information about the origin of this value.
     * @param index the index of the value
     * @param value the value. May be is null.
     * @throws IllegalArgumentException if <code>Batch</code> is null or <code>Batch</code> has no id
     */
    public GenericBatchValue(Batch batch, long index, T value) {
        if (batch == null) {
            throw new IllegalArgumentException("Batch is null. Initialization of the GenericBatchValue ​​is not possible.");
        }
        if (batch.getId() < 0) {
            throw new IllegalArgumentException("Batch has no id. Initialization of the GenericBatchValue ​​is not possible.");
        }

        this.batch = batch;
        this.value = value;
    }

    @Override
    public Batch getBatch() {
        return batch;
    }

    @Override
    public long getIndex() {
        return index;
    }

    @Override
    public void setIndex(long index) {
        this.index = index;
    }

    @Override
    public T getValue() {
        return value;
    }

    @Override
    public void setValue(T value) {
        this.value = value;
    }

}
