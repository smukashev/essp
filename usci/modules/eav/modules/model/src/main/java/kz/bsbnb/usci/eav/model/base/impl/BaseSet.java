package kz.bsbnb.usci.eav.model.base.impl;

import kz.bsbnb.usci.eav.model.base.IBaseContainer;
import kz.bsbnb.usci.eav.model.base.IBaseValue;
import kz.bsbnb.usci.eav.model.meta.IMetaAttribute;
import kz.bsbnb.usci.eav.model.meta.impl.MetaClass;
import kz.bsbnb.usci.eav.model.meta.impl.MetaValue;
import kz.bsbnb.usci.eav.model.persistable.impl.Persistable;
import kz.bsbnb.usci.eav.model.meta.IMetaType;
import kz.bsbnb.usci.eav.model.type.DataTypes;
import kz.bsbnb.usci.eav.util.DateUtils;

import java.util.*;

/**
 * @author k.tulbassiyev
 */
public class BaseSet extends Persistable implements IBaseContainer
{

    private UUID uuid = UUID.randomUUID();

    /**
     * Holds data about entity structure
     * @see kz.bsbnb.usci.eav.model.meta.impl.MetaClass
     */
    private IMetaType meta;

    private Set<IBaseValue> data = new HashSet<IBaseValue>();

    /**
     * Initializes entity with a class name.
     *
     * @param meta MetaClass of the entity..
     */
    public BaseSet(IMetaType meta)
    {
        this.meta = meta;
    }

    public BaseSet(long id, IMetaType meta)
    {
        super(id);
        this.meta = meta;
    }

    public UUID getUuid() {
        return uuid;
    }

    @Override
    public IMetaType getMemberType(String name)
    {
        return meta;
    }

    public IMetaType getMemberType()
    {
        return meta;
    }

    @Override
    public void put(String name, IBaseValue value)
    {
        data.add(value);
    }

    public void put(IBaseValue value)
    {
        data.add(value);
    }

    @Override
    public Set<IBaseValue> get()
    {
        return data;
    }

    public int getElementCount()
    {
        return data.size();
    }

    @Override
    public String toString()
    {
        String str = "[";
        boolean first = true;

        for (IBaseValue value : data) {
            if (first) {
                str += value.getValue().toString();
                first = false;
            } else {
                str += ", " + value.getValue().toString();
            }
        }

        str += "]";

        return str;
    }

    public Object getElSimple(String filter)
    {
        if (meta.isComplex() || meta.isSet())
        {
            throw new IllegalArgumentException("Get simple attribute method called for complex attribute or array");
        }

        for (IBaseValue value : data)
        {
            Object innerValue = value.getValue();
            if (innerValue == null)
            {
                continue;
            }

            if (((BaseValue)value).equalsToString(filter, ((MetaValue)meta).getTypeCode()))
                return innerValue;
        }

        return null;
    }

    public Object getElComplex(String filter)
    {
        if (!meta.isComplex() || meta.isSet())
        {
            throw new IllegalArgumentException("Get complex attribute method called for simple attribute or array");
        }

        StringTokenizer tokenizer = new StringTokenizer(filter, ",");

        Object valueOut = null;
        HashMap<String, String> params = new HashMap<String, String>();

        while (tokenizer.hasMoreTokens())
        {
            String token = tokenizer.nextToken();

            StringTokenizer innerTokenizer = new StringTokenizer(token, "=");

            String fieldName = innerTokenizer.nextToken().trim();
            if (!innerTokenizer.hasMoreTokens())
                throw new IllegalStateException("Field value expected.");

            String fieldValue = innerTokenizer.nextToken().trim();

            params.put(fieldName, fieldValue);
        }

        for (IBaseValue value : data)
        {
            Object innerValue = value.getValue();
            if (innerValue == null)
            {
                continue;
            }

            if (((BaseEntity)innerValue).equalsToString(params))
                return innerValue;
        }

        return valueOut;
    }

    public Object getEl(String filter)
    {
        if (meta.isComplex())
            return getElComplex(filter);
        return getElSimple(filter);
    }

    @Override
    public void addListener(AttributeChangeListener listener) {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    @Override
    public void removeListener(AttributeChangeListener listener) {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
        {
            return true;
        }

        if (obj == null)
        {
            return false;
        }

        if (!(getClass() == obj.getClass()))
        {
            return false;
        }

        BaseSet that = (BaseSet) obj;

        IMetaType thisMetaType = this.getMemberType();
        IMetaType thatMetaType = that.getMemberType();
        if (!thisMetaType.equals(thatMetaType)) {
            return false;
        }

        boolean date = false;
        if (!thisMetaType.isSet() && !thisMetaType.isComplex())
        {
            MetaValue metaValue = (MetaValue)thisMetaType;
            if (metaValue.getTypeCode().equals(DataTypes.DATE))
            {
                date = true;
            }
        }

        if (this.getElementCount() != that.getElementCount())
        {
            return false;
        }

        Set<UUID> uuids = new HashSet<UUID>();
        Iterator<IBaseValue> thisIt = this.get().iterator();
        while (thisIt.hasNext())
        {
            IBaseValue thisBaseValue = thisIt.next();

            boolean found = false;

            Iterator<IBaseValue> thatIt = that.get().iterator();
            while (thatIt.hasNext())
            {
                IBaseValue thatBaseValue = thatIt.next();
                if (uuids.contains(thatBaseValue.getUuid()))
                {
                    continue;
                }
                else
                {
                    Object thisObject = thisBaseValue.getValue();
                    if (thisObject == null) {
                        throw new RuntimeException("Element of the set can not be equal to null.");
                    }

                    Object thatObject = thatBaseValue.getValue();
                    if (thatObject == null) {
                        throw new RuntimeException("Element of the set can not be equal to null.");
                    }

                    if (date)
                    {
                        DateUtils.toBeginningOfTheDay((Date)thisObject);
                        DateUtils.toBeginningOfTheDay((Date)thatObject);
                    }

                    if (thisObject.equals(thatObject))
                    {
                        uuids.add(thatBaseValue.getUuid());
                        found = true;
                    }
                }
            }

            if (!found)
            {
                return false;
            }
        }

        return true;
    }

}
