package kz.bsbnb.usci.eav.model.base;

import kz.bsbnb.usci.eav.model.meta.IMetaType;

import java.io.Serializable;
import java.util.Set;

/**
 * @author k.tulbassiyev
 */
public interface IBaseContainer extends Serializable
{
    public void put(String name, IBaseValue value);

    public Set<IBaseValue> get();

    public IMetaType getMemberType(String name);
}
