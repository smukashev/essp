package kz.bsbnb.usci.eav.model.base.impl;

import kz.bsbnb.usci.eav.model.base.IBaseContainer;
import kz.bsbnb.usci.eav.model.base.IBaseValue;
import kz.bsbnb.usci.eav.model.persistable.impl.Persistable;
import kz.bsbnb.usci.eav.model.meta.IMetaType;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

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
}
