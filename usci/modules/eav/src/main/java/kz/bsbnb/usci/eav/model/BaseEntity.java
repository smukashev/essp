package kz.bsbnb.usci.eav.model;

import java.util.Date;
import java.util.HashMap;

import kz.bsbnb.usci.eav.model.metadata.DataTypes;
import kz.bsbnb.usci.eav.model.metadata.MetaData;
import kz.bsbnb.usci.eav.model.metadata.Type;

/**
 *
 * @author a.tkachenko
 */
public class BaseEntity {
    String id = null;
    MetaData meta;
    HashMap<String, Object> data = new HashMap<String, Object>();
    
    public BaseEntity(String className)
    {
        meta = new MetaData(className);
    }

    public MetaData getMeta() {
        return meta;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
    
    public Date getDate(String name)
    {
        Type type = meta.getType(name);
        
        if(type == null) {
            throw new IllegalArgumentException("Type: " + name + 
                    ", not found in class: " + meta.getClassName());
        }
        
        if(type.getTypeCode() != DataTypes.DATE) {
            throw new IllegalArgumentException("Type missmatch in class: " + 
                    meta.getClassName() + ". Nedded date, got: " + 
                    type.getTypeCode());
        }
        
        Object obj = data.get(name);
        
        if(obj == null) {
            return null;
        }
        
        return (Date)obj;
    }
    
    public String getString(String name)
    {
        Type type = meta.getType(name);
        
        if(type == null) {
            throw new IllegalArgumentException("Type: " + name + 
                    ", not found in class: " + meta.getClassName());
        }
        
        if(type.getTypeCode() != DataTypes.STRING) {
            throw new IllegalArgumentException("Type missmatch in class: " + 
                    meta.getClassName() + ". Nedded String, got: " + 
                    type.getTypeCode());
        }
        
        Object obj = data.get(name);
        
        if(obj == null) {
            return null;
        }
        
        return (String)obj;
    }
    
    public Integer getInteger(String name)
    {
        Type type = meta.getType(name);
        
        if(type == null) {
            throw new IllegalArgumentException("Type: " + name + 
                    ", not found in class: " + meta.getClassName());
        }
        
        if(type.getTypeCode() != DataTypes.INTEGER) {
            throw new IllegalArgumentException("Type missmatch in class: " + 
                    meta.getClassName() + ". Nedded Integer, got: " + 
                    type.getTypeCode());
        }
        
        Object obj = data.get(name);
        
        if(obj == null) {
            return null;
        }
        
        return (Integer)obj;
    }
    
    public Double getDouble(String name)
    {
        Type type = meta.getType(name);
        
        if(type == null) {
            throw new IllegalArgumentException("Type: " + name + 
                    ", not found in class: " + meta.getClassName());
        }
        
        if(type.getTypeCode() != DataTypes.DOUBLE) {
            throw new IllegalArgumentException("Type missmatch in class: " + 
                    meta.getClassName() + ". Nedded Double, got: " + 
                    type.getTypeCode());
        }
        
        Object obj = data.get(name);
        
        if(obj == null) {
            return null;
        }
        
        return (Double)obj;
    }
    
    public Boolean getBoolean(String name)
    {
        Type type = meta.getType(name);
        
        if(type == null) {
            throw new IllegalArgumentException("Type: " + name + 
                    ", not found in class: " + meta.getClassName());
        }
        
        if(type.getTypeCode() != DataTypes.BOOLEAN) {
            throw new IllegalArgumentException("Type missmatch in class: " + 
                    meta.getClassName() + ". Nedded Boolean, got: " + 
                    type.getTypeCode());
        }
        
        Object obj = data.get(name);
        
        if(obj == null) {
            return null;
        }
        
        return (Boolean)obj;
    }
    
    public void set(String name, Date value)
    {
        Type type = meta.getType(name);
        
        if(type == null) {
            throw new IllegalArgumentException("Type: " + name + 
                    ", not found in class: " + meta.getClassName());
        }
        
        if(type.getTypeCode() != DataTypes.DATE) {
            throw new IllegalArgumentException("Type missmatch in class: " + 
                    meta.getClassName() + ". Nedded Date, got: " + 
                    type.getTypeCode());
        }
        
        data.put(name, value);
    }
    
    public void set(String name, String value)
    {
        Type type = meta.getType(name);
        
        if(type == null) {
            throw new IllegalArgumentException("Type: " + name + 
                    ", not found in class: " + meta.getClassName());
        }
        
        if(type.getTypeCode() != DataTypes.STRING) {
            throw new IllegalArgumentException("Type missmatch in class: " + 
                    meta.getClassName() + ". Nedded String, got: " + 
                    type.getTypeCode());
        }
        
        data.put(name, value);
    }
    
    public void set(String name, Integer value)
    {
        Type type = meta.getType(name);
        
        if(type == null) {
            throw new IllegalArgumentException("Type: " + name + 
                    ", not found in class: " + meta.getClassName());
        }
        
        if(type.getTypeCode() != DataTypes.INTEGER) {
            throw new IllegalArgumentException("Type missmatch in class: " + 
                    meta.getClassName() + ". Nedded Integer, got: " + 
                    type.getTypeCode());
        }
        
        data.put(name, value);
    }
    
    public void set(String name, Double value)
    {
        Type type = meta.getType(name);
        
        if(type == null) {
            throw new IllegalArgumentException("Type: " + name + 
                    ", not found in class: " + meta.getClassName());
        }
        
        if(type.getTypeCode() != DataTypes.DOUBLE) {
            throw new IllegalArgumentException("Type missmatch in class: " + 
                    meta.getClassName() + ". Nedded Double, got: " + 
                    type.getTypeCode());
        }
        
        data.put(name, value);
    }
    
    public void set(String name, Boolean value)
    {
        Type type = meta.getType(name);
        
        if(type == null) {
            throw new IllegalArgumentException("Type: " + name + 
                    ", not found in class: " + meta.getClassName());
        }
        
        if(type.getTypeCode() != DataTypes.BOOLEAN) {
            throw new IllegalArgumentException("Type missmatch in class: " + 
                    meta.getClassName() + ". Nedded Boolean, got: " + 
                    type.getTypeCode());
        }
        
        data.put(name, value);
    }
}
