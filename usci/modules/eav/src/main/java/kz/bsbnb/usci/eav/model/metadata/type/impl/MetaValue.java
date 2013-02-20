package kz.bsbnb.usci.eav.model.metadata.type.impl;

import kz.bsbnb.usci.eav.model.metadata.DataTypes;
import kz.bsbnb.usci.eav.model.metadata.type.IMetaAttribute;
import kz.bsbnb.usci.eav.model.metadata.type.IMetaType;

/**
 * Represents EAV entity attribute's type metadata 
 *
 * @author a.tkachenko
 * @version 1.1, 17.01.2013
 */
public class MetaValue implements IMetaType
{
	/**
	 * Attributes type's code
	 */
    private DataTypes typeCode;

    public MetaValue()
    {
        super();
    }
    
    /**
     * 
     * @param typeCode code of the attribute's type
     */
    public MetaValue(DataTypes typeCode)
    {
        this.typeCode = typeCode;
    }
    
	public DataTypes getTypeCode()
    {
        return typeCode;
    }

    /**
     * 
     * @param type attributes type code
     */
    public void setTypeCode(DataTypes type)
    {
        this.typeCode = type;
    }

    public boolean equals(Object obj)
    {
		if (obj == this)
			return true;

		if (obj == null)
			return false;

		if (!(getClass() == obj.getClass()))
			return false;
		else
        {
			MetaValue tmp = (MetaValue) obj;
            return !(tmp.getTypeCode() != this.getTypeCode());
        }
	}

	@Override
	public boolean isArray()
    {
		return false;
	}

	@Override
	public boolean isComplex()
    {
		return false;
	}
}
