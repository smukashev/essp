package kz.bsbnb.usci.eav.model.searchForm.impl;

import kz.bsbnb.usci.eav.Errors;
import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;
import kz.bsbnb.usci.eav.model.searchForm.SearchPagination;
import kz.bsbnb.usci.eav.model.searchForm.ISearchResult;

import java.util.Iterator;

public class NonPaginableSearchResult extends AbstractSearchResult implements ISearchResult {

    private static final long serialVersionUID = 13282783418L;

    @Override
    public Iterator<BaseEntity> iterator() {
        return data.iterator();
    }

    @Override
    public boolean hasPagination() {
        return false;
    }

    @Override
    public int getTotalCount() {
        return data.size();
    }

    @Override
    public SearchPagination getPagination() {
        throw new UnsupportedOperationException(String.valueOf(Errors.E47));
    }

    @Override
    public void setTotalCount(int totalCount) {
        throw new UnsupportedOperationException(String.valueOf(Errors.E48));
    }

    @Override
    public void setPagination(SearchPagination searchPagination) {
        throw new UnsupportedOperationException(String.valueOf(Errors.E48));
    }
}
