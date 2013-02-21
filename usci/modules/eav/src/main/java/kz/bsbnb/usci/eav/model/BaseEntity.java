package kz.bsbnb.usci.eav.model;

import java.util.*;

import kz.bsbnb.usci.eav.model.batchdata.IBaseValue;
import kz.bsbnb.usci.eav.model.metadata.DataTypes;
import kz.bsbnb.usci.eav.model.metadata.type.IMetaType;
import kz.bsbnb.usci.eav.model.metadata.type.impl.MetaSet;
import kz.bsbnb.usci.eav.model.metadata.type.impl.MetaClass;
import kz.bsbnb.usci.eav.model.metadata.type.impl.MetaValue;
import kz.bsbnb.usci.eav.persistance.Persistable;
import kz.bsbnb.usci.eav.util.SetUtils;

/**
 * Implements EAV entity object. 
 *
 * @version 1.0, 17.01.2013
 * @author a.tkachenko
 * @see MetaClass
 * @see DataTypes
 */
public class BaseEntity extends Persistable implements IBaseContainer
{
    /**
     * Holds data about entity structure
     * @see MetaClass
     */
    private MetaClass meta;
    
    /**
     * Holds attributes values
     */
    private HashMap<String, IBaseValue> values =
            new HashMap<String, IBaseValue>();

    /**
     * Initializes entity.
     */
    public BaseEntity()
    {

    }

    /**
     * Initializes entity with a class name.
     *
     * @param meta MetaClass of the entity..
     */
    public BaseEntity(MetaClass meta)
    {
        this.meta = meta;
    }

    /**
     * Used to retrieve object structure description. Can be used to modify metadata.
     * 
     * @return Object structure
     */
    public MetaClass getMeta()
    {
        return meta;
    }

    /**
     * Retrieves attribute titled <code>name</code>. Attribute must have type of <code>DataTypes.DATE</code>
     *
     * @param name attribute name. Must exist in entity metadata
     * @return attribute value, null if value is not set
     * @throws IllegalArgumentException if attribute name does not exist in entity metadata,
     * 	                                or attribute has type different from <code>DataTypes.DATE</code>
     * @see DataTypes
     */
    public IBaseValue getBaseValue(String name)
    {
        IMetaType type = meta.getMemberType(name);

        if(type == null)
            throw new IllegalArgumentException("Type: " + name +
                    ", not found in class: " + meta.getClassName());

        IBaseValue batchValue = values.get(name);

        if(batchValue == null)
            return null;

        return batchValue;
    }

    /**
     * Retrieves attribute titled <code>name</code>.
     *
     * @param name name attribute name. Must exist in entity metadata
     * @param value new value of the attribute
     * @throws IllegalArgumentException if attribute name does not exist in entity metadata,
     * 	                                or attribute has type different from <code>DataTypes.DATE</code>
     * @see DataTypes
     */
    @Override
    public void put(String name, IBaseValue value)
    {
        IMetaType type = meta.getMemberType(name);

        if(type == null)
            throw new IllegalArgumentException("Type: " + name +
                    ", not found in class: " + meta.getClassName());

        if (value == null)
            throw new IllegalArgumentException("Value not be equal to null.");

        if (value.getValue() != null)
        {
            Class<?> valueClass = value.getValue().getClass();
            Class<?> expValueClass = null;

            if (type.isComplex())
                if(type.isArray())
                {
                    expValueClass = BaseSet.class;
                }
                else
                {
                    expValueClass = BaseEntity.class;
                }
            else
            {
                if(type.isArray())
                {
                    MetaSet metaValue = (MetaSet)type;
                    expValueClass = metaValue.getTypeCode().getDataTypeClass();

                    valueClass = ((MetaValue)(((BaseSet)value.getValue()).getMemberType())).getTypeCode().
                            getDataTypeClass();
                }
                else
                {
                    MetaValue metaValue = (MetaValue)type;
                    expValueClass = metaValue.getTypeCode().getDataTypeClass();
                }

            }

            if(expValueClass == null || !expValueClass.isAssignableFrom(valueClass))
                throw new IllegalArgumentException("Type mismatch in class: " +
                        meta.getClassName() + ". Needed " + expValueClass + ", got: " +
                        valueClass);
        }

        values.put(name, value);
    }

    //arrays

