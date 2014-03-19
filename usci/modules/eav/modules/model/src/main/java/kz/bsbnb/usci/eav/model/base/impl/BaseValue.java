package kz.bsbnb.usci.eav.model.base.impl;


import kz.bsbnb.usci.eav.model.Batch;
import kz.bsbnb.usci.eav.model.base.IBaseContainer;
import kz.bsbnb.usci.eav.model.base.IBaseEntity;
import kz.bsbnb.usci.eav.model.base.IBaseSet;
import kz.bsbnb.usci.eav.model.base.IBaseValue;
import kz.bsbnb.usci.eav.model.meta.IMetaAttribute;
import kz.bsbnb.usci.eav.model.meta.IMetaSet;
import kz.bsbnb.usci.eav.model.meta.IMetaType;
import kz.bsbnb.usci.eav.model.meta.IMetaValue;
import kz.bsbnb.usci.eav.model.persistable.impl.Persistable;
import kz.bsbnb.usci.eav.model.type.DataTypes;
import kz.bsbnb.usci.eav.util.DataUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Attributes value place holder for BaseEntity. Contains information about batch and record number of the value's
 * origin and reference to the actual value as an instance of Object.
 *
 * @see kz.bsbnb.usci.eav.model.base.impl.BaseEntity
 *
 * @author a.motov
 */
public class BaseValue<T> extends Persistable implements IBaseValue<T>
{

    public static final boolean DEFAULT_LAST = true;

    public static final boolean DEFAULT_CLOSED = false;

    Logger logger = LoggerFactory.getLogger(BaseValue.class);

    private UUID uuid = UUID.randomUUID();

    private IBaseContainer baseContainer;

    private IMetaAttribute metaAttribute;

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
    private T value;

    private Date reportDate;

    private boolean last = DEFAULT_LAST;

    private boolean closed = DEFAULT_CLOSED;

    public BaseValue(Batch batch, long index, Date reportDate, T value)
    {
        this(DEFAULT_ID, batch, index, reportDate, value, DEFAULT_CLOSED, DEFAULT_LAST);
    }

    public BaseValue(Batch batch, long index, Date reportDate, T value, boolean closed, boolean last)
    {
        this(DEFAULT_ID, batch, index, reportDate, value, closed, last);
    }

    public BaseValue(long id, Batch batch, long index, Date reportDate, T value)
    {
        this(id, batch, index, reportDate, value, DEFAULT_CLOSED, DEFAULT_LAST);
    }

    public BaseValue(Batch batch, long index, T value)
    {
        this(DEFAULT_ID, batch, index, batch.getRepDate(), value, DEFAULT_CLOSED, DEFAULT_LAST);
    }

    public BaseValue(long id, Batch batch, long index, Date reportDate, T value, boolean closed, boolean last)
    {
        super(id);

        if (batch == null)
            throw new IllegalArgumentException
                    ("Batch is null. Initialization of the BaseValue ​​is not possible.");

        if (batch.getId() < 1)
            throw new IllegalArgumentException
                    ("Batch has no id. Initialization of the BaseValue ​​is not possible.");

        if (reportDate == null)
            throw new IllegalArgumentException
                    ("reportDate is null. Initialization of the BaseValue ​​is not possible.");

        Date newReportDate = (Date)reportDate.clone();
        DataUtils.toBeginningOfTheDay(newReportDate);

        this.batch = batch;
        this.index = index;
        this.value = (T)value;
        this.reportDate = newReportDate;
        this.closed = closed;
        this.last = last;
    }

    @Override
    public IBaseContainer getBaseContainer() {
        return baseContainer;
    }

    @Override
    public void setBaseContainer(IBaseContainer baseContainer) {
        this.baseContainer = baseContainer;
    }

    @Override
    public IMetaAttribute getMetaAttribute() {
        return metaAttribute;
    }

