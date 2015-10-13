package kz.bsbnb.usci.eav.model.base;

import kz.bsbnb.usci.eav.model.meta.IMetaType;

public interface IBaseSet extends IBaseContainer {
    IMetaType getMemberType();

    IBaseSet put(IBaseValue value);

    void remove(String identifier);

    long getLevel();

    void setLevel(long level);

    boolean isLast();

    void setLast(boolean last);
}
