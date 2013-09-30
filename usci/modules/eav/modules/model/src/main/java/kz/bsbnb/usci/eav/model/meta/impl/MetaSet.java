package kz.bsbnb.usci.eav.model.meta.impl;

import kz.bsbnb.usci.eav.model.base.ContainerTypes;
import kz.bsbnb.usci.eav.model.meta.IMetaAttribute;
import kz.bsbnb.usci.eav.model.meta.IMetaContainer;
import kz.bsbnb.usci.eav.model.meta.IMetaSet;
import kz.bsbnb.usci.eav.model.meta.IMetaType;
import kz.bsbnb.usci.eav.model.persistable.impl.Persistable;
import kz.bsbnb.usci.eav.model.type.ComplexKeyTypes;
import kz.bsbnb.usci.eav.model.type.DataTypes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MetaSet  extends Persistable implements IMetaType, IMetaSet
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

    HashMap<String, ArrayList<String>> arrayKeyFilter = new HashMap<String, ArrayList<String>>();

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
                String str = "metaSet(" + getId() + ")[";

                boolean first = true;
                for (String attrName : arrayKeyFilter.keySet()) {
                    if (!first) {
                        str += ", " + attrName + ":" + arrayKeyFilter.get(attrName);
                    } else {
                        str += attrName + ":" + arrayKeyFilter.get(attrName);
                        first = false;
                    }
                }

                str += "], " + metaType.toString(prefix);

                return str;
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
    public String toJava(String prefix) {
        if(isComplex()) {
            if (isSetOfSets()) {
                return "\n  metaSet(" + getId() + "), complex, setOfSets";
            }
            else {
                String str =  "\n ";


                str += " " + metaType.toJava(prefix);

                return str;
            }
        }
        else {
            if (isSetOfSets()) {
                return "+new MetaSet("+metaType.toJava(prefix);
            }
            else {
                return " new MetaSet(" + metaType.toJava(prefix)+")";
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

    public HashMap<String, ArrayList<String>> getArrayKeyFilter()
    {
        return arrayKeyFilter;
    }

    public void addArrayKeyFilter(String attributeName, String value) {
        ArrayList<String> valHolder = arrayKeyFilter.get(attributeName);
        if (valHolder != null) {
            valHolder.add(value);
        } else {
            valHolder = new ArrayList<String>();

            valHolder.add(value);

            arrayKeyFilter.put(attributeName, valHolder);
        }
    }

    public void recursiveSet(MetaClass subMeta) {
        if (metaType.isSet()) {
            ((MetaSet)metaType).recursiveSet(subMeta);
        } else if (metaType.isComplex()) {
            if (((MetaClass)metaType).getClassName().equals(subMeta.getClassName())) {
                metaType = subMeta;
            } else {
                ((MetaClass)metaType).recursiveSet(subMeta);
            }
        }
    }

    public List<String> getAllPaths(MetaClass subMeta, String path) {
        ArrayList<String> paths = new ArrayList<String>();

        if (metaType.isSet()) {
            paths.addAll(((MetaSet)metaType).getAllPaths(subMeta, path));
        } else if (metaType.isComplex()) {
            if (((MetaClass)metaType).getClassName().equals(subMeta.getClassName())) {
                paths.add(path);
            } else {
                paths.addAll(((MetaClass)metaType).getAllPaths(subMeta, path));
            }
        }

        return paths;
    }
}
