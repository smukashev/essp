package kz.bsbnb.usci.eav.model.searchForm.impl;

import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;
import kz.bsbnb.usci.eav.model.searchForm.SearchPagination;
import kz.bsbnb.usci.eav.model.searchForm.ISearchResult;

import java.util.Iterator;

public class PaginableSearchResult extends AbstractSearchResult implements ISearchResult {

    private static final long serialVersionUID = 1L;

    SearchPagination pagination;

    @Override
    public Iterator<BaseEntity> iterator() {
        return data.iterator();
    }

    @Override
    public boolean hasPagination() {
        return true;
    }

    @Override
    public int getTotalCount() {
        return pagination.getTotalCount();
    }

    @Override
    public SearchPagination getPagination() {
        return pagination;
    }

    @Override
    public void setTotalCount(int totalCount) {
        pagination = new SearchPagination(totalCount);
    }

    @Override
    public void setPagination(SearchPagination searchPagination) {
        this.pagination = searchPagination;
    }
}