    @Override
    public void setMetaAttribute(IMetaAttribute metaAttribute) {
        this.metaAttribute = metaAttribute;
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
    public T getValue()
    {
        return value;
    }

    @Override
    public void setValue(T value)
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

    @Override
    public Date getRepDate()
    {
        return reportDate;
    }

    public void setRepDate(Date reportDate)
    {
        Date newReportDate = (Date)reportDate.clone();
        DataUtils.toBeginningOfTheDay(newReportDate);

        this.reportDate = newReportDate;
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

    public boolean equalsByValue(IBaseValue baseValue)
    {
        IMetaAttribute thisMetaAttribute = this.getMetaAttribute();
        IMetaAttribute thatMetaAttribute = baseValue.getMetaAttribute();

        if (thisMetaAttribute == null || thatMetaAttribute == null)
        {
            throw new RuntimeException("Comparison values of two instances of BaseValue without meta data is not possible.");
        }

        Object thisValue = this.getValue();
        Object thatValue = baseValue.getValue();

        if (thisValue == null || thatValue == null)
        {
            throw new RuntimeException("Comparison values of two instances of BaseValue with null values is not possible.");
        }

        if (thisMetaAttribute.getId() == thatMetaAttribute.getId())
        {
            IMetaType metaType = thisMetaAttribute.getMetaType();
            if (metaType.isSetOfSets())
            {
                throw new UnsupportedOperationException("Not yet implemented");
            }

            if (metaType.isComplex())
            {
                if (metaType.isSet())
                {
                    IBaseSet thisBaseSet = (IBaseSet)thisValue;
                    IBaseSet thatBaseSet = (IBaseSet)thatValue;

                    List<Long> thisIds = new ArrayList<Long>();
                    for (IBaseValue thisChildBaseValue : thisBaseSet.get())
                    {
                        IBaseEntity thisBaseEntity = (IBaseEntity)thisChildBaseValue.getValue();
                        thisIds.add(thisBaseEntity.getId());
                    }

                    List<Long> thatIds = new ArrayList<Long>();
                    for (IBaseValue thatChildBaseValue : thatBaseSet.get())
                    {
                        BaseEntity thatBaseEntity = (BaseEntity)thatChildBaseValue.getValue();
                        thatIds.add(thatBaseEntity.getId());
                    }
                    Collections.sort(thatIds);
                    Collections.sort(thatIds);

                    return thisIds.equals(thatIds);
                }
                else
                {
                    IBaseEntity thisBaseEntity = (IBaseEntity)thisValue;
                    IBaseEntity thatBaseEntity = (IBaseEntity)thatValue;
                    return thisBaseEntity.getId() == thatBaseEntity.getId();
                }
            }
            else
            {
                if (metaType.isSet())
                {
                    IMetaSet metaSet = (IMetaSet)metaType;
                    IMetaValue metaValue = (IMetaValue)metaSet.getMemberType();

                    IBaseSet thisBaseSet = (IBaseSet)thisValue;
                    IBaseSet thatBaseSet = (IBaseSet)thatValue;

                    List<Object> thisIds = new ArrayList<Object>();
                    for (IBaseValue thisChildBaseValue : thisBaseSet.get())
                    {
                        thisIds.add(metaValue.getTypeCode() == DataTypes.DATE ?
                                DataUtils.toBeginningOfTheDay(new Date(((Date)thisChildBaseValue).getTime())) :
                                thisChildBaseValue.getValue());
                    }

                    List<Long> thatIds = new ArrayList<Long>();
                    for (IBaseValue thatChildBaseValue : thatBaseSet.get())
                    {
                        thisIds.add(metaValue.getTypeCode() == DataTypes.DATE ?
                                DataUtils.toBeginningOfTheDay(new Date(((Date)thatChildBaseValue).getTime())) :
                                thatChildBaseValue.getValue());
                    }
                    Collections.sort(thatIds);
                    Collections.sort(thatIds);

                    return thisIds.equals(thatIds);
                }
                else
                {
                    IMetaValue metaValue = (IMetaValue)metaType;
                    return metaValue.getTypeCode() == DataTypes.DATE ?
                            DataUtils.compareBeginningOfTheDay((Date)thisValue, (Date)thatValue) == 0 :
                            thisValue.equals(thatValue);
                }
            }
        }

        return false;
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
                if (value instanceof BaseSet)
                {
                    baseValue.setValue(((BaseSet)value).clone());
                }
                if (value instanceof Date)
                {
                    baseValue.setValue(((Date)value).clone());
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
