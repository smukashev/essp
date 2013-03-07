package kz.bsbnb.usci.eav_model.model.meta;

import kz.bsbnb.usci.eav_model.model.persistable.IPersistable;

/**
 * @author k.tulbassiyev
 */
public interface IMetaAttribute extends IPersistable
{
    /**
     *
     * @return <code>true</code>, when attribute is a key attribute
     */
    public boolean isKey();

    /**
     *
     * @return <code>true</code>, when attribute can have null value
     */
    public boolean isNullable();

    public IMetaType getMetaType();

    public void setKey(boolean isKey);
    public void setNullable(boolean isNullable);
}
