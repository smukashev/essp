package kz.bsbnb.usci.eav.model.searchForm.impl;

import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;
import kz.bsbnb.usci.eav.model.searchForm.ISearchResult;

import java.io.Serializable;
import java.util.List;

public abstract class AbstractSearchResult implements ISearchResult, Serializable {

    private static final long serialVersionUID = 1L;

    List<BaseEntity> data;

    public List<BaseEntity> getData() {
        return data;
    }

    public void setData(List<BaseEntity> data) {
        this.data = data;
    }
}
