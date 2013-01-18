package kz.bsbnb.usci.eav.model;

import java.util.Date;
import java.util.HashMap;

import kz.bsbnb.usci.eav.model.metadata.DataTypes;
import kz.bsbnb.usci.eav.model.metadata.type.IMetaType;
import kz.bsbnb.usci.eav.model.metadata.type.impl.MetaClass;
import kz.bsbnb.usci.eav.model.metadata.type.impl.MetaValue;

/**
 * Implements EAV entity object. 
 *
 * @version 1.0, 17.01.2013
 * @author a.tkachenko
 * @see MetaData
 * @see DataTypes
 */
public class BaseEntity {
	
	/**
	 * Holds database id attribute value.
	 */
    long id = 0;
    
    /**
     * Holds data about entity structure
     * @see MetaData
     */
    MetaClass meta;
    
    /**
     * Holds attributes values
     */
    HashMap<String, Object> data = new HashMap<String, Object>();
    
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
     * Used to retrieve object structure description. Can be used to modify metadata.
     * 
     * @return Object structure
     */
    public MetaClass getMeta() {
        return meta;
    }

    /**
     * Used to get object database id.
     * 
     * @return database id
     */
    public long getId() {
        return id;
    }

    /**
     * Used by DAO object to set database connection attribute.
     * Do not use it outside DAO objects
     * 
     * @param id database id
     */
    public void setId(long id) {
        this.id = id;
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
        
        if(type == null) {
            throw new IllegalArgumentException("Type: " + name + 
                    ", not found in class: " + meta.getClassName());
        }
        
        if(type.isComplex()) {
            throw new IllegalArgumentException("Type: " + name + 
                    ", is an object of class: " + ((MetaClass)type).getClassName());
        }
        
        MetaValue simpleType = (MetaValue)type;
        
        if(simpleType.getTypeCode() != DataTypes.DATE) {
            throw new IllegalArgumentException("Type missmatch in class: " + 
                    meta.getClassName() + ". Nedded " + DataTypes.DATE + ", got: " + 
                    simpleType.getTypeCode());
        }
        
        Object obj = data.get(name);
        
        if(obj == null) {
            return null;
        }
        
        return (Date)obj;
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
        
        if(type == null) {
            throw new IllegalArgumentException("Type: " + name + 
                    ", not found in class: " + meta.getClassName());
        }
        
        if(type.isComplex()) {
            throw new IllegalArgumentException("Type: " + name + 
                    ", is an object of class: " + ((MetaClass)type).getClassName());
        }
        
        MetaValue simpleType = (MetaValue)type;
        
        if(simpleType.getTypeCode() != DataTypes.STRING) {
            throw new IllegalArgumentException("Type missmatch in class: " + 
                    meta.getClassName() + ". Nedded " + DataTypes.STRING + ", got: " + 
                    simpleType.getTypeCode());
        }
        
        Object obj = data.get(name);
        
        if(obj == null) {
            return null;
        }
        
        return (String)obj;
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
        
        if(type == null) {
            throw new IllegalArgumentException("Type: " + name + 
                    ", not found in class: " + meta.getClassName());
        }
        
        if(type.isComplex()) {
            throw new IllegalArgumentException("Type: " + name + 
                    ", is an object of class: " + ((MetaClass)type).getClassName());
        }
        
        MetaValue simpleType = (MetaValue)type;
        
        if(simpleType.getTypeCode() != DataTypes.INTEGER) {
            throw new IllegalArgumentException("Type missmatch in class: " + 
                    meta.getClassName() + ". Nedded " + DataTypes.INTEGER + ", got: " + 
                    simpleType.getTypeCode());
        }
        
        Object obj = data.get(name);
        
        if(obj == null) {
            return null;
        }
        
        return (Integer)obj;
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
        
        if(type == null) {
            throw new IllegalArgumentException("Type: " + name + 
                    ", not found in class: " + meta.getClassName());
        }
        
        if(type.isComplex()) {
            throw new IllegalArgumentException("Type: " + name + 
                    ", is an object of class: " + ((MetaClass)type).getClassName());
        }
        
        MetaValue simpleType = (MetaValue)type;
        
        if(simpleType.getTypeCode() != DataTypes.DOUBLE) {
            throw new IllegalArgumentException("Type missmatch in class: " + 
                    meta.getClassName() + ". Nedded " + DataTypes.DOUBLE + ", got: " + 
                    simpleType.getTypeCode());
        }
        
        Object obj = data.get(name);
        
        if(obj == null) {
            return null;
        }
        
        return (Double)obj;
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
        
        if(type == null) {
            throw new IllegalArgumentException("Type: " + name + 
                    ", not found in class: " + meta.getClassName());
        }
        
        if(type.isComplex()) {
            throw new IllegalArgumentException("Type: " + name + 
                    ", is an object of class: " + ((MetaClass)type).getClassName());
        }
        
        MetaValue simpleType = (MetaValue)type;
        
        if(simpleType.getTypeCode() != DataTypes.BOOLEAN) {
            throw new IllegalArgumentException("Type missmatch in class: " + 
                    meta.getClassName() + ". Nedded " + DataTypes.BOOLEAN + ", got: " + 
                    simpleType.getTypeCode());
        }
        
        Object obj = data.get(name);
        
        if(obj == null) {
            return null;
        }
        
        return (Boolean)obj;
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
        
        if(type == null) {
            throw new IllegalArgumentException("Type: " + name + 
                    ", not found in class: " + meta.getClassName());
        }
        
        if(!type.isComplex()) {
            throw new IllegalArgumentException("Type: " + name + 
                    ", is not an object of class. It's a simple value with type " + 
            		((MetaValue)type).getTypeCode());
        }
        
        Object obj = data.get(name);
        
        if(obj == null) {
            return null;
        }
        
        return (BaseEntity)obj;
    }
    
