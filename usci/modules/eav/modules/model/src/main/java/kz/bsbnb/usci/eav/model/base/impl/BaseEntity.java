package kz.bsbnb.usci.eav.model.base.impl;

import com.sun.org.apache.bcel.internal.generic.ACONST_NULL;
import kz.bsbnb.usci.eav.model.base.IBaseContainer;
import kz.bsbnb.usci.eav.model.base.IBaseValue;
import kz.bsbnb.usci.eav.model.meta.IMetaType;
import kz.bsbnb.usci.eav.model.meta.impl.MetaClass;
import kz.bsbnb.usci.eav.model.meta.impl.MetaSet;
import kz.bsbnb.usci.eav.model.meta.impl.MetaValue;
import kz.bsbnb.usci.eav.model.persistable.impl.Persistable;
import kz.bsbnb.usci.eav.model.type.DataTypes;
import kz.bsbnb.usci.eav.util.SetUtils;
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

    private UUID uuid = UUID.randomUUID();

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

    public UUID getUuid() {
        return uuid;
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

    public void remove(String name) {
        values.remove(name);
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
     * @return - set of needed attributes
     */
    public Set<String> getPresentSimpleAttributeNames(DataTypes dataType)
    {
        return SetUtils.intersection(meta.getSimpleAttributesNames(dataType), values.keySet());
    }

    /**
     * Set of complex attribute names that are actually set in entity
     *
     * @return - set of needed attributes
     */
    public Set<String> getPresentComplexAttributeNames()
    {
        return SetUtils.intersection(meta.getComplexAttributesNames(), values.keySet());
    }

    /**
     * Set of simpleSet attribute names that are actually set in entity
     *
     * @param dataType - attributes are filtered by this type
     * @return - set of needed attributes
     */
    public Set<String> getPresentSimpleSetAttributeNames(DataTypes dataType)
    {
        return SetUtils.intersection(meta.getSimpleSetAttributesNames(dataType), values.keySet());
    }

    /**
     * Set of complexSet attribute names that are actually set in entity
     *
     * @return - set of needed attributes
     */
    public Set<String> getPresentComplexArrayAttributeNames()
    {
        return SetUtils.intersection(meta.getComplexArrayAttributesNames(), values.keySet());
    }

    /**
     * Names of all attributes that are actually set in entity
     * @return - set of needed attributes
     */
    public Set<String> getAttributeNames() {
        return values.keySet();
    }

    public int getAttributeCount() {

        return values.size();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
        {
            logger.debug("Same object");
            return true;
        }


        if (obj == null)
        {
            logger.debug("Object is null");
            return false;
        }

        if (!(getClass() == obj.getClass()))
        {
            logger.debug("Different classes");
            return false;
        }
        else
        {
            BaseEntity that = (BaseEntity) obj;

            int thisAttributeCount = this.getAttributeCount();
            int thatAttributeCount = that.getAttributeCount();

            if (thisAttributeCount != thatAttributeCount)
            {
                logger.debug("Different attributes count");
                return false;
            }

            for (String attributeName : values.keySet())
            {
                IBaseValue thisValue;
                IBaseValue thatValue;

                try
                {
                    thisValue = this.getBaseValue(attributeName);
                } catch (IllegalArgumentException e)
                {
                    thisValue = null;
                }

                try
                {
                    thatValue = that.getBaseValue(attributeName);
                } catch (IllegalArgumentException e)
                {
                    thatValue = null;
                }

                logger.debug("Attribute: " + attributeName);
                logger.debug("This: " + thisValue);
                logger.debug("That: " + thatValue);

                if (thisValue == null && thatValue == null)
                {
                    logger.debug("Both null skiped");
                    continue;
                }

                if (thisValue == null || thatValue == null)
                {
                    logger.debug("Null met");
                    return false;
                }

                if (!thisValue.getRepDate().equals(thatValue.getRepDate()))
                {
                    logger.debug("Different repDates");
                    return false;
                }

                if (this.getMeta().getMemberType(attributeName).isSet())
                {
                    logger.debug("It is an array");
                    BaseSet thisSet = (BaseSet) (thisValue.getValue());
                    BaseSet thatSet = (BaseSet) (thatValue.getValue());

                    Set<IBaseValue> thisBatchValues = thisSet.get();
                    Set<IBaseValue> thatBatchValues = thatSet.get();

                    logger.debug("Arrays sizes: " + thisBatchValues.size() + " " + thatBatchValues.size());
                    if(thisBatchValues.size() != thatBatchValues.size())
                    {
                        logger.debug("Sizes are different");
                        return false;
                    }

                    if (!thisBatchValues.containsAll(thatBatchValues))
                    {
                        logger.debug("Arrays are different");
                        return false;
                    }
                } else
                {
                    logger.debug("It is a single value");
                    Object thisActualValue = thisValue.getValue();
                    Object thatActualValue = thatValue.getValue();

                    if(thisActualValue == null && thatActualValue == null)
                    {
                        logger.debug("Both null so skipped.");
                        continue;
                    }

                    if(thisActualValue == null || thatActualValue == null)
                    {
                        logger.debug("One is null");
                        return false;
                    }

                    if (!thisActualValue.equals(thatActualValue))
                    {
                        logger.debug("Single values are different.");
                        return false;
                    }
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

    public IBaseValue safeGetValue(String name)
    {
        try
        {
            return getBaseValue(name);
        }
        catch(Exception e)
        {
            return null;
        }
    }
}
