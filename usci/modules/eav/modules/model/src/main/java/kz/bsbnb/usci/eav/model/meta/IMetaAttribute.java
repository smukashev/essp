package kz.bsbnb.usci.eav.model.meta;

import kz.bsbnb.usci.eav.model.persistable.IPersistable;

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
    public void setMetaType(IMetaType metaType);

    public String getTitle();

    public boolean isFinal();
    public void setFinal(boolean isFinal);

    public boolean isImmutable();

    public void setImmutable(boolean immutable);
}
