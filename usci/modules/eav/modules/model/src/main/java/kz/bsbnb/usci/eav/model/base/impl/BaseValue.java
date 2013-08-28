package kz.bsbnb.usci.eav.model.base.impl;


import kz.bsbnb.usci.eav.model.Batch;
import kz.bsbnb.usci.eav.model.base.IBaseEntity;
import kz.bsbnb.usci.eav.model.base.IBaseValue;
import kz.bsbnb.usci.eav.model.persistable.impl.Persistable;
import kz.bsbnb.usci.eav.model.type.DataTypes;
import kz.bsbnb.usci.eav.util.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Date;
import java.util.UUID;

/**
 * Attributes value place holder for BaseEntity. Contains information about batch and record number of the value's
 * origin and reference to the actual value as an instance of Object.
 *
 * @see kz.bsbnb.usci.eav.model.base.impl.BaseEntity
 *
 * @author a.motov
 */
public class BaseValue extends Persistable implements IBaseValue
{
    Logger logger = LoggerFactory.getLogger(BaseValue.class);

    private UUID uuid = UUID.randomUUID();

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

    private Date reportDate;

    private boolean last = false;

    private boolean closed = false;

    /**
     * Initializes batch value with a batch information, index and value.
     * @param batch information about the origin of this value.
     * @param index the index of the value
     * @param value the value. May be is null.
     * @throws IllegalArgumentException if <code>Batch</code> is null or <code>Batch</code> has no id
     */
    public BaseValue(Batch batch, long index, Date reportDate, Object value)
    {
        if (reportDate == null)
            throw new IllegalArgumentException
                    ("reportDate is null. Initialization of the BaseValue ​​is not possible.");

        if (batch == null)
            throw new IllegalArgumentException
                    ("Batch is null. Initialization of the BaseValue ​​is not possible.");

        if (batch.getId() < 1)
            throw new IllegalArgumentException
                    ("Batch has no id. Initialization of the BaseValue ​​is not possible.");


        this.batch = batch;
        this.index = index;
        this.value = value;
        this.reportDate = new Date(DateUtils.cutOffTime(reportDate));
    }

    public BaseValue(Batch batch, long index, Date reportDate, Object value, boolean closed, boolean last)
    {
        this(batch, index, reportDate, value);
        this.closed = closed;
        this.last = last;
    }

    public BaseValue(long id, Batch batch, long index, Date reportDate, Object value)
    {
        super(id);

        if (batch == null)
            throw new IllegalArgumentException
                    ("Batch is null. Initialization of the BaseValue ​​is not possible.");

        if (batch.getId() < 1)
            throw new IllegalArgumentException
                    ("Batch has no id. Initialization of the BaseValue ​​is not possible.");

        this.batch = batch;
        this.index = index;
        this.value = value;
        this.reportDate = new Date(DateUtils.cutOffTime(reportDate));
    }

    public BaseValue(long id, Batch batch, long index, Date reportDate, Object value, boolean closed, boolean last)
    {
        this(id, batch, index, reportDate, value);
        this.closed = closed;
        this.last = last;

    }

    public BaseValue(Batch batch, long index, Object value)
    {
        if (batch == null)
            throw new IllegalArgumentException
                    ("Batch is null. Initialization of the BaseValue ​​is not possible.");

        if (batch.getId() < 1)
            throw new IllegalArgumentException
                    ("Batch has no id. Initialization of the BaseValue ​​is not possible.");


        this.batch = batch;
        this.index = index;
        this.value = value;
        this.reportDate = batch.getRepDate();
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

    public UUID getUuid() {
        return uuid;
    }

    @Override
    public void setLast(boolean last)
    {
        this.last = last;
    }

    @Override
    public boolean isLast()
    {
        return last;
    }

    @Override
    public void setClosed(boolean closed)
    {
        this.closed = closed;
    }

    @Override
    public boolean isClosed()
    {
        return closed;
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

            boolean res = index == that.index && batch.equals(that.batch) &&
                    !(value != null ? !value.equals(that.value) : that.value != null) &&
                    reportDate.equals(that.reportDate);

            logger.debug("Values: " + this.value + ", " + that.value);
            logger.debug("BaseValue Equals main expression: " + res);
            logger.debug("index == that.index: " + (index == that.index));
            logger.debug("batch.equals(that.batch): " + (batch.equals(that.batch)));
            logger.debug("!(value != null ? !value.equals(that.value) : that.value != null): " +
                    !(value != null ? !value.equals(that.value) : that.value != null));
            logger.debug("reportDate.equals(that.reportDate): " + (reportDate.equals(that.reportDate)));

            return res;

        }
    }

    public Date getRepDate()
    {
        return reportDate;
    }

    public void setRepDate(Date repDate)
    {
        this.reportDate = repDate;
    }

    @Override
    public int hashCode()
    {
        int result = super.hashCode();
        result = 31 * result + (int) (index ^ (index >>> 32));
        result = 31 * result + batch.hashCode();
        result = 31 * result + (value != null ? value.hashCode() : 0);
        result = 31 * result + reportDate.hashCode();
        return result;
    }

    public boolean equalsToString(String str, DataTypes type)
    {
        switch (type)
        {
            case INTEGER:
                if (value.equals(Integer.parseInt(str)))
                    return true;
                break;
            case DATE:
                //TODO: add date format
                break;
            case STRING:
                if (value.equals(str))
                    return true;
                break;
            case BOOLEAN:
                if (value.equals(Boolean.parseBoolean(str)))
                    return true;
                break;
            case DOUBLE:
                if (value.equals(Double.parseDouble(str)))
                    return true;
                break;
            default:
                throw new IllegalStateException("Unknown DataType: " + type);
        }

        return false;
    }

    @Override
    public BaseValue clone()
    {
        BaseValue baseValue = null;
        try
        {
            baseValue = (BaseValue)super.clone();
            baseValue.setRepDate((Date)reportDate.clone());

            if (value != null)
            {
                if (value instanceof BaseEntity)
                {
                    baseValue.setValue(((BaseEntity)value).clone());
                }
                if (value instanceof java.util.Date)
                {
                    baseValue.setValue(((java.util.Date)value).clone());
                }
            }
        }
        catch(CloneNotSupportedException ex)
        {
            throw new RuntimeException("BaseValue class does not implement interface Cloneable.");
        }
        return baseValue;
    }
}
