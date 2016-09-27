package com.bsbnb.vaadin.paged.table.control;

import com.vaadin.data.util.BeanItemContainer;
import java.util.ArrayList;

/**
 *
 * @author Aidar.Myrzahanov
 */
class PagingHandler<T> {

    private final PagedDataProvider<T> provider;
    private final BeanItemContainer<T> container;
    private final int pageSize;

    private final ArrayList<PageChangedListener> pageChangeListeners = new ArrayList<PageChangedListener>();
    private int currentPage = -1;
    private int pagesCount = 1;

    public PagingHandler(PagedDataProvider<T> provider, int pageSize, Class<? super T> entityClass) {
        this.provider = provider;
        this.pageSize = pageSize;
        container = new BeanItemContainer<T>(entityClass);
    }

    public void reload() {
        currentPage = -1;
        pagesCount = retrievePagesCount();
        setPage(0);
    }

    private int retrievePagesCount() {
        int recordsCount = provider.getCount();
        int count = recordsCount / pageSize;
        return recordsCount % pageSize == 0 && recordsCount > 0 ? count : count + 1;
    }

    public void setPage(int pageIndex) {
        if (!isPageValid(pageIndex)) {
            return;
        }
        if (pageIndex == currentPage) {
            return;
        }
        currentPage = pageIndex;
        loadData();
        firePageChanged(pageIndex);
    }

    private boolean isPageValid(int page) {
        return page >= 0 && page < pagesCount;
    }

    private void loadData() {
        container.removeAllItems();
        container.addAll(provider.getRecords(currentPage * pageSize, pageSize));
    }

    private void firePageChanged(int pageIndex) {
        for (PageChangedListener listener : pageChangeListeners) {
            listener.pageChanged(pageIndex, pagesCount);
        }
    }

    public BeanItemContainer<T> getContainer() {
        return container;
    }

    public int getPagesCount() {
        return pagesCount;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public void addListener(PageChangedListener listener) {
        pageChangeListeners.add(listener);
    }
}
