package kz.bsbnb.usci.eav.model.base;


import kz.bsbnb.usci.eav.model.Batch;
import kz.bsbnb.usci.eav.model.meta.IMetaAttribute;
import kz.bsbnb.usci.eav.model.meta.IMetaType;
import kz.bsbnb.usci.eav.model.persistable.IPersistable;

import java.util.Date;
import java.util.UUID;

/**
 * Actual value placeholder used in BaseEntity.
 *
 * @see kz.bsbnb.usci.eav.model.base.impl.BaseEntity
 *
 * @author a.motov
 */
public interface IBaseValue<T> extends IPersistable, Cloneable
{

    public IBaseContainer getBaseContainer();

    public void setBaseContainer(IBaseContainer baseContainer);

    public IMetaAttribute getMetaAttribute();

    public void setMetaAttribute(IMetaAttribute metaAttribute);

    /**
     * Returns the <code>Batch</code> that contains information about the origin of this value.
     * @return <code>Batch</code> that contains information about the origin of this value.
     */
    public Batch getBatch();

    public void setBatch(Batch batch);

    /**
     * Returns the index of the value.
     * @return the index
     */
    public long getIndex();

    /**
     * Sets the index values.
     * @param index the index of value to set.
     */
    public void setIndex(long index);

    /**
     * Return the value. The value can be a simple type, an array or a complex type.
     * @return value the value. May be is null.
     *
     * @see kz.bsbnb.usci.eav.model.type.DataTypes
     * @see kz.bsbnb.usci.eav.model.base.impl.BaseEntity
     */
    public T getValue();

    public void setValue(T value);

    public Date getRepDate();

    public void setRepDate(Date reportDate);

    public void setLast(boolean last);

    public boolean isLast();

    public void setClosed(boolean closed);

    public boolean isClosed();

    public void setNewBaseValue(IBaseValue baseValue);

    public IBaseValue getNewBaseValue();

    public UUID getUuid();

    public boolean equalsByValue(IBaseValue baseValue);

    public boolean equalsByValue(IMetaType metaType, IBaseValue baseValue);

}
