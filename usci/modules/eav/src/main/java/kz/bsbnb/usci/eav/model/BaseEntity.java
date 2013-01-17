package kz.bsbnb.usci.eav.model;

import java.util.Date;
import java.util.HashMap;

import kz.bsbnb.usci.eav.model.metadata.DataTypes;
import kz.bsbnb.usci.eav.model.metadata.MetaData;
import kz.bsbnb.usci.eav.model.metadata.Type;

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
    MetaData meta;
    
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
        meta = new MetaData(className);
    }

    /**
     * Used to retrieve object structure description. Can be used to modify metadata.
     * 
     * @return Object structure
     */
    public MetaData getMeta() {
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
        Type type = meta.getType(name);
        
        if(type == null) {
            throw new IllegalArgumentException("Type: " + name + 
                    ", not found in class: " + meta.getClassName());
        }
        
        if(type.getTypeCode() != DataTypes.DATE) {
            throw new IllegalArgumentException("Type missmatch in class: " + 
                    meta.getClassName() + ". Nedded " + DataTypes.DATE + ", got: " + 
                    type.getTypeCode());
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
        Type type = meta.getType(name);
        
        if(type == null) {
            throw new IllegalArgumentException("Type: " + name + 
                    ", not found in class: " + meta.getClassName());
        }
        
        if(type.getTypeCode() != DataTypes.STRING) {
            throw new IllegalArgumentException("Type missmatch in class: " + 
                    meta.getClassName() + ". Nedded " + DataTypes.STRING + ", got: " + 
                    type.getTypeCode());
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
        Type type = meta.getType(name);
        
        if(type == null) {
            throw new IllegalArgumentException("Type: " + name + 
                    ", not found in class: " + meta.getClassName());
        }
        
        if(type.getTypeCode() != DataTypes.INTEGER) {
            throw new IllegalArgumentException("Type missmatch in class: " + 
                    meta.getClassName() + ". Nedded " + DataTypes.INTEGER + ", got: " + 
                    type.getTypeCode());
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
        Type type = meta.getType(name);
        
        if(type == null) {
            throw new IllegalArgumentException("Type: " + name + 
                    ", not found in class: " + meta.getClassName());
        }
        
        if(type.getTypeCode() != DataTypes.DOUBLE) {
            throw new IllegalArgumentException("Type missmatch in class: " + 
                    meta.getClassName() + ". Nedded " + DataTypes.DOUBLE + ", got: " + 
                    type.getTypeCode());
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
        Type type = meta.getType(name);
        
        if(type == null) {
            throw new IllegalArgumentException("Type: " + name + 
                    ", not found in class: " + meta.getClassName());
        }
        
        if(type.getTypeCode() != DataTypes.BOOLEAN) {
            throw new IllegalArgumentException("Type missmatch in class: " + 
                    meta.getClassName() + ". Nedded " + DataTypes.BOOLEAN + ", got: " + 
                    type.getTypeCode());
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
        Type type = meta.getType(name);
        
        if(type == null) {
            throw new IllegalArgumentException("Type: " + name + 
                    ", not found in class: " + meta.getClassName());
        }
        
        if(type.getTypeCode() != DataTypes.COMPLEX) {
            throw new IllegalArgumentException("Type missmatch in class: " + 
                    meta.getClassName() + ". Nedded " + DataTypes.COMPLEX + ", got: " + 
                    type.getTypeCode());
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
        Type type = meta.getType(name);
        
        if(type == null) {
            throw new IllegalArgumentException("Type: " + name + 
                    ", not found in class: " + meta.getClassName());
        }
        
        if(type.getTypeCode() != DataTypes.DATE) {
            throw new IllegalArgumentException("Type missmatch in class: " + 
                    meta.getClassName() + ". Nedded " + DataTypes.DATE + ", got: " + 
                    type.getTypeCode());
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
        Type type = meta.getType(name);
        
        if(type == null) {
            throw new IllegalArgumentException("Type: " + name + 
                    ", not found in class: " + meta.getClassName());
        }
        
        if(type.getTypeCode() != DataTypes.STRING) {
            throw new IllegalArgumentException("Type missmatch in class: " + 
                    meta.getClassName() + ". Nedded " + DataTypes.STRING + ", got: " + 
                    type.getTypeCode());
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
        Type type = meta.getType(name);
        
        if(type == null) {
            throw new IllegalArgumentException("Type: " + name + 
                    ", not found in class: " + meta.getClassName());
        }
        
        if(type.getTypeCode() != DataTypes.INTEGER) {
            throw new IllegalArgumentException("Type missmatch in class: " + 
                    meta.getClassName() + ". Nedded " + DataTypes.INTEGER + ", got: " + 
                    type.getTypeCode());
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
        Type type = meta.getType(name);
        
        if(type == null) {
            throw new IllegalArgumentException("Type: " + name + 
                    ", not found in class: " + meta.getClassName());
        }
        
        if(type.getTypeCode() != DataTypes.DOUBLE) {
            throw new IllegalArgumentException("Type missmatch in class: " + 
                    meta.getClassName() + ". Nedded " + DataTypes.DOUBLE + ", got: " + 
                    type.getTypeCode());
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
        Type type = meta.getType(name);
        
        if(type == null) {
            throw new IllegalArgumentException("Type: " + name + 
                    ", not found in class: " + meta.getClassName());
        }
        
        if(type.getTypeCode() != DataTypes.BOOLEAN) {
            throw new IllegalArgumentException("Type missmatch in class: " + 
                    meta.getClassName() + ". Nedded " + DataTypes.BOOLEAN + ", got: " + 
                    type.getTypeCode());
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
        Type type = meta.getType(name);
        
        if(type == null) {
            throw new IllegalArgumentException("Type: " + name + 
                    ", not found in class: " + meta.getClassName());
        }
        
        if(type.getTypeCode() != DataTypes.COMPLEX) {
            throw new IllegalArgumentException("Type missmatch in class: " + 
                    meta.getClassName() + ". Nedded " + DataTypes.COMPLEX + ", got: " + 
                    type.getTypeCode());
        }
        
        data.put(name, value);
    }
}
