package kz.bsbnb.usci.eav.model.batchdata.impl;

import kz.bsbnb.usci.eav.model.Batch;
import kz.bsbnb.usci.eav.model.batchdata.IBaseValue;
import kz.bsbnb.usci.eav.persistance.Persistable;

/**
 * Attributes value place holder for BaseEntity. Contains information about batch and record number of the value's
 * origin and reference to the actual value as an instance of Object.
 *
 * @see kz.bsbnb.usci.eav.model.BaseEntity
 *
 * @author a.motov
 */
public class BaseValue extends Persistable implements IBaseValue
{
    /**
     * Information about the sequential number of record in the batch
     */
    private long index;

    /**
     * Information about the origin of this value.
     */
    private Batch batch;

    /**
     * Can be a simple type, an array or a complex type.
     */
    private Object value;

    /**
     * Initializes batch value with a batch information, index and value.
     * @param batch information about the origin of this value.
     * @param index the index of the value
     * @param value the value. May be is null.
     * @throws IllegalArgumentException if <code>Batch</code> is null or <code>Batch</code> has no id
     */
    public BaseValue(Batch batch, long index, Object value)
    {
        if (batch == null)
            throw new IllegalArgumentException
                    ("Batch is null. Initialization of the BaseValue ​​is not possible.");

        if (batch.getId() < 0)
            throw new IllegalArgumentException
                    ("Batch has no id. Initialization of the BaseValue ​​is not possible.");


        this.batch = batch;
        this.index = index;
        this.value = value;
    }

    @Override
    public Batch getBatch()
    {
        return batch;
    }

    @Override
    public long getIndex()
    {
        return index;
    }

    @Override
    public void setIndex(long index)
    {
        this.index = index;
    }

    @Override
    public Object getValue()
    {
        return value;
    }

    @Override
    public void setValue(Object value)
    {
        this.value = value;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == this)
            return true;

        if (obj == null)
            return false;

        if (!(getClass() == obj.getClass()))
            return false;
        else
        {
            BaseValue that = (BaseValue)obj;

            return index == that.index && batch.equals(that.batch) && !(value != null ? !value.equals(that.value) : that.value != null);

        }
    }

}
