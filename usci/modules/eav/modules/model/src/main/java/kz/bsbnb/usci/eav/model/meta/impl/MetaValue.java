package kz.bsbnb.usci.eav.model.meta.impl;

import kz.bsbnb.usci.eav.model.meta.IMetaType;
import kz.bsbnb.usci.eav.model.type.DataTypes;

/**
 * Represents EAV entity attribute's type meta
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

    private boolean immutable = false;

    private boolean reference = false;

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
    public int hashCode()
    {
        return typeCode.hashCode();
    }

    @Override
	public boolean isSet()
    {
		return false;
	}

	@Override
	public boolean isComplex()
    {
		return false;
	}

    @Override
    public boolean isSetOfSets() {
        return false;
    }

    @Override
    public String toString(String prefix)
    {
        return "metaValue: " + typeCode;
    }

    @Override
    public boolean isImmutable()
    {
        return immutable;
    }

    @Override
    public boolean isReference()
    {
        return reference;
    }

    @Override
    public void setImmutable(boolean value)
    {
        immutable = value;
    }

    @Override
    public void setReference(boolean value)
    {
        reference = value;
    }
}
