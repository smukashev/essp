package kz.bsbnb.usci.eav.model.base;

import kz.bsbnb.usci.eav.model.meta.IMetaType;

public interface IBaseSet extends IBaseContainer {
    IMetaType getMemberType();

    IBaseSet put(IBaseValue value);

    void remove(String identifier);

    boolean isLast();

    void setLast(boolean last);

    long getCreditorId();

    void setCreditorId(long creditorId);
}
