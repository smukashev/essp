package kz.bsbnb.usci.eav_model.model.base;

import kz.bsbnb.usci.eav_model.model.meta.IMetaType;

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
