package kz.bsbnb.usci.eav.model;

import java.util.*;

import kz.bsbnb.usci.eav.model.batchdata.IGenericBatchValue;
import kz.bsbnb.usci.eav.model.batchdata.impl.GenericBatchValue;
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
    private HashMap<String, IGenericBatchValue> data =
            new HashMap<String, IGenericBatchValue>();
    private HashMap<String, ArrayList<IGenericBatchValue>> dataForArray =
            new HashMap<String, ArrayList<IGenericBatchValue>>();

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
    public void setMeta(MetaClass meta) {
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
    public Date getDate(String name)
    {
        IMetaType type = meta.getMemberType(name);
        
        if(type == null)
            throw new IllegalArgumentException("Type: " + name + 
                    ", not found in class: " + meta.getClassName());

        if(type.isComplex())
            throw new IllegalArgumentException("Type: " + name + 
                    ", is an object of class: " + ((MetaClass)type).getClassName());
        
        MetaValue simpleType = (MetaValue)type;
        
        if(simpleType.getTypeCode() != DataTypes.DATE)
            throw new IllegalArgumentException("Type mismatch in class: " + 
                    meta.getClassName() + ". Needed " + DataTypes.DATE + ", got: " + 
                    simpleType.getTypeCode());

        IGenericBatchValue<Date> batchValue = data.get(name);

        if(batchValue == null)
            return null;

        return batchValue.getValue();
    }
    
    /**
     * Retrieves attribute titled <code>name</code>. Attribute must have type of <code>DataTypes.STRING</code>
     * 
     * @param name attribute name. Must exist in entity metadata 
     * @return attribute value, null if value is not set
     * @throws IllegalArgumentException if attribute name does not exist in entity metadata,
     * 	                                or attribute has type different from <code>DataTypes.STRING</code>
     * @see DataTypes
     */
    public String getString(String name)
    {
    	IMetaType type = meta.getMemberType(name);
        
        if(type == null)
            throw new IllegalArgumentException("Type: " + name + 
                    ", not found in class: " + meta.getClassName());
        
        if(type.isComplex())
            throw new IllegalArgumentException("Type: " + name + 
                    ", is an object of class: " + ((MetaClass)type).getClassName());
        
        MetaValue simpleType = (MetaValue)type;
        
        if(simpleType.getTypeCode() != DataTypes.STRING)
            throw new IllegalArgumentException("Type mismatch in class: " + 
                    meta.getClassName() + ". Needed " + DataTypes.STRING + ", got: " + 
                    simpleType.getTypeCode());
        
        IGenericBatchValue<String> batchValue = data.get(name);
        
        if(batchValue == null)
            return null;
        
        return batchValue.getValue();
    }
    
    /**
     * Retrieves attribute titled <code>name</code>. Attribute must have type of <code>DataTypes.INTEGER</code>
     * 
     * @param name attribute name. Must exist in entity metadata 
     * @return attribute value, null if value is not set
     * @throws IllegalArgumentException if attribute name does not exist in entity metadata,
     * 	                                or attribute has type different from <code>DataTypes.INTEGER</code>
     * @see DataTypes
     */
    public Integer getInteger(String name)
    {
    	IMetaType type = meta.getMemberType(name);
        
        if(type == null)
            throw new IllegalArgumentException("Type: " + name + 
                    ", not found in class: " + meta.getClassName());
        
        if(type.isComplex())
            throw new IllegalArgumentException("Type: " + name + 
                    ", is an object of class: " + ((MetaClass)type).getClassName());
        
        MetaValue simpleType = (MetaValue)type;
        
        if(simpleType.getTypeCode() != DataTypes.INTEGER)
            throw new IllegalArgumentException("Type mismatch in class: " + 
                    meta.getClassName() + ". Needed " + DataTypes.INTEGER + ", got: " + 
                    simpleType.getTypeCode());

        IGenericBatchValue<Integer> batchValue = data.get(name);

        if(batchValue == null)
            return null;

        return batchValue.getValue();
    }
    
    /**
     * Retrieves attribute titled <code>name</code>. Attribute must have type of <code>DataTypes.DOUBLE</code>
     * 
     * @param name attribute name. Must exist in entity metadata 
     * @return attribute value, null if value is not set
     * @throws IllegalArgumentException if attribute name does not exist in entity metadata,
     * 	                                or attribute has type different from <code>DataTypes.DOUBLE</code>
     * @see DataTypes
     */
    public Double getDouble(String name)
    {
    	IMetaType type = meta.getMemberType(name);
        
        if(type == null)
            throw new IllegalArgumentException("Type: " + name + 
                    ", not found in class: " + meta.getClassName());
        
        if(type.isComplex())
            throw new IllegalArgumentException("Type: " + name + 
                    ", is an object of class: " + ((MetaClass)type).getClassName());
        
        MetaValue simpleType = (MetaValue)type;
        
        if(simpleType.getTypeCode() != DataTypes.DOUBLE)
            throw new IllegalArgumentException("Type mismatch in class: " + 
                    meta.getClassName() + ". Needed " + DataTypes.DOUBLE + ", got: " + 
                    simpleType.getTypeCode());

        IGenericBatchValue<Double> batchValue = data.get(name);

        if(batchValue == null)
            return null;

        return batchValue.getValue();
    }
    
    /**
     * Retrieves attribute titled <code>name</code>. Attribute must have type of <code>DataTypes.BOOLEAN</code>
     * 
     * @param name attribute name. Must exist in entity metadata 
     * @return attribute value, null if value is not set
     * @throws IllegalArgumentException if attribute name does not exist in entity metadata,
     * 	                                or attribute has type different from <code>DataTypes.BOOLEAN</code>
     * @see DataTypes
     */
    public Boolean getBoolean(String name)
    {
    	IMetaType type = meta.getMemberType(name);
        
        if(type == null)
            throw new IllegalArgumentException("Type: " + name + 
                    ", not found in class: " + meta.getClassName());
        
        if(type.isComplex())
            throw new IllegalArgumentException("Type: " + name + 
                    ", is an object of class: " + ((MetaClass)type).getClassName());
        
        MetaValue simpleType = (MetaValue)type;
        
        if(simpleType.getTypeCode() != DataTypes.BOOLEAN)
            throw new IllegalArgumentException("Type mismatch in class: " + 
                    meta.getClassName() + ". Needed " + DataTypes.BOOLEAN + ", got: " + 
                    simpleType.getTypeCode());

        IGenericBatchValue<Boolean> batchValue = data.get(name);

        if(batchValue == null)
            return null;

        return batchValue.getValue();
    }
    
    /**
     * Retrieves attribute titled <code>name</code>. Attribute must have type of <code>DataTypes.COMPLEX</code>
     * 
     * @param name attribute name. Must exist in entity metadata 
     * @return attribute value, null if value is not set
     * @throws IllegalArgumentException if attribute name does not exist in entity metadata,
     * 	                                or attribute has type different from <code>DataTypes.COMPLEX</code>
     * @see DataTypes
     */
    public BaseEntity getComplex(String name)
    {
        IMetaType type = meta.getMemberType(name);
        
        if(type == null)
            throw new IllegalArgumentException("Type: " + name + 
                    ", not found in class: " + meta.getClassName());
        
        if(!type.isComplex())
            throw new IllegalArgumentException("Type: " + name + 
                    ", is not an object of class. It's a simple value with type " + 
            		((MetaValue)type).getTypeCode());

        IGenericBatchValue<BaseEntity> batchValue = data.get(name);

        if(batchValue == null)
            return null;

        return batchValue.getValue();
    }

    /**
     * Retrieves attribute titled <code>name</code>. Attribute must have type of <code>DataTypes.DATE</code>.
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
    public void set(String name, long index, Date value)
    {
        if (defaultBatch == null) {
            throw new IllegalStateException("Default Batch is not set.");
        }
        set(name, defaultBatch, index, value);
    }

    /**
     * Retrieves attribute titled <code>name</code>. Attribute must have type of <code>DataTypes.DATE</code>
     * 
     * @param name name attribute name. Must exist in entity metadata
     * @param batch information about the origin of this value
     * @param index the index of value
     * @param value new value of the attribute
     * @throws IllegalArgumentException if attribute name does not exist in entity metadata,
     * 	                                or attribute has type different from <code>DataTypes.DATE</code>
     * @see DataTypes
     */
    public void set(String name, Batch batch, long index, Date value)
    {
    	IMetaType type = meta.getMemberType(name);
        
        if(type == null)
            throw new IllegalArgumentException("Type: " + name + 
                    ", not found in class: " + meta.getClassName());
        
        if(type.isComplex())
            throw new IllegalArgumentException("Type: " + name + 
                    ", is an object of class: " + ((MetaClass)type).getClassName());
        
        MetaValue simpleType = (MetaValue)type;
        
        if(simpleType.getTypeCode() != DataTypes.DATE)
            throw new IllegalArgumentException("Type mismatch in class: " +
                    meta.getClassName() + ". Needed " + DataTypes.DATE + ", got: " +
                    simpleType.getTypeCode());
        
        data.put(name, new GenericBatchValue<Date>(batch, index, value));
    }

    /**
     * Retrieves attribute titled <code>name</code>. Attribute must have type of <code>DataTypes.DATE</code>.
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
    public void set(String name, long index, String value)
    {
        if (defaultBatch == null)
        {
            throw new IllegalStateException("Default Batch is not set.");
        }
        set(name, defaultBatch, index, value);
    }
    
    /**
     * Retrieves attribute titled <code>name</code>. Attribute must have type of <code>DataTypes.STRING</code>
     * 
     * @param name name attribute name. Must exist in entity metadata
     * @param batch information about the origin of this value
     * @param index the index of value
     * @param value new value of the attribute
     * @throws IllegalArgumentException if attribute name does not exist in entity metadata,
     * 	                                or attribute has type different from <code>DataTypes.STRING</code>
     * @see DataTypes
     */
    public void set(String name, Batch batch, long index, String value)
    {
    	IMetaType type = meta.getMemberType(name);
        
        if(type == null)
            throw new IllegalArgumentException("Type: " + name + 
                    ", not found in class: " + meta.getClassName());
        
        if(type.isComplex())
            throw new IllegalArgumentException("Type: " + name + 
                    ", is an object of class: " + ((MetaClass)type).getClassName());

        MetaValue simpleType = (MetaValue)type;
        
        if(simpleType.getTypeCode() != DataTypes.STRING)
            throw new IllegalArgumentException("Type mismatch in class: " + 
                    meta.getClassName() + ". Needed " + DataTypes.STRING + ", got: " + 
                    simpleType.getTypeCode());
        
        data.put(name, new GenericBatchValue<String>(batch, index, value));
    }

    /**
     * Retrieves attribute titled <code>name</code>. Attribute must have type of <code>DataTypes.DATE</code>.
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
    public void set(String name, long index, Integer value)
    {
        if (defaultBatch == null)
        {
            throw new IllegalStateException("Default Batch is not set.");
        }
        set(name, defaultBatch, index, value);
    }

    /**
     * Retrieves attribute titled <code>name</code>. Attribute must have type of <code>DataTypes.INTEGER</code>
     * 
     * @param name name attribute name. Must exist in entity metadata
     * @param batch information about the origin of this value
     * @param index the index of value
     * @param value new value of the attribute
     * @throws IllegalArgumentException if attribute name does not exist in entity metadata,
     * 	                                or attribute has type different from <code>DataTypes.INTEGER</code>
     * @see DataTypes
     */
    public void set(String name, Batch batch, long index, Integer value)
    {
    	IMetaType type = meta.getMemberType(name);
        
        if(type == null)
            throw new IllegalArgumentException("Type: " + name + 
                    ", not found in class: " + meta.getClassName());
        
        if(type.isComplex())
            throw new IllegalArgumentException("Type: " + name + 
                    ", is an object of class: " + ((MetaClass)type).getClassName());
        
        MetaValue simpleType = (MetaValue)type;
        
        if(simpleType.getTypeCode() != DataTypes.INTEGER)
            throw new IllegalArgumentException("Type mismatch in class: " + 
                    meta.getClassName() + ". Needed " + DataTypes.INTEGER + ", got: " + 
                    simpleType.getTypeCode());
        
        data.put(name, new GenericBatchValue<Integer>(batch, index, value));
    }

    /**
     * Retrieves attribute titled <code>name</code>. Attribute must have type of <code>DataTypes.DATE</code>.
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
    public void set(String name, long index, Double value)
    {
        if (defaultBatch == null)
        {
            throw new IllegalStateException("Default Batch is not set.");
        }
        set(name, defaultBatch, index, value);
    }

    /**
     * Retrieves attribute titled <code>name</code>. Attribute must have type of <code>DataTypes.DOUBLE</code>
     * 
     * @param name name attribute name. Must exist in entity metadata
     * @param batch information about the origin of this value
     * @param index the index of value
     * @param value new value of the attribute
     * @throws IllegalArgumentException if attribute name does not exist in entity metadata,
     * 	                                or attribute has type different from <code>DataTypes.DOUBLE</code>
     * @see DataTypes
     */
    public void set(String name, Batch batch, long index, Double value)
    {
    	IMetaType type = meta.getMemberType(name);
        
        if(type == null)
            throw new IllegalArgumentException("Type: " + name + 
                    ", not found in class: " + meta.getClassName());
        
        if(type.isComplex())
            throw new IllegalArgumentException("Type: " + name + 
                    ", is an object of class: " + ((MetaClass)type).getClassName());
        
        MetaValue simpleType = (MetaValue)type;
        
        if(simpleType.getTypeCode() != DataTypes.DOUBLE)
            throw new IllegalArgumentException("Type mismatch in class: " + 
                    meta.getClassName() + ". Needed " + DataTypes.DOUBLE + ", got: " + 
                    simpleType.getTypeCode());
        
        data.put(name, new GenericBatchValue<Double>(batch, index, value));
    }

    /**
     * Retrieves attribute titled <code>name</code>. Attribute must have type of <code>DataTypes.DATE</code>.
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
    public void set(String name, long index, Boolean value)
    {
        if (defaultBatch == null)
        {
            throw new IllegalStateException("Default Batch is not set.");
        }
        set(name, defaultBatch, index, value);
    }

    /**
     * Retrieves attribute titled <code>name</code>. Attribute must have type of <code>DataTypes.BOOLEAN</code>
     * 
     * @param name name attribute name. Must exist in entity metadata
     * @param batch information about the origin of this value
     * @param index the index of value
     * @param value new value of the attribute
     * @throws IllegalArgumentException if attribute name does not exist in entity metadata,
     * 	                                or attribute has type different from <code>DataTypes.BOOLEAN</code>
     * @see DataTypes
     */
    public void set(String name, Batch batch, long index,  Boolean value)
    {
    	IMetaType type = meta.getMemberType(name);
        
        if(type == null)
            throw new IllegalArgumentException("Type: " + name + 
                    ", not found in class: " + meta.getClassName());
        
        if(type.isComplex())
            throw new IllegalArgumentException("Type: " + name + 
                    ", is an object of class: " + ((MetaClass)type).getClassName());
        
        MetaValue simpleType = (MetaValue)type;
        
        if(simpleType.getTypeCode() != DataTypes.BOOLEAN)
            throw new IllegalArgumentException("Type mismatch in class: " + 
                    meta.getClassName() + ". Needed " + DataTypes.BOOLEAN + ", got: " + 
                    simpleType.getTypeCode());
        
        data.put(name, new GenericBatchValue<Boolean>(batch, index, value));
    }

    /**
     * Retrieves attribute titled <code>name</code>. Attribute must have type of <code>DataTypes.DATE</code>.
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
    public void set(String name, long index, BaseEntity value)
    {
        if (defaultBatch == null)
        {
            throw new IllegalStateException("Default Batch is not set.");
        }
        set(name, defaultBatch, index, value);
    }

    /**
     * Retrieves attribute titled <code>name</code>. Attribute must have type of <code>DataTypes.COMPLEX</code>
     * 
     * @param name name attribute name. Must exist in entity metadata
     * @param batch information about the origin of this value
     * @param index the index of value
     * @param value new value of the attribute
     * @throws IllegalArgumentException if attribute name does not exist in entity metadata,
     * 	                                or attribute has type different from <code>DataTypes.COMPLEX</code>
     * @see DataTypes
     */
    public void set(String name, Batch batch, long index, BaseEntity value)
    {
    	IMetaType type = meta.getMemberType(name);
        
        if(type == null)
            throw new IllegalArgumentException("Type: " + name + 
                    ", not found in class: " + meta.getClassName());
        
        if(!type.isComplex())
            throw new IllegalArgumentException("Type: " + name + 
                    ", is not an object of class. It's a simple value with type " + 
            		((MetaValue)type).getTypeCode());

        data.put(name, new GenericBatchValue<BaseEntity>(batch, index, value));
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
    public ArrayList<IGenericBatchValue> getDateArray(String name)
    {
        IMetaType type = meta.getMemberType(name);

        if(type == null)
            throw new IllegalArgumentException("Type: " + name +
                    ", not found in class: " + meta.getClassName());

        if(type.isComplex())
            throw new IllegalArgumentException("Type: " + name +
                    ", is an object of class: " + ((MetaClass)type).getClassName());

        if(!type.isArray())
            throw new IllegalArgumentException("Type: " + name +
                    ", is not an array");

        MetaValueArray simpleType = (MetaValueArray)type;

        if(simpleType.getTypeCode() != DataTypes.DATE)
            throw new IllegalArgumentException("Type mismatch in class: " +
                    meta.getClassName() + ". Needed " + DataTypes.DATE + ", got: " +
                    simpleType.getTypeCode());

        ArrayList<IGenericBatchValue> batchValues = dataForArray.get(name);

        if(batchValues == null)
        {
            batchValues = new ArrayList<IGenericBatchValue>();
            dataForArray.put(name, batchValues);
        }

        return batchValues;
    }

    /**
     * Retrieves attribute titled <code>name</code>. Attribute must have type of <code>DataTypes.STRING</code>
     *
     * @param name attribute name. Must exist in entity metadata
     * @return attribute value, null if value is not set
     * @throws IllegalArgumentException if attribute name does not exist in entity metadata,
     * 	                                or attribute has type different from <code>DataTypes.STRING</code>
     * @see DataTypes
     */
    public ArrayList<IGenericBatchValue> getStringArray(String name)
    {
        IMetaType type = meta.getMemberType(name);

        if(type == null)
            throw new IllegalArgumentException("Type: " + name +
                    ", not found in class: " + meta.getClassName());

        if(type.isComplex())
            throw new IllegalArgumentException("Type: " + name +
                    ", is an object of class: " + ((MetaClass)type).getClassName());

        if(!type.isArray())
            throw new IllegalArgumentException("Type: " + name +
                    ", is not an array");

        MetaValueArray simpleType = (MetaValueArray)type;

        if(simpleType.getTypeCode() != DataTypes.STRING)
            throw new IllegalArgumentException("Type mismatch in class: " +
                    meta.getClassName() + ". Needed " + DataTypes.STRING + ", got: " +
                    simpleType.getTypeCode());

        ArrayList<IGenericBatchValue> batchValues = dataForArray.get(name);

        if(batchValues == null)
        {
            batchValues = new ArrayList<IGenericBatchValue>();
            dataForArray.put(name, batchValues);
        }

        return batchValues;
    }

    /**
     * Retrieves attribute titled <code>name</code>. Attribute must have type of <code>DataTypes.INTEGER</code>
     *
     * @param name attribute name. Must exist in entity metadata
     * @return attribute value, null if value is not set
     * @throws IllegalArgumentException if attribute name does not exist in entity metadata,
     * 	                                or attribute has type different from <code>DataTypes.INTEGER</code>
     * @see DataTypes
     */
    public List<IGenericBatchValue> getIntegerArray(String name)
    {
        IMetaType type = meta.getMemberType(name);

        if(type == null)
            throw new IllegalArgumentException("Type: " + name +
                    ", not found in class: " + meta.getClassName());

        if(type.isComplex())
            throw new IllegalArgumentException("Type: " + name +
                    ", is an object of class: " + ((MetaClass)type).getClassName());

        if(!type.isArray())
            throw new IllegalArgumentException("Type: " + name +
                    ", is not an array");

        MetaValueArray simpleType = (MetaValueArray)type;

        if(simpleType.getTypeCode() != DataTypes.INTEGER)
            throw new IllegalArgumentException("Type mismatch in class: " +
                    meta.getClassName() + ". Needed " + DataTypes.INTEGER + ", got: " +
                    simpleType.getTypeCode());

        ArrayList<IGenericBatchValue> batchValues = dataForArray.get(name);

        if(batchValues == null)
        {
            batchValues = new ArrayList<IGenericBatchValue>();
            dataForArray.put(name, batchValues);
        }

        return batchValues;
    }

    /**
     * Retrieves attribute titled <code>name</code>. Attribute must have type of <code>DataTypes.DOUBLE</code>
     *
     * @param name attribute name. Must exist in entity metadata
     * @return attribute value, null if value is not set
     * @throws IllegalArgumentException if attribute name does not exist in entity metadata,
     * 	                                or attribute has type different from <code>DataTypes.DOUBLE</code>
     * @see DataTypes
     */
    public List<IGenericBatchValue> getDoubleArray(String name)
    {
        IMetaType type = meta.getMemberType(name);

        if(type == null)
            throw new IllegalArgumentException("Type: " + name +
                    ", not found in class: " + meta.getClassName());

        if(type.isComplex())
            throw new IllegalArgumentException("Type: " + name +
                    ", is an object of class: " + ((MetaClass)type).getClassName());

        if(!type.isArray())
            throw new IllegalArgumentException("Type: " + name +
                    ", is not an array");

        MetaValueArray simpleType = (MetaValueArray)type;

        if(simpleType.getTypeCode() != DataTypes.DOUBLE)
            throw new IllegalArgumentException("Type mismatch in class: " +
                    meta.getClassName() + ". Needed " + DataTypes.DOUBLE + ", got: " +
                    simpleType.getTypeCode());

        ArrayList<IGenericBatchValue> batchValues = dataForArray.get(name);

        if(batchValues == null)
        {
            batchValues = new ArrayList<IGenericBatchValue>();
            dataForArray.put(name, batchValues);
        }

        return batchValues;
    }

    /**
     * Retrieves attribute titled <code>name</code>. Attribute must have type of <code>DataTypes.BOOLEAN</code>
     *
     * @param name attribute name. Must exist in entity metadata
     * @return attribute value, null if value is not set
     * @throws IllegalArgumentException if attribute name does not exist in entity metadata,
     * 	                                or attribute has type different from <code>DataTypes.BOOLEAN</code>
     * @see DataTypes
     */
    public ArrayList<IGenericBatchValue> getBooleanArray(String name)
    {
        IMetaType type = meta.getMemberType(name);

        if(type == null)
            throw new IllegalArgumentException("Type: " + name +
                    ", not found in class: " + meta.getClassName());

        if(type.isComplex())
            throw new IllegalArgumentException("Type: " + name +
                    ", is an object of class: " + ((MetaClass)type).getClassName());

        if(!type.isArray())
            throw new IllegalArgumentException("Type: " + name +
                    ", is not an array");

        MetaValueArray simpleType = (MetaValueArray)type;

        if(simpleType.getTypeCode() != DataTypes.BOOLEAN)
            throw new IllegalArgumentException("Type mismatch in class: " +
                    meta.getClassName() + ". Needed " + DataTypes.BOOLEAN + ", got: " +
                    simpleType.getTypeCode());

        ArrayList<IGenericBatchValue> batchValues = dataForArray.get(name);

        if(batchValues == null)
        {
            batchValues = new ArrayList<IGenericBatchValue>();
            dataForArray.put(name, batchValues);
        }

        return batchValues;
    }

    /**
     * Retrieves attribute titled <code>name</code>. Attribute must have type of <code>DataTypes.COMPLEX</code>
     *
     * @param name attribute name. Must exist in entity metadata
     * @return attribute value, null if value is not set
     * @throws IllegalArgumentException if attribute name does not exist in entity metadata,
     * 	                                or attribute has type different from <code>DataTypes.COMPLEX</code>
     * @see DataTypes
     */
    public ArrayList<IGenericBatchValue> getComplexArray(String name)
    {
        IMetaType type = meta.getMemberType(name);

        if(type == null)
            throw new IllegalArgumentException("Type: " + name +
                    ", not found in class: " + meta.getClassName());

        if(!type.isComplex())
            throw new IllegalArgumentException("Type: " + name +
                    ", is not an object of class. It's a simple value with type " +
                    ((MetaValue)type).getTypeCode());

        if(!type.isArray())
            throw new IllegalArgumentException("Type: " + name +
                    ", is not an array");

        ArrayList<IGenericBatchValue> batchValues = dataForArray.get(name);

        if(batchValues == null)
        {
            batchValues = new ArrayList<IGenericBatchValue>();
            dataForArray.put(name, batchValues);
        }

        return batchValues;
    }

    public Set<String> getPresentSimpleAttributeNames(DataTypes dataType)
    {
        if (!this.meta.isComplex())
            throw new UnsupportedOperationException("Simple types can be contained only in complex types.");

        return SetUtils.intersection(meta.getSimpleAttributesNames(dataType), data.keySet());
    }

    public Set<String> getPresentDateAttributeNames()
    {
        return getPresentSimpleAttributeNames(DataTypes.DATE);
    }

    public Batch getDefaultBatch() {
        return defaultBatch;
    }

}
