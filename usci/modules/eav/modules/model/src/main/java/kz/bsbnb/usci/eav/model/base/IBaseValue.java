package kz.bsbnb.usci.eav.model.base;


import kz.bsbnb.usci.eav.model.Batch;
import kz.bsbnb.usci.eav.model.persistable.IPersistable;

import java.sql.Date;
import java.util.UUID;

/**
 * Actual value placeholder used in BaseEntity.
 *
 * @see kz.bsbnb.usci.eav.model.base.impl.BaseEntity
 *
 * @author a.motov
 */
public interface IBaseValue extends IPersistable, Cloneable
{
    /**
     * Returns the <code>Batch</code> that contains information about the origin of this value.
     * @return <code>Batch</code> that contains information about the origin of this value.
     */
    public Batch getBatch();

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
    public Object getValue();

    public void setValue(Object value);

    public Date getRepDate();

    public void setLast(boolean last);

    public boolean isLast();

    public void setClosed(boolean closed);

    public boolean isClosed();

    public UUID getUuid();

}
