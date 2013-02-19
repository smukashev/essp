package kz.bsbnb.usci.eav.model.metadata.type.impl;

import kz.bsbnb.usci.eav.model.metadata.type.IMetaAttribute;
import kz.bsbnb.usci.eav.model.metadata.type.IMetaType;
import kz.bsbnb.usci.eav.persistance.Persistable;

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
    /**
     * <code>true</code> if attribute can have <code>null</code> value
     * key attributes have this flag always set to false
     * Defaults to <code>true</code>
     */
    private boolean isNullable = true;

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
}