    public <T> void addToArray(String name, IBaseValue value)
    {
        IMetaType type = meta.getMemberType(name);

        if(type == null)
            throw new IllegalArgumentException("Type: " + name +
                    ", not found in class: " + meta.getClassName());

        if(!type.isArray())
            throw new IllegalArgumentException("Type: " + name +
                    ", is not an array");

        if (value == null)
            throw new IllegalArgumentException("Element of the array can not be equal to null.");

        if (value.getValue() == null)
            throw new IllegalArgumentException("Element of the array can not be equal to null.");

        Class<?> valueClass = value.getClass();
        Class<?> expValueClass;

        if (type.isComplex())
            expValueClass = BaseEntity.class;
        else
        {
            if (type.isArray())
            {
                MetaSet metaClassArray = (MetaSet)type;
                expValueClass = metaClassArray.getTypeCode().getDataTypeClass();
            }
            else
            {
                MetaValue metaValue = (MetaValue)type;
                expValueClass = metaValue.getTypeCode().getDataTypeClass();
            }
        }

        if(expValueClass == null || !expValueClass.isAssignableFrom(valueClass))
            throw new IllegalArgumentException("Type mismatch in class: " +
                    meta.getClassName() + ". Needed " + expValueClass + ", got: " +
                    valueClass);

        ((Set<IBaseValue>)values.get(name).getValue()).add(value);
    }

    public Set<String> getPresentSimpleAttributeNames(DataTypes dataType)
    {
        return SetUtils.intersection(meta.getSimpleAttributesNames(dataType), values.keySet());
    }

    public Set<String> getPresentDateAttributeNames()
    {
        return getPresentSimpleAttributeNames(DataTypes.DATE);
    }

    public Set<String> getPresentDoubleAttributeNames()
    {
        return getPresentSimpleAttributeNames(DataTypes.DOUBLE);
    }

    public Set<String> getPresentIntegerAttributeNames()
    {
        return getPresentSimpleAttributeNames(DataTypes.INTEGER);
    }

    public Set<String> getPresentBooleanAttributeNames()
    {
        return getPresentSimpleAttributeNames(DataTypes.BOOLEAN);
    }

    public Set<String> getPresentStringAttributeNames()
    {
        return getPresentSimpleAttributeNames(DataTypes.STRING);
    }

    public Set<String> getPresentComplexAttributeNames() {
        return SetUtils.intersection(meta.getComplexAttributesNames(), values.keySet());
    }

    public Set<String> getPresentSimpleArrayAttributeNames(DataTypes dataType) {
        return SetUtils.intersection(meta.getSimpleArrayAttributesNames(dataType), values.keySet());
    }

    public Set<String> getPresentComplexArrayAttributeNames() {
        return SetUtils.intersection(meta.getComplexArrayAttributesNames(), values.keySet());
    }


    @Override
    public String toString()
    {
        return meta.getClassName();
    }

    public Set<String> getAttributeNames() {
        Set<String> attributeNames = values.keySet();
        return attributeNames;
    }

    public int getAttributeCount() {
        int valuesCount = values.size();

        return valuesCount;
    }

    @Override
    public boolean equals(Object obj) {
       if (obj == this)
            return true;

        if (obj == null)
            return false;

        if (!(getClass() == obj.getClass()))
            return false;
        else
        {
            BaseEntity that = (BaseEntity) obj;

            int thisAttributeCount = this.getAttributeCount();
            int thatAttributeCount = this.getAttributeCount();

            if (thisAttributeCount != thatAttributeCount)
                return false;

            // todo: implement and complete

            Iterator<String> valuesIt = values.keySet().iterator();
            while (valuesIt.hasNext())
            {
                String attributeName = valuesIt.next();

                IBaseValue thisBaseValue = this.getBaseValue(attributeName);
                IBaseValue thatBaseValue = that.getBaseValue(attributeName);

                if (!thisBaseValue.equals(thatBaseValue))
                    return false;
            }

           /*Iterator<String> arraysIt = arrays.keySet().iterator();
            while (arraysIt.hasNext())
            {
                String attributeName = arraysIt.next();

                List<IBaseValue> thisBatchValues = this.getBatchValueArray(attributeName);
                List<IBaseValue> thatBatchValues = that.getBatchValueArray(attributeName);

                Iterator<IBaseValue> thisBatchValueIt = thisBatchValues.iterator();
                while (thisBatchValueIt.hasNext())
                {
                    IBaseValue thisBatchValue = thisBatchValueIt.next();

                    boolean find = false;
                    Iterator<IBaseValue> thatBatchValueIt = thatBatchValues.iterator();
                    while (thatBatchValueIt.hasNext())
                    {
                        IBaseValue thatBatchValue = thatBatchValueIt.next();
                        if (thatBatchValue.equals(thisBatchValue))
                        {
                            find = true;
                            break;
                        }
                    }

                    if (!find)
                    {
                        return false;
                    }
                }
            }*/

            return true;
        }
    }

    @Override
    public Set<IBaseValue> get() {
        return (Set<IBaseValue>) values.values();
    }

    @Override
    public IMetaType getMemberType(String name) {
        return meta.getMemberType(name);
    }
}
