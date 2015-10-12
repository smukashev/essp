package kz.bsbnb.usci.eav.model.base.impl;

import kz.bsbnb.usci.eav.model.base.IBaseContainer;
import kz.bsbnb.usci.eav.model.persistable.impl.BaseObject;

import java.util.HashSet;
import java.util.Set;

public abstract class BaseContainer extends BaseObject implements IBaseContainer {
    private BaseContainerType baseContainerType;
    private Set<String> modifiedIdentifiers = new HashSet<String>();

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

    @Override
    public void setBaseContainerType(BaseContainerType baseContainerType) {
        this.baseContainerType = baseContainerType;
    }
}
