package kz.bsbnb.usci.eav.model.base.impl;

import kz.bsbnb.usci.eav.model.base.IBaseContainer;
import kz.bsbnb.usci.eav.model.persistable.impl.BaseObject;

import java.util.HashSet;
import java.util.Set;

public abstract class BaseContainer extends BaseObject implements IBaseContainer {
    private BaseContainerType baseContainerType;

    protected long creditorId;

    public BaseContainer(BaseContainerType baseContainerType) {
        this.baseContainerType = baseContainerType;
    }

    public BaseContainer(long id, BaseContainerType baseContainerType) {
        super(id);
        this.baseContainerType = baseContainerType;
    }

    @Override
    public BaseContainerType getBaseContainerType() {
        return baseContainerType;
    }
}
