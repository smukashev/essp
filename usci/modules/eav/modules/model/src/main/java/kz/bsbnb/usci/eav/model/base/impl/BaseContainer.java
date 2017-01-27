package kz.bsbnb.usci.eav.model.base.impl;

import kz.bsbnb.usci.eav.model.base.IBaseContainer;
import kz.bsbnb.usci.eav.model.persistable.impl.BaseObject;

import java.util.HashSet;
import java.util.Set;

public abstract class BaseContainer extends BaseObject implements IBaseContainer {
    private BaseContainerType baseContainerType;

    protected long creditorId;

    protected Long userId;

    protected Long batchId;

    protected Long index;

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
    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    @Override
    public Long getBatchId() {
        return batchId;
    }

    public void setBatchId(Long batchId) {
        this.batchId = batchId;
    }

    public Long getBatchIndex() {
        return index;
    }

    public void setBatchIndex(Long index) {
        this.index = index;
    }
}
