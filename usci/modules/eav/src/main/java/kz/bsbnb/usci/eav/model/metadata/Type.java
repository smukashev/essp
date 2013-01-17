package kz.bsbnb.usci.eav.model.metadata;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Represents EAV entity attribute's type metadata 
 *
 * @author a.tkachenko
 * @version 1.1, 17.01.2013
 */
public class Type {
	/**
	 * Attributes type's code
	 */
    DataTypes typeCode;
    
    /**
     * <code>true</code> if attribute is a key attribute (used by DAO to find persisted entity)
     * if an attribute has type DataTypes.COMPLEX then all it's key values will be used
     * Defaults to <code>false</code>
     */
    boolean isKey = false;
    
    /**
     * <code>true</code> if attribute can have <code>null</code> value
     * key attributes have this flag always set to false
     * Defaults to <code>true</code> 
     */
    boolean isNullable = true;
    
    /**
     * <code>true</code> if attribute can have multiple values
     * Defaults to <code>false</code>
     */
    boolean isArray = false;
    
    /**
     * When attribute is an array, and is a key attribute - sets key usage strategy.
     * Defaults to <code>ArrayKeyTypes.ALL</code>
     * @see ComplexKeyTypes
     */
    ComplexKeyTypes arrayKeyType = ComplexKeyTypes.ALL;
    
    /**
     * When attribute is an entity, and is a key attribute - sets key usage strategy.
     * Defaults to <code>ArrayKeyTypes.ALL</code>
     * @see ComplexKeyTypes
     */
    ComplexKeyTypes complexKeyType = ComplexKeyTypes.ALL;
    
    /**
     * When attribute is an array, and is a key attribute, and has type DataTypes.COMPLEX - 
     * sets array elements filter.
     * First parameter is array elements attribute name (must be a key attribute).
     * Second parameter is a list of it's values string representation.
     * 
     * Example:
     * A document array as a key value for person.
     * <code>arrayKeyType</code> is set to <code>ArrayKeyTypes.ALL</code>.
     * And <code>arrayKeyFilter</code> has a value (pseudo code) <code>["docType" => ["RNN", "UDL"]]</code>
     * Then we will get persons with the same RNN and UDL documents.
     * Other documents won't be used by DAO.
     * 
     * @see DataTypes
     */
    HashMap<String, ArrayList<String>> arrayKeyFilter = new HashMap<String, ArrayList<String>>();
    
    /**
     * 
     * @param typeCode code of the attribute's type 
     * @param isKey <code>true</code> when attribute is a key for search 
     * @param isNullable <code>true</code> when attribute can have <code>null</code> value
     */
    public Type(DataTypes typeCode, boolean isKey, boolean isNullable)
    {
        this.typeCode = typeCode;
        this.isKey = isKey;
        this.isNullable = isNullable && !isKey;
    }
    
    /**
     * 
     * @return attribute's type code
     */
    public DataTypes getTypeCode() {
        return typeCode;
    }

    /**
     * 
     * @param type attributes type code
     */
    public void setTypeCode(DataTypes type) {
        this.typeCode = type;
    }

    /**
     * 
     * @return <code>true</code>, when attribute is a key attribute 
     */
	public boolean isKey() {
		return isKey;
	}

	/**
	 * 
	 * @param isKey <code>true</code>, when attribute is a key attribute
	 */
	public void setKey(boolean isKey) {
		this.isKey = isKey;
		this.isNullable = isNullable && !isKey;
	}

	/**
	 * 
	 * @return <code>true</code>, when attribute can have null value
	 */
	public boolean isNullable() {
		return isNullable;
	}

	/**
	 * 
	 * @param isNullable <code>true</code>, when attribute can have null value
	 */
	public void setNullable(boolean isNullable) {
		this.isNullable = isNullable && !isKey;
	}
	
	public boolean equals(Object obj) {
		if (obj == this)
			return true;

		if (obj == null)
			return false;

		if (!(getClass() == obj.getClass()))
			return false;
		else {
			Type tmp = (Type) obj;
			if (tmp.getTypeCode() != this.getTypeCode() ||
					tmp.isKey() != this.isKey() ||
					tmp.isNullable() != this.isNullable())
				return false;
			
			return true;
		}
	}

	public boolean isArray() {
		return isArray;
	}

	public void setArray(boolean isArray) {
		this.isArray = isArray;
	}

	public ComplexKeyTypes getArrayKeyType() {
		return arrayKeyType;
	}

	public void setArrayKeyType(ComplexKeyTypes arrayKeyType) {
		this.arrayKeyType = arrayKeyType;
	}

	public ComplexKeyTypes getComplexKeyType() {
		return complexKeyType;
	}

	public void setComplexKeyType(ComplexKeyTypes complexKeyType) {
		this.complexKeyType = complexKeyType;
	}
}
