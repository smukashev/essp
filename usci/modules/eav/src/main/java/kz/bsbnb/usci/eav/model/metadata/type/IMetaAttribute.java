package kz.bsbnb.usci.eav.model.metadata.type;

import kz.bsbnb.usci.eav.persistance.IPersistable;

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
}
