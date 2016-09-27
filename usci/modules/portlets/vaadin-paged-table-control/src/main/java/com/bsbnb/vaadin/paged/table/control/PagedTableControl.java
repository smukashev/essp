package com.bsbnb.vaadin.paged.table.control;

import com.bsbnb.vaadin.formattedtable.FormattedTable;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.VerticalLayout;

/**
 *
 * @author Aidar.Myrzahanov
 */
public class PagedTableControl<T> extends VerticalLayout {

    private final PagingHandler<T> handler;
    private final PageControl<T> pageControl;
    private final FormattedTable table = new FormattedTable(null);

    public PagedTableControl(Class<? super T> entityClass, int pageSize, PagedDataProvider<T> provider) {
        handler = new PagingHandler<T>(provider, pageSize, entityClass);
        pageControl = new PageControl<T>(handler);
        buildLayout(handler.getContainer());
    }

    private void buildLayout(BeanItemContainer<T> container) {
        table.setContainerDataSource(container);
        table.setWidth("100%");
        table.setImmediate(true);
        addComponent(table);
        addComponent(pageControl);
        setComponentAlignment(pageControl, Alignment.MIDDLE_CENTER);
        setSpacing(false);
    }

    public FormattedTable getTable() {
        return table;
    }

    public BeanItemContainer<T> getContainer() {
        return handler.getContainer();
    }

    public void reload() {
        handler.reload();
    }

}
