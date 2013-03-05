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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
            Class<?> expValueClass;

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

                    IMetaType metaValueChild = metaValue.getMemberType();
                    if (type.isArray())
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
    public Set<String> getPresentComplexAttributeNames() {
        return SetUtils.intersection(meta.getComplexAttributesNames(), values.keySet());
    }

    /**
     * Set of simpleSet attribute names that are actually set in entity
     *
     * @param dataType - attributes are filtered by this type
     * @return
     */
    public Set<String> getPresentSimpleSetAttributeNames(DataTypes dataType) {
        return SetUtils.intersection(meta.getSimpleSetAttributesNames(dataType), values.keySet());
    }

    /**
     * Set of complexSet attribute names that are actually set in entity
     *
     * @return
     */
    public Set<String> getPresentComplexArrayAttributeNames() {
        return SetUtils.intersection(meta.getComplexArrayAttributesNames(), values.keySet());
    }

    @Override
    public String toString()
    {
        return meta.getClassName();
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
            int thatAttributeCount = this.getAttributeCount();

            if (thisAttributeCount != thatAttributeCount)
                return false;

            Iterator<String> valuesIt = values.keySet().iterator();
            while (valuesIt.hasNext())
            {
                String attributeName = valuesIt.next();
                IBaseValue thisValue = this.getBaseValue(attributeName);
                IBaseValue thatValue = that.getBaseValue(attributeName);

                logger.debug("Attribute: " + attributeName);
                logger.debug("This: " + thisValue);
                logger.debug("That: " + thatValue);

                if(thisValue == null || thatValue == null)
                    return false;

                if (this.getMeta().getMemberType(attributeName).isArray())
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
    public Set<IBaseValue> get() {
        return (Set<IBaseValue>) values.values();
    }

    @Override
    public IMetaType getMemberType(String name) {
        return meta.getMemberType(name);
    }
}
