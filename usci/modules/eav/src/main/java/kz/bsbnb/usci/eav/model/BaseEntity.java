package kz.bsbnb.usci.eav.model;

import java.util.*;

import kz.bsbnb.usci.eav.model.batchdata.IBatchValue;
import kz.bsbnb.usci.eav.model.batchdata.impl.BatchValue;
import kz.bsbnb.usci.eav.model.metadata.DataTypes;
import kz.bsbnb.usci.eav.model.metadata.type.IMetaType;
import kz.bsbnb.usci.eav.model.metadata.type.impl.MetaClass;
import kz.bsbnb.usci.eav.model.metadata.type.impl.MetaValue;
import kz.bsbnb.usci.eav.model.metadata.type.impl.MetaValueArray;
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
public class BaseEntity extends Persistable
{
    /**
     * Holds data about entity structure
     * @see MetaClass
     */
    private MetaClass meta;

    /**
     * <code>Batch</code> that is used by default when adding values.
     */
    private Batch defaultBatch = null;
    
    /**
     * Holds attributes values
     */
    private HashMap<String, IBatchValue> data =
            new HashMap<String, IBatchValue>();

    private HashMap<String, ArrayList<IBatchValue>> dataForArray =
            new HashMap<String, ArrayList<IBatchValue>>();

    /**
     * Initializes entity.
     */
    public BaseEntity()
    {

    }

    /**
     * Initializes entity with a class name.
     * 
     * @param className the class name.
     */
    public BaseEntity(String className)
    {
        meta = new MetaClass(className);
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
     * Initializes entity with a class name and batch information.
     *
     * @param className the class name.
     * @param defaultBatch information about batch
     */
    public BaseEntity(String className, Batch defaultBatch)
    {
        this(className);
        this.defaultBatch = defaultBatch;
    }

    public BaseEntity(MetaClass meta, Batch defaultBatch)
    {
        this.meta = meta;
        this.defaultBatch = defaultBatch;
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
     * Used to set object structure description.
     *
     * @param meta Object structure
     */
    public void setMeta(MetaClass meta)
    {
        this.meta = meta;
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
    public IBatchValue getBatchValue(String name)
    {
        IMetaType type = meta.getMemberType(name);

        if(type == null)
            throw new IllegalArgumentException("Type: " + name +
                    ", not found in class: " + meta.getClassName());

        IBatchValue batchValue = data.get(name);

        if(batchValue == null)
            return null;

        return batchValue;
    }

    /**
     * Retrieves attribute titled <code>name</code>.
     * Uses default <code>Batch</code>.
     *
     * @param name name attribute name. Must exist in entity metadata
     * @param index the index of value
     * @param value new value of the attribute
     * @throws IllegalArgumentException if attribute name does not exist in entity metadata,
     * 	                                or attribute has type different from <code>DataTypes.DATE</code>
     * @throws IllegalStateException if default <code>Batch</code> is not set
     * @see DataTypes
     */
    public <T> void set(String name, long index, T value)
    {
        if (defaultBatch == null)
            throw new IllegalStateException("Default Batch is not set.");

        set(name, defaultBatch, index, value);
    }

    /**
     * Retrieves attribute titled <code>name</code>.
     *
     * @param name name attribute name. Must exist in entity metadata
     * @param batch information about the origin of this value
     * @param index the index of value
     * @param value new value of the attribute
     * @throws IllegalArgumentException if attribute name does not exist in entity metadata,
     * 	                                or attribute has type different from <code>DataTypes.DATE</code>
     * @see DataTypes
     */
    public <T> void set(String name, Batch batch, long index, T value)
    {
        IMetaType type = meta.getMemberType(name);

        if(type == null)
            throw new IllegalArgumentException("Type: " + name +
                    ", not found in class: " + meta.getClassName());

        if(type.isArray())
            throw new IllegalArgumentException("Type: " + name +
                    ", is an array");

        if (value != null)
        {
            Class<?> valueClass = value.getClass();
            Class<?> expValueClass = null;

            if (type.isComplex())
                expValueClass = BaseEntity.class;
            else
            {
                MetaValue metaValue = (MetaValue)type;
                expValueClass = metaValue.getTypeCode().getDataTypeClass();
            }

            if(expValueClass == null || !expValueClass.isAssignableFrom(valueClass))
                throw new IllegalArgumentException("Type mismatch in class: " +
                        meta.getClassName() + ". Needed " + expValueClass + ", got: " +
                        valueClass);
        }

        data.put(name, new BatchValue(batch, index, value));
    }

    //arrays

    /**
     * Retrieves attribute titled <code>name</code>. Attribute must have type of <code>DataTypes.DATE</code>
     *
     * @param name attribute name. Must exist in entity metadata
     * @return attribute value, null if value is not set
     * @throws IllegalArgumentException if attribute name does not exist in entity metadata,
     * 	                                or attribute has type different from <code>DataTypes.DATE</code>
     * @see DataTypes
     */
    public ArrayList<IBatchValue> getBatchValueArray(String name)
    {
        IMetaType type = meta.getMemberType(name);

        if(type == null)
            throw new IllegalArgumentException("Type: " + name +
                    ", not found in class: " + meta.getClassName());

        if(!type.isArray())
            throw new IllegalArgumentException("Type: " + name +
                    ", is not an array");

        ArrayList<IBatchValue> batchValues = dataForArray.get(name);

        if(batchValues == null)
        {
            batchValues = new ArrayList<IBatchValue>();
            dataForArray.put(name, batchValues);
        }

        return batchValues;
    }

    public <T> void addToArray(String name, long index, T value)
    {
        if (defaultBatch == null)
            throw new IllegalStateException("Default Batch is not set.");

        addToArray(name, defaultBatch, index, value);
    }

    public <T> void addToArray(String name, Batch batch, long index, T value)
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

        Class<?> valueClass = value.getClass();
        Class<?> expValueClass;

        if (type.isComplex())
            expValueClass = BaseEntity.class;
        else
        {
            if (type.isArray())
            {
                MetaValueArray metaClassArray = (MetaValueArray)type;
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


        this.getBatchValueArray(name).add(new BatchValue(batch, index, value));
    }

    public Set<String> getPresentSimpleAttributeNames(DataTypes dataType)
    {
        return SetUtils.intersection(meta.getSimpleAttributesNames(dataType), data.keySet());
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
        return SetUtils.intersection(meta.getComplexAttributesNames(), data.keySet());
    }

    public Set<String> getPresentSimpleArrayAttributeNames(DataTypes dataType) {
        return SetUtils.intersection(meta.getSimpleArrayAttributesNames(dataType), dataForArray.keySet());
    }

    public Set<String> getPresentComplexArrayAttributeNames() {
        return SetUtils.intersection(meta.getComplexArrayAttributesNames(), dataForArray.keySet());
    }

    public Batch getDefaultBatch()
    {
        return defaultBatch;
    }

    @Override
    public String toString()
    {
        return meta.getClassName();
    }
}