    /**
     * Retrieves attribute titled <code>name</code>. Attribute must have type of <code>DataTypes.DATE</code>
     * 
     * @param name name attribute name. Must exist in entity metadata
     * @param value new value of the attribute
     * @throws IllegalArgumentException if attribute name does not exist in entity metadata,
     * 	                                or attribute has type different from <code>DataTypes.DATE</code>
     * @see DataTypes
     */
    public void set(String name, Date value)
    {
    	IMetaType type = meta.getMemberType(name);
        
        if(type == null) {
            throw new IllegalArgumentException("Type: " + name + 
                    ", not found in class: " + meta.getClassName());
        }
        
        if(type.isComplex()) {
            throw new IllegalArgumentException("Type: " + name + 
                    ", is an object of class: " + ((MetaClass)type).getClassName());
        }
        
        MetaValue simpleType = (MetaValue)type;
        
        if(simpleType.getTypeCode() != DataTypes.DATE) {
            throw new IllegalArgumentException("Type missmatch in class: " + 
                    meta.getClassName() + ". Nedded " + DataTypes.DATE + ", got: " + 
                    simpleType.getTypeCode());
        }
        
        data.put(name, value);
    }
    
    /**
     * Retrieves attribute titled <code>name</code>. Attribute must have type of <code>DataTypes.STRING</code>
     * 
     * @param name name attribute name. Must exist in entity metadata
     * @param value new value of the attribute
     * @throws IllegalArgumentException if attribute name does not exist in entity metadata,
     * 	                                or attribute has type different from <code>DataTypes.STRING</code>
     * @see DataTypes
     */
    public void set(String name, String value)
    {
    	IMetaType type = meta.getMemberType(name);
        
        if(type == null) {
            throw new IllegalArgumentException("Type: " + name + 
                    ", not found in class: " + meta.getClassName());
        }
        
        if(type.isComplex()) {
            throw new IllegalArgumentException("Type: " + name + 
                    ", is an object of class: " + ((MetaClass)type).getClassName());
        }
        
        MetaValue simpleType = (MetaValue)type;
        
        if(simpleType.getTypeCode() != DataTypes.STRING) {
            throw new IllegalArgumentException("Type missmatch in class: " + 
                    meta.getClassName() + ". Nedded " + DataTypes.STRING + ", got: " + 
                    simpleType.getTypeCode());
        }
        
        data.put(name, value);
    }
    
