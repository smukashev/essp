package kz.bsbnb.usci.eav.model.base;

import kz.bsbnb.usci.eav.model.meta.IMetaType;

/**
 *
 */
public interface IBaseSet extends IBaseContainer {

    public IMetaType getMemberType();

    public IBaseSet put(IBaseValue value);

    public void remove(String identifier);

    public long getLevel();

    public void setLevel(long level);

    public boolean isLast();

    public void setLast(boolean last);

}
