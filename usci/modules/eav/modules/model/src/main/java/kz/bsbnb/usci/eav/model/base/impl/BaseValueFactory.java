package kz.bsbnb.usci.eav.model.base.impl;

import kz.bsbnb.usci.eav.model.Batch;
import kz.bsbnb.usci.eav.model.base.IBaseEntity;
import kz.bsbnb.usci.eav.model.base.IBaseSet;
import kz.bsbnb.usci.eav.model.base.IBaseValue;
import kz.bsbnb.usci.eav.model.base.impl.value.*;
import kz.bsbnb.usci.eav.model.meta.IMetaType;
import kz.bsbnb.usci.eav.model.meta.IMetaValue;
import kz.bsbnb.usci.eav.model.meta.impl.MetaContainerTypes;

import java.util.Date;

/**
 * @author alexandr.motov
 */
public class BaseValueFactory {

    public static IBaseValue create(int metaContainerType, IMetaType metaType, Batch batch,
                                    long index, Date reportDate, Object value)
    {
        return create(metaContainerType, metaType, BaseValue.DEFAULT_ID, batch, index, reportDate,
                value, BaseValue.DEFAULT_CLOSED, BaseValue.DEFAULT_LAST);
    }

    public static IBaseValue create(int metaContainerType, IMetaType metaType, Batch batch,
                                    long index, Date reportDate, Object value,
                                    boolean closed, boolean last)
    {
        return create(metaContainerType, metaType, BaseValue.DEFAULT_ID, batch, index, reportDate,
                value, closed, last);
    }

    public static IBaseValue create(int metaContainerType, IMetaType metaType, long id,
                                    Batch batch, long index, Date reportDate, Object value)
    {
        return create(metaContainerType, metaType, id, batch, index, reportDate,
                value, BaseValue.DEFAULT_CLOSED, BaseValue.DEFAULT_LAST);
    }

    public static IBaseValue create(int metaContainerType, IMetaType metaType, Batch batch,
                                    long index, Object value)
    {
        return create(metaContainerType, metaType, BaseValue.DEFAULT_ID, batch, index, batch.getRepDate(),
                value, BaseValue.DEFAULT_CLOSED, BaseValue.DEFAULT_LAST);
    }

    public static IBaseValue create(BaseContainerType baseContainerType, IMetaType metaType, Batch batch,
                                    long index, Object value)
    {
        return create(baseContainerType, metaType, BaseValue.DEFAULT_ID, batch, index, batch.getRepDate(),
                value, BaseValue.DEFAULT_CLOSED, BaseValue.DEFAULT_LAST);
    }

    public static IBaseValue create(BaseContainerType baseContainerType, IMetaType metaType, long id, Batch batch,
                                    long index, Date reportDate, Object value, boolean closed, boolean last)
    {
        int metaContainerType = 0;
        switch(baseContainerType)
        {
            case BASE_ENTITY: metaContainerType = MetaContainerTypes.META_CLASS; break;
            case BASE_SET: metaContainerType = MetaContainerTypes.META_SET; break;
        }

        return create(metaContainerType, metaType, id, batch, index, reportDate, value, closed, last);
    }

    public static IBaseValue create(int metaContainerType, IMetaType metaType, long id, Batch batch,
                                    long index, Date reportDate, Object value, boolean closed, boolean last)
    {
        IBaseValue baseValue = null;
        switch(metaContainerType)
        {
            case MetaContainerTypes.META_CLASS:
            {
                if (metaType.isSet())
                {
                    IBaseSet baseSet = (IBaseSet)value;
                    if (metaType.isComplex())
                        baseValue = new BaseEntityComplexSet(id, batch, index, reportDate, baseSet, closed, last);
                    else
                        baseValue = new BaseEntitySimpleSet(id, batch, index, reportDate, baseSet, closed, last);
                }
                else
                {
                    if (metaType.isComplex())
                    {
                        IBaseEntity baseEntity = (IBaseEntity)value;
                        baseValue = new BaseEntityComplexValue(id, batch, index, reportDate, baseEntity, closed, last);
                    }
                    else
                    {
                        IMetaValue metaValue = (IMetaValue)metaType;
                        switch (metaValue.getTypeCode())
                        {
                            case INTEGER:
                            {
                                Integer integerValue = (Integer)value;
                                baseValue = new BaseEntityIntegerValue(id, batch, index, reportDate, integerValue, closed, last);
                                break;
                            }
                            case DATE:
                            {
                                Date dateValue = (Date)value;
                                baseValue = new BaseEntityDateValue(id, batch, index, reportDate, dateValue, closed, last);
                                break;
                            }
                            case STRING:
                            {
                                String stringValue = (String)value;
                                baseValue = new BaseEntityStringValue(id, batch, index, reportDate, stringValue, closed, last);
                                break;
                            }
                            case BOOLEAN:
                            {
                                Boolean booleanValue = (Boolean)value;
                                baseValue = new BaseEntityBooleanValue(id, batch, index, reportDate, booleanValue, closed, last);
                                break;
                            }
                            case DOUBLE:
                            {
                                Double doubleValue = (Double)value;
                                baseValue = new BaseEntityDoubleValue(id, batch, index, reportDate, doubleValue, closed, last);
                                break;
                            }
                            default:
                                throw new RuntimeException("Unknown data type.");
                        }
                    }
                }
                break;
            }
            case MetaContainerTypes.META_SET:
            {
                if (metaType.isSet())
                {
                    throw new UnsupportedOperationException("Не реализовано;");
                }
                else
                {
                    if (metaType.isComplex())
                    {
                        IBaseEntity baseEntity = (IBaseEntity)value;
                        baseValue = new BaseSetComplexValue(id, batch, index, reportDate, baseEntity, closed, last);
                    }
                    else
                    {
                        IMetaValue metaValue = (IMetaValue)metaType;
                        switch (metaValue.getTypeCode())
                        {
                            case INTEGER:
                            {
                                Integer integerValue = (Integer)value;
                                baseValue = new BaseSetIntegerValue(id, batch, index, reportDate, integerValue, closed, last);
                                break;
                            }
                            case DATE:
                            {
                                Date dateValue = (Date)value;
                                baseValue = new BaseSetDateValue(id, batch, index, reportDate, dateValue, closed, last);
                                break;
                            }
                            case STRING:
                            {
                                String stringValue = (String)value;
                                baseValue = new BaseSetStringValue(id, batch, index, reportDate, stringValue, closed, last);
                                break;
                            }
                            case BOOLEAN:
                            {
                                Boolean booleanValue = (Boolean)value;
                                baseValue = new BaseSetBooleanValue(id, batch, index, reportDate, booleanValue, closed, last);
                                break;
                            }
                            case DOUBLE:
                            {
                                Double doubleValue = (Double)value;
                                baseValue = new BaseSetDoubleValue(id, batch, index, reportDate, doubleValue, closed, last);
                                break;
                            }
                            default:
                                throw new RuntimeException("Unknown data type.");
                        }
                    }
                }
                break;
            }
        }

        if (baseValue == null)
        {
            throw new RuntimeException("Can not create instance of BaseValue.");
            //baseValue = new BaseValue(id, batch, index, reportDate, value, closed, last);
        }

        return baseValue;
    }

}
