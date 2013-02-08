package kz.bsbnb.usci.eav.model.batchdata;

import kz.bsbnb.usci.eav.model.Batch;

/**
 * @author a.motov
 */
public interface IGenericBatchValue<T> {

    /**
     * Returns the <code>Batch</code> that contains information about the origin of this value.
     * @return <code>Batch</code> that contains information about the origin of this value.
     */
    public Batch getBatch();

    /**
     * Returns the index of the value.
     * @return the index
     */
    public long getIndex();

    /**
     * Sets the index values.
     * @param index the index of value to set.
     */
    public void setIndex(long index);

    /**
     * Return the value. The value can be a simple type, an array or a complex type.
     * @return value the value. May be is null.
     *
     * @see kz.bsbnb.usci.eav.model.metadata.DataTypes
     * @see kz.bsbnb.usci.eav.model.BaseEntity
     */
    public T getValue();

    public void setValue(T value);

}
