package kz.bsbnb.usci.eav.model.metadata;

/**
 *
 * @author a.tkachenko
 */
public class Type {
    DataTypes typeCode;
    boolean isKey;
    boolean isNullable;
    
    public Type(DataTypes typeCode, boolean isKey, boolean isNullable)
    {
        this.typeCode = typeCode;
        this.isKey = isKey;
        this.isNullable = isNullable;
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
	}

	public boolean isNullable() {
		return isNullable;
	}

	public void setNullable(boolean isNullable) {
		this.isNullable = isNullable;
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
