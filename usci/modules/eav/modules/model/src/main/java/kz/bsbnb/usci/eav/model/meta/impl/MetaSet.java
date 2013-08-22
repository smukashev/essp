package kz.bsbnb.usci.eav.model.meta.impl;

import kz.bsbnb.usci.eav.model.base.ContainerTypes;
import kz.bsbnb.usci.eav.model.meta.IMetaAttribute;
import kz.bsbnb.usci.eav.model.meta.IMetaContainer;
import kz.bsbnb.usci.eav.model.meta.IMetaType;
import kz.bsbnb.usci.eav.model.persistable.impl.Persistable;
import kz.bsbnb.usci.eav.model.type.ComplexKeyTypes;
import kz.bsbnb.usci.eav.model.type.DataTypes;

public class MetaSet  extends Persistable implements IMetaType, IMetaContainer
{
    IMetaType metaType;

    private boolean immutable = false;

    private boolean reference = false;

    public MetaSet()
    {
    }

    public MetaSet(IMetaType metaType)
    {
        if (metaType == null)
        {
            throw new IllegalArgumentException("MetaType can not be null.");
        }
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

    public boolean isSet()
    {
        return true;
    }

    @Override
    public boolean isComplex()
    {
        return metaType.isComplex();
    }

    @Override
    public boolean isSetOfSets() {
        return metaType.isSet();
    }

    public void setMetaType(IMetaType metaType)
    {
        if (metaType == null)
        {
            throw new IllegalArgumentException("MetaType can not be null.");
        }
        this.metaType = metaType;
    }

    public DataTypes getTypeCode()
    {
        if(isComplex())
            throw new IllegalStateException();

        if(metaType.isSet())
            throw new IllegalStateException();

        return ((MetaValue) metaType).getTypeCode();
    }

    public IMetaType getMemberType()
    {
        return metaType;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (!(o instanceof MetaSet)) return false;

        MetaSet metaSet = (MetaSet) o;

        if (arrayKeyType != metaSet.arrayKeyType) return false;
        if (!metaType.equals(metaSet.metaType)) return false;

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = metaType.hashCode();
        result = 31 * result + (arrayKeyType != null ? arrayKeyType.hashCode() : 0);
        return result;
    }

    @Override
    public void setMetaAttribute(String name, IMetaAttribute metaAttribute)
    {
        if (metaAttribute.getMetaType() == null)
        {
            throw new IllegalArgumentException("MetaType can not be null.");
        }
        setMetaType(metaAttribute.getMetaType());
    }

    @Override
    public int getType()
    {
        return ContainerTypes.SET;
    }

    public String toString(String prefix)
    {
        if(isComplex()) {
            if (isSetOfSets()) {
                return "metaSet(" + getId() + "), complex, setOfSets";
            }
            else {
                return "metaSet(" + getId() + "), " + metaType.toString(prefix);
            }
        }
        else {
            if (isSetOfSets()) {
                return "metaSet(" + getId() + "), simple, setOfSets";
            }
            else {
                return "metaSet(" + getId() + "), " + metaType.toString(prefix);
            }
        }
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
