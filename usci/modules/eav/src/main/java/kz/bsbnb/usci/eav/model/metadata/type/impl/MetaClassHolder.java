package kz.bsbnb.usci.eav.model.metadata.type.impl;

import kz.bsbnb.usci.eav.persistance.Persistable;

import java.util.Set;

/**
 *
 */
public class MetaClassHolder extends AbstractMetaType {

    MetaClass meta;

    public MetaClassHolder(long id, MetaClass meta, boolean isKey, boolean isNullable) {
        super(id, isKey, isNullable);
        this.meta = meta;
    }

    public MetaClassHolder(MetaClass meta, boolean isKey, boolean isNullable) {
        super(isKey, isNullable);
        this.meta = meta;
    }

    public MetaClassHolder(String className, boolean isKey, boolean isNullable) {
        super(isKey, isNullable);
        this.meta = new MetaClass(className);
    }

    public MetaClassHolder(MetaClass meta) {
        super();
        this.meta = meta;
    }

    public MetaClassHolder(String className) {
        super();
        this.meta = new MetaClass(className);
    }

    @Override
    public boolean isArray() {
        return false;
    }

    @Override
    public boolean isComplex() {
        return true;
    }

    public MetaClass getMeta() {
        return meta;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;

        if (obj == null)
            return false;

        if (!(getClass() == obj.getClass()))
            return false;
        else {
            MetaClassHolder tmp = (MetaClassHolder) obj;

            if(!(this.getMeta().equals(tmp.getMeta())))
                return false;

            return !(tmp.isKey() != this.isKey() ||
                    tmp.isNullable() != this.isNullable());

        }
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (meta != null ? meta.hashCode() : 0);
        return result;
    }

}
