package kz.bsbnb.usci.eav.model.meta.impl;

import kz.bsbnb.usci.eav.model.meta.IMetaAttribute;
import kz.bsbnb.usci.eav.model.meta.IMetaSet;
import kz.bsbnb.usci.eav.model.meta.IMetaType;
import kz.bsbnb.usci.eav.model.type.ComplexKeyTypes;
import kz.bsbnb.usci.eav.model.type.DataTypes;
import kz.bsbnb.usci.eav.util.Errors;
import org.apache.commons.lang.NotImplementedException;

import java.util.ArrayList;
import java.util.List;

public class MetaSet extends MetaContainer implements IMetaSet {
    IMetaType metaType;

    private boolean reference = false;

    public MetaSet() {
        super(MetaContainerTypes.META_SET);
    }

    public MetaSet(IMetaType metaType) {
        super(MetaContainerTypes.META_SET);

        if (metaType == null) {
            throw new IllegalArgumentException(Errors.getMessage(Errors.E46));
        }
        this.metaType = metaType;
    }

    ComplexKeyTypes arrayKeyType = ComplexKeyTypes.ALL;

    public ComplexKeyTypes getArrayKeyType() {
        return arrayKeyType;
    }

    public void setArrayKeyType(ComplexKeyTypes arrayKeyType) {
        this.arrayKeyType = arrayKeyType;
    }

    public boolean isSet() {
        return true;
    }

    @Override
    public boolean isComplex() {
        return metaType.isComplex();
    }

    public void setMetaType(IMetaType metaType) {
        if (metaType == null) {
            throw new IllegalArgumentException(Errors.getMessage(Errors.E46));
        }
        this.metaType = metaType;
    }

    public DataTypes getTypeCode() {
        if (isComplex())
            throw new NotImplementedException(Errors.getMessage(Errors.E2));

        if (metaType.isSet())
            throw new NotImplementedException(Errors.getMessage(Errors.E2));

        return ((MetaValue) metaType).getTypeCode();
    }

    public IMetaType getMemberType() {
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
        if (metaAttribute.getMetaType() == null) {
            throw new IllegalArgumentException(Errors.getMessage(Errors.E46));
        }
        setMetaType(metaAttribute.getMetaType());
    }

    @Override
    public int getType() {
        return MetaContainerTypes.META_SET;
    }

    public String toString(String prefix) {
        if (isComplex()) {
            String str = "metaSet(" + getId() + ")[";

            str += "], " + metaType.toString(prefix);

            return str;
        } else {
            return "metaSet(" + getId() + "), " + metaType.toString(prefix);
        }
    }

    @Override
    public String toJava(String prefix) {
        if (isComplex()) {
            String str = "\n ";
            str += " " + metaType.toJava(prefix);
            return str;
        } else {
            return " new MetaSet(" + metaType.toJava(prefix) + ")";
        }
    }

    @Override
    public boolean isReference() {
        return reference;
    }

    @Override
    public void setReference(boolean value) {
        reference = value;
    }

    public void recursiveSet(MetaClass subMeta) {
        if (metaType.isSet()) {
            ((MetaSet) metaType).recursiveSet(subMeta);
        } else if (metaType.isComplex()) {
            if (((MetaClass) metaType).getClassName().equals(subMeta.getClassName())) {
                metaType = subMeta;
            } else {
                ((MetaClass) metaType).recursiveSet(subMeta);
            }
        }
    }

    public List<String> getAllPaths(MetaClass subMeta, String path) {
        ArrayList<String> paths = new ArrayList<>();

        if (metaType.isSet()) {
            paths.addAll(((MetaSet) metaType).getAllPaths(subMeta, path));
        } else if (metaType.isComplex()) {
            if (((MetaClass) metaType).getClassName().equals(subMeta.getClassName())) {
                paths.add(path);
            } else {
                paths.addAll(((MetaClass) metaType).getAllPaths(subMeta, path));
            }
        }

        return paths;
    }
}
