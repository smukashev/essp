package kz.bsbnb.usci.eav.model.searchForm;

import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;

public interface ISearchResult extends Serializable, Cloneable {
    Iterator<BaseEntity> iterator();
    public boolean hasPagination();
    public int getTotalCount();
    public SearchPagination getPagination();
    public void setPagination(SearchPagination searchPagination);
    public void setTotalCount(int totalCount);
    public void setData(List<BaseEntity> data);
    public List<BaseEntity> getData();
}
