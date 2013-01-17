package kz.bsbnb.usci.eav.model.metadata;

/**
 * Represents EAV entity attribute's type metadata 
 *
 * @author a.tkachenko
 * @version 1.0, 17.01.2013
 */
public class Type {
	/**
	 * Attributes type's code
	 */
    DataTypes typeCode;
    
    /**
     * <code>true</code> if attribute is a key attribute (used by DAO to find persisted entity)
     */
    boolean isKey;
    /**
     * <code>true</code> if attribute can have <code>null</code> value
     * key attributes have this flag always set to false 
     */
    boolean isNullable;
    
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
    
    public DataTypes getTypeCode() {
        return typeCode;
    }

    public void setTypeCode(DataTypes type) {
        this.typeCode = type;
    }

	public boolean isKey() {
		return isKey;
	}

	public void setKey(boolean isKey) {
		this.isKey = isKey;
		this.isNullable = isNullable && !isKey;
	}

	public boolean isNullable() {
		return isNullable;
	}

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
}
