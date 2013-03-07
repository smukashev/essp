package kz.bsbnb.usci.eav_model.model.meta.impl;

import kz.bsbnb.usci.eav_model.model.meta.IMetaAttribute;
import kz.bsbnb.usci.eav_model.model.meta.IMetaContainer;
import kz.bsbnb.usci.eav_model.model.meta.IMetaType;
import kz.bsbnb.usci.eav_model.model.persistable.impl.Persistable;
import kz.bsbnb.usci.eav_model.model.type.ComplexKeyTypes;
import kz.bsbnb.usci.eav_model.model.type.DataTypes;

public class MetaSet  extends Persistable implements IMetaType, IMetaContainer
{
    IMetaType metaType;

    public MetaSet()
    {
    }

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

    @Override
    public void setMetaAttribute(String name, IMetaAttribute metaAttribute) {
        setMetaType(metaAttribute.getMetaType());
    }
}