    /**
     * Retrieves attribute titled <code>name</code>. Attribute must have type of <code>DataTypes.INTEGER</code>
     * 
     * @param name name attribute name. Must exist in entity metadata
     * @param value new value of the attribute
     * @throws IllegalArgumentException if attribute name does not exist in entity metadata,
     * 	                                or attribute has type different from <code>DataTypes.INTEGER</code>
     * @see DataTypes
     */
    public void set(String name, Integer value)
    {
    	IMetaType type = meta.getMemberType(name);
        
        if(type == null) {
            throw new IllegalArgumentException("Type: " + name + 
                    ", not found in class: " + meta.getClassName());
        }
        
        if(type.isComplex()) {
            throw new IllegalArgumentException("Type: " + name + 
                    ", is an object of class: " + ((MetaClass)type).getClassName());
        }
        
        MetaValue simpleType = (MetaValue)type;
        
        if(simpleType.getTypeCode() != DataTypes.INTEGER) {
            throw new IllegalArgumentException("Type missmatch in class: " + 
                    meta.getClassName() + ". Nedded " + DataTypes.INTEGER + ", got: " + 
                    simpleType.getTypeCode());
        }
        
        data.put(name, value);
    }
    
    /**
     * Retrieves attribute titled <code>name</code>. Attribute must have type of <code>DataTypes.DOUBLE</code>
     * 
     * @param name name attribute name. Must exist in entity metadata
     * @param value new value of the attribute
     * @throws IllegalArgumentException if attribute name does not exist in entity metadata,
     * 	                                or attribute has type different from <code>DataTypes.DOUBLE</code>
     * @see DataTypes
     */
    public void set(String name, Double value)
    {
    	IMetaType type = meta.getMemberType(name);
        
        if(type == null) {
            throw new IllegalArgumentException("Type: " + name + 
                    ", not found in class: " + meta.getClassName());
        }
        
        if(type.isComplex()) {
            throw new IllegalArgumentException("Type: " + name + 
                    ", is an object of class: " + ((MetaClass)type).getClassName());
        }
        
        MetaValue simpleType = (MetaValue)type;
        
        if(simpleType.getTypeCode() != DataTypes.DOUBLE) {
            throw new IllegalArgumentException("Type missmatch in class: " + 
                    meta.getClassName() + ". Nedded " + DataTypes.DOUBLE + ", got: " + 
                    simpleType.getTypeCode());
        }
        
        data.put(name, value);
    }
    
    /**
     * Retrieves attribute titled <code>name</code>. Attribute must have type of <code>DataTypes.BOOLEAN</code>
     * 
     * @param name name attribute name. Must exist in entity metadata
     * @param value new value of the attribute
     * @throws IllegalArgumentException if attribute name does not exist in entity metadata,
     * 	                                or attribute has type different from <code>DataTypes.BOOLEAN</code>
     * @see DataTypes
     */
    public void set(String name, Boolean value)
    {
    	IMetaType type = meta.getMemberType(name);
        
        if(type == null) {
            throw new IllegalArgumentException("Type: " + name + 
                    ", not found in class: " + meta.getClassName());
        }
        
        if(type.isComplex()) {
            throw new IllegalArgumentException("Type: " + name + 
                    ", is an object of class: " + ((MetaClass)type).getClassName());
        }
        
        MetaValue simpleType = (MetaValue)type;
        
        if(simpleType.getTypeCode() != DataTypes.BOOLEAN) {
            throw new IllegalArgumentException("Type missmatch in class: " + 
                    meta.getClassName() + ". Nedded " + DataTypes.BOOLEAN + ", got: " + 
                    simpleType.getTypeCode());
        }
        
        data.put(name, value);
    }
    
    /**
     * Retrieves attribute titled <code>name</code>. Attribute must have type of <code>DataTypes.COMPLEX</code>
     * 
     * @param name name attribute name. Must exist in entity metadata
     * @param value new value of the attribute
     * @throws IllegalArgumentException if attribute name does not exist in entity metadata,
     * 	                                or attribute has type different from <code>DataTypes.COMPLEX</code>
     * @see DataTypes
     */
    public void set(String name, BaseEntity value)
    {
    	IMetaType type = meta.getMemberType(name);
        
        if(type == null) {
            throw new IllegalArgumentException("Type: " + name + 
                    ", not found in class: " + meta.getClassName());
        }
        
        if(!type.isComplex()) {
            throw new IllegalArgumentException("Type: " + name + 
                    ", is not an object of class. It's a simple value with type " + 
            		((MetaValue)type).getTypeCode());
        }
        
        data.put(name, value);
    }
}
