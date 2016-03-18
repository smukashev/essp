package kz.bsbnb.usci.eav.model.meta.impl;

import kz.bsbnb.usci.eav.model.meta.IMetaValue;
import kz.bsbnb.usci.eav.model.type.DataTypes;

public class MetaValue implements IMetaValue {
    private DataTypes typeCode;

    private boolean immutable = false;

    private boolean reference = false;

    public MetaValue() {
        super();
    }

    public MetaValue(DataTypes typeCode) {
        this.typeCode = typeCode;
    }

    public DataTypes getTypeCode() {
        return typeCode;
    }

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
            return tmp.getTypeCode() == this.getTypeCode();
        }
    }

    @Override
    public int hashCode() {
        return typeCode.hashCode();
    }

    @Override
    public boolean isSet() {
        return false;
    }

    @Override
    public boolean isComplex() {
        return false;
    }

    @Override
    public String toString(String prefix) {
        return "metaValue: " + typeCode;
    }

    @Override
    public String toJava(String prefix) {
        return "new MetaValue(DataTypes." + typeCode + ")";
    }

    @Override
    public boolean isReference() {
        return reference;
    }

    @Override
    public void setReference(boolean value) {
        reference = value;
    }
}
