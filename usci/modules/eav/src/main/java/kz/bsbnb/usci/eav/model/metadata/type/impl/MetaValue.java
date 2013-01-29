package kz.bsbnb.usci.eav.model.metadata.type.impl;

import kz.bsbnb.usci.eav.model.metadata.DataTypes;

/**
 * Represents EAV entity attribute's type metadata 
 *
 * @author a.tkachenko
 * @version 1.1, 17.01.2013
 */
public class MetaValue extends AbstractMetaType {
	/**
	 * Attributes type's code
	 */
    private DataTypes typeCode;

    public MetaValue()
    {
    }
    
    /**
     * 
     * @param typeCode code of the attribute's type 
     * @param isKey <code>true</code> when attribute is a key for search 
     * @param isNullable <code>true</code> when attribute can have <code>null</code> value
     */
    public MetaValue(DataTypes typeCode, boolean isKey, boolean isNullable)
    {
    	super(isKey, isNullable);
        this.typeCode = typeCode;
    }
    
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

    public boolean equals(Object obj) {
		if (obj == this)
			return true;

		if (obj == null)
			return false;

		if (!(getClass() == obj.getClass()))
			return false;
		else {
			MetaValue tmp = (MetaValue) obj;
            return !(tmp.getTypeCode() != this.getTypeCode() ||
                    tmp.isKey() != this.isKey() ||
                    tmp.isNullable() != this.isNullable());

        }
	}

	@Override
	public boolean isArray() {
		return false;
	}

	@Override
	public boolean isComplex() {
		return false;
	}
}
