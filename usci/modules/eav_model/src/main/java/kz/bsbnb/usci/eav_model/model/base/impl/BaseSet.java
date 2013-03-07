package kz.bsbnb.usci.eav_model.model.base.impl;

import kz.bsbnb.usci.eav_model.model.base.IBaseContainer;
import kz.bsbnb.usci.eav_model.model.base.IBaseValue;
import kz.bsbnb.usci.eav_model.model.persistable.impl.Persistable;
import kz.bsbnb.usci.eav_model.model.meta.IMetaType;

import java.util.HashSet;
import java.util.Set;

/**
 * @author k.tulbassiyev
 */
public class BaseSet extends Persistable implements IBaseContainer
{

    /**
     * Holds data about entity structure
     * @see kz.bsbnb.usci.eav_model.model.meta.impl.MetaClass
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
}
