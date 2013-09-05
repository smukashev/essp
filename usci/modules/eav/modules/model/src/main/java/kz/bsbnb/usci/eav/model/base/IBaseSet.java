package kz.bsbnb.usci.eav.model.base;

import kz.bsbnb.usci.eav.model.meta.IMetaType;

/**
 *
 */
public interface IBaseSet extends IBaseContainer {

    public IMetaType getMemberType();

    public IBaseSet put(IBaseValue value);

    public void remove(String identifier);

    public int getElementCount();

}
