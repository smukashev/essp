package kz.bsbnb.usci.eav.model;

import kz.bsbnb.usci.eav.model.batchdata.IBaseValue;
import kz.bsbnb.usci.eav.model.metadata.type.IMetaType;

import java.util.Set;

/**
 * @author k.tulbassiyev
 */
public interface IBaseContainer
{
    public void put(String name, IBaseValue value);

    public Set<IBaseValue> get();

    public IMetaType getMemberType(String name);
}
