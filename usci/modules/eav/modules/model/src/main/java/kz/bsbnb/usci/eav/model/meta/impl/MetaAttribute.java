package kz.bsbnb.usci.eav.model.meta.impl;

import kz.bsbnb.usci.eav.model.meta.IMetaAttribute;
import kz.bsbnb.usci.eav.model.meta.IMetaType;
import kz.bsbnb.usci.eav.model.persistable.impl.Persistable;

/**
 * @author k.tulbassiyev
 */
public class MetaAttribute extends Persistable implements IMetaAttribute
{
    IMetaType metaType;

    /**
     * <code>true</code> if attribute is a key attribute (used by DAO to find persisted entity)
     * if an attribute has type DataTypes.COMPLEX then all it's key values will be used
     * Defaults to <code>false</code>
     */
    private boolean isKey = false;

    private boolean isFinal = false;

    private boolean isRequired = false;

    private boolean immutable = false;

    private String name = "";
    /**
     * <code>true</code> if attribute can have <code>null</code> value
     * key attributes have this flag always set to false
     * Defaults to <code>true</code>
     */
    private boolean isNullable = true;

    private String title;

    public MetaAttribute(boolean isKey, boolean isNullable)
    {
        this.isKey = isKey;
        this.isNullable = isNullable && !isKey;
    }

    public MetaAttribute(boolean isKey, boolean isNullable, IMetaType metaType)
    {
        this.isKey = isKey;
        this.isNullable = isNullable && !isKey;
        this.metaType = metaType;
    }

    public MetaAttribute(IMetaType metaType)
    {
        this.isKey = false;
        this.isNullable = true;
        this.metaType = metaType;
    }

    public MetaAttribute(long id, boolean isKey, boolean isNullable)
    {
        super(id);
        this.isKey = isKey;
        this.isNullable = isNullable && !isKey;
    }

    @Override
    public boolean isKey()
    {
        return isKey;
    }

    public void setKey(boolean isKey)
    {
        this.isKey = isKey;
        this.isNullable = isNullable && !isKey;
    }

    @Override
    public boolean isNullable()
    {
        return isNullable;
    }

    public void setNullable(boolean isNullable)
    {
        this.isNullable = isNullable && !isKey;
    }

    @Override
    public IMetaType getMetaType()
    {
        return metaType;
    }

    public void setMetaType(IMetaType metaType)
    {
        this.metaType = metaType;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        MetaAttribute that = (MetaAttribute) o;

        if (isKey != that.isKey) return false;
        if (isNullable != that.isNullable) return false;
        //if (!metaType.equals(that.metaType)) return false;

        if (metaType.isSet() && metaType.isComplex()) {
            MetaSet thisSet = (MetaSet)metaType;
            MetaSet thatSet = (MetaSet)(that.getMetaType());

            if (thisSet.getArrayKeyFilter().size() != thatSet.getArrayKeyFilter().size()) {
                return false;
            } else {
                for (String attrName : thisSet.getArrayKeyFilter().keySet()) {
                    for(String value : thatSet.getArrayKeyFilter().get(attrName)) {
                        if (value == null || !value.equals(thisSet.getArrayKeyFilter().get(attrName))) {
                            return false;
                        }
                    }
                }
            }

            return true;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = super.hashCode();
        result = 31 * result + metaType.hashCode();
        result = 31 * result + (isKey ? 1 : 0);
        result = 31 * result + (isNullable ? 1 : 0);
        return result;
    }

    public String getTitle()
    {
        if(title != null)
            return title;
        return getName();
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    public boolean isFinal()
    {
        return isFinal;
    }

    public void setFinal(boolean aFinal)
    {
        isFinal = aFinal;
    }

    public boolean isRequired() {
        return isRequired;
    }

    public void setRequired(boolean isRequired) {
        this.isRequired = isRequired;
    }

    public boolean isImmutable()
    {
        return immutable;
    }

    public void setImmutable(boolean immutable)
    {
        this.immutable = immutable;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }
}
