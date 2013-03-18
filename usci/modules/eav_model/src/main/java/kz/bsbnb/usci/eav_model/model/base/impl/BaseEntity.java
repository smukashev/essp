package kz.bsbnb.usci.eav_model.model.base.impl;

import kz.bsbnb.usci.eav_model.model.base.IBaseContainer;
import kz.bsbnb.usci.eav_model.model.base.IBaseValue;
import kz.bsbnb.usci.eav_model.model.meta.IMetaType;
import kz.bsbnb.usci.eav_model.model.meta.impl.MetaClass;
import kz.bsbnb.usci.eav_model.model.meta.impl.MetaSet;
import kz.bsbnb.usci.eav_model.model.meta.impl.MetaValue;
import kz.bsbnb.usci.eav_model.model.persistable.impl.Persistable;
import kz.bsbnb.usci.eav_model.model.type.DataTypes;
import kz.bsbnb.usci.eav_model.util.SetUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

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
    Logger logger = LoggerFactory.getLogger(BaseEntity.class);
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
     * Used to retrieve object structure description. Can be used to modify meta.
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
     * @param name attribute name. Must exist in entity meta
     * @return attribute value, null if value is not set
     * @throws IllegalArgumentException if attribute name does not exist in entity meta,
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
     * @param name name attribute name. Must exist in entity meta
     * @param value new value of the attribute
     * @throws IllegalArgumentException if attribute name does not exist in entity meta,
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
            Class<?> expValueClass;

            if (type.isComplex())
                if(type.isSet())
                {
                    expValueClass = BaseSet.class;
                }
                else
                {
                    expValueClass = BaseEntity.class;
                }
            else
            {
                if(type.isSet())
                {
                    MetaSet metaValue = (MetaSet)type;

                    IMetaType metaValueChild = metaValue.getMemberType();
                    if (type.isSet())
                    {
                        expValueClass = BaseSet.class;
                        valueClass = value.getValue().getClass();
                    }
                    else
                    {
                        expValueClass = metaValue.getTypeCode().getDataTypeClass();
                        valueClass = ((MetaValue)(((BaseSet)value.getValue()).getMemberType())).getTypeCode().
                                getDataTypeClass();
                    }


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

    @Override
    public Set<IBaseValue> get() {
        List<IBaseValue> list = new ArrayList<IBaseValue>(values.values());
        return (new HashSet<IBaseValue>(list));
    }

    @Override
    public IMetaType getMemberType(String name) {
        return meta.getMemberType(name);
    }

    /**
     * Set of simple attribute names that are actually set in entity
     *
     * @param dataType - attributes are filtered by this type
     * @return
     */
    public Set<String> getPresentSimpleAttributeNames(DataTypes dataType)
    {
        return SetUtils.intersection(meta.getSimpleAttributesNames(dataType), values.keySet());
    }

    /**
     * Set of complex attribute names that are actually set in entity
     *
     * @return
     */
    public Set<String> getPresentComplexAttributeNames()
    {
        return SetUtils.intersection(meta.getComplexAttributesNames(), values.keySet());
    }

    /**
     * Set of simpleSet attribute names that are actually set in entity
     *
     * @param dataType - attributes are filtered by this type
     * @return
     */
    public Set<String> getPresentSimpleSetAttributeNames(DataTypes dataType)
    {
        return SetUtils.intersection(meta.getSimpleSetAttributesNames(dataType), values.keySet());
    }

    /**
     * Set of complexSet attribute names that are actually set in entity
     *
     * @return
     */
    public Set<String> getPresentComplexArrayAttributeNames()
    {
        return SetUtils.intersection(meta.getComplexArrayAttributesNames(), values.keySet());
    }

    /**
     * Names of all attributes that are actually set in entity
     * @return
     */
    public Set<String> getAttributeNames() {
        return values.keySet();
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
            int thatAttributeCount = that.getAttributeCount();

            if (thisAttributeCount != thatAttributeCount)
                return false;

            Iterator<String> valuesIt = values.keySet().iterator();
            while (valuesIt.hasNext())
            {
                String attributeName = valuesIt.next();

                IBaseValue thisValue;
                IBaseValue thatValue;

                try
                {
                    thisValue = this.getBaseValue(attributeName);
                }
                catch(IllegalArgumentException e)
                {
                    thisValue = null;
                }

                try
                {
                    thatValue = that.getBaseValue(attributeName);
                }
                catch(IllegalArgumentException e)
                {
                    thatValue = null;
                }

                logger.debug("Attribute: " + attributeName);
                logger.debug("This: " + thisValue);
                logger.debug("That: " + thatValue);

                if(thisValue == null && thatValue == null)
                    continue;

                if(thisValue == null || thatValue == null)
                    return false;

                if(!thisValue.getRepDate().equals(thatValue.getRepDate()))
                    return false;

                if (this.getMeta().getMemberType(attributeName).isSet())
                {
                    BaseSet thisSet = (BaseSet)(thisValue.getValue());
                    BaseSet thatSet = (BaseSet)(thatValue.getValue());

                    Set<IBaseValue> thisBatchValues = thisSet.get();
                    Set<IBaseValue> thatBatchValues = thatSet.get();

                    if(!thisBatchValues.equals(thatBatchValues))
                        return false;
                }
                else
                {
                    IBaseValue thisBaseValue = this.getBaseValue(attributeName);
                    IBaseValue thatBaseValue = that.getBaseValue(attributeName);

                    if (!thisBaseValue.equals(thatBaseValue))
                        return false;
                }
            }

            return true;
        }
    }

    @Override
    public String toString()
    {
        return meta.getClassName();
    }

}
