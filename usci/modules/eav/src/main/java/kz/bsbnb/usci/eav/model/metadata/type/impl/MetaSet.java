package kz.bsbnb.usci.eav.model.metadata.type.impl;

import kz.bsbnb.usci.eav.model.metadata.ComplexKeyTypes;
import kz.bsbnb.usci.eav.model.metadata.DataTypes;
import kz.bsbnb.usci.eav.model.metadata.type.IMetaType;

public class MetaSet implements IMetaType
{
    IMetaType metaType;

    public MetaSet(IMetaType metaType)
    {
        this.metaType = metaType;
    }

    /**
     * When attribute is an array, and is a key attribute - sets key usage strategy.
     * Defaults to <code>ArrayKeyTypes.ALL</code>
     * @see ComplexKeyTypes
     */
    ComplexKeyTypes arrayKeyType = ComplexKeyTypes.ALL;
    
    public ComplexKeyTypes getArrayKeyType()
    {
		return arrayKeyType;
	}

	public void setArrayKeyType(ComplexKeyTypes arrayKeyType)
    {
		this.arrayKeyType = arrayKeyType;
	}
	
	public boolean isArray()
    {
		return true;
	}

    @Override
    public boolean isComplex()
    {
        return metaType.isComplex();
    }

    public void setMetaType(IMetaType metaType)
    {
        this.metaType = metaType;
    }

    public DataTypes getTypeCode()
    {
        if(isComplex())
            throw new IllegalStateException();

        if(metaType.isArray())
            throw new IllegalStateException();

        return ((MetaValue) metaType).getTypeCode();
    }

    public IMetaType getMemberType()
    {
        return metaType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MetaSet)) return false;

        MetaSet metaSet = (MetaSet) o;

        if (arrayKeyType != metaSet.arrayKeyType) return false;
        if (!metaType.equals(metaSet.metaType)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = metaType.hashCode();
        result = 31 * result + (arrayKeyType != null ? arrayKeyType.hashCode() : 0);
        return result;
    }


}
