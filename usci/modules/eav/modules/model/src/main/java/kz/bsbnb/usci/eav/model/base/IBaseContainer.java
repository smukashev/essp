package kz.bsbnb.usci.eav.model.base;

import kz.bsbnb.usci.eav.model.base.impl.BaseContainerType;
import kz.bsbnb.usci.eav.model.meta.IMetaContainer;
import kz.bsbnb.usci.eav.model.meta.IMetaType;
import kz.bsbnb.usci.eav.model.meta.impl.MetaContainer;
import kz.bsbnb.usci.eav.model.persistable.IBaseObject;

import java.io.Serializable;
import java.util.Collection;
import java.util.Set;

/**
 * @author k.tulbassiyev
 */
public interface IBaseContainer extends IBaseObject {
    Set<String> getAttributes();

    void put(String identifier, IBaseValue value);

    Collection<IBaseValue> get();

    IBaseValue getBaseValue(String identifier);

    IMetaType getMemberType(String identifier);

    boolean isSet();

    int getValueCount();

    BaseContainerType getBaseContainerType();
}
