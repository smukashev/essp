package kz.bsbnb.usci.portlets.query;

import com.bsbnb.vaadin.messagebox.MessageBox;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.data.util.filter.SimpleStringFilter;
import com.vaadin.data.util.filter.UnsupportedFilterException;
import com.vaadin.event.FieldEvents;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import java.util.Iterator;

/**
 *
 * @author Aidar.Myrzahanov
 */
class FavoritesDialog extends Window implements FieldEvents.TextChangeListener {

    private static final String SAVED_QUERIES_SQL = "SELECT ID, NAME FROM MAINTENANCE.SAVED_QUERY ORDER BY ID";
    private static final String REMOVE_QUERY_SQL = "DELETE FROM MAINTENANCE.SAVED_QUERY WHERE ID = %s";

    private final SqlExecutor executor;

    private ResultsTable queriesTable;
    private TextField filterField;
    private VerticalLayout mainLayout;

    FavoritesDialog(SqlExecutor executor) {
        this.executor = executor;
        setModal(true);
        setWidth("500px");
    }

    @Override
    public void attach() {
        queriesTable = getSavedQueriesTable();
        filterField = createFilterField();
        addComponent(createMainLayout());
        filterField.focus();
    }

    private VerticalLayout createMainLayout() {
        mainLayout = new VerticalLayout();
        mainLayout.addComponent(filterField);
        mainLayout.addComponent(queriesTable);
        HorizontalLayout buttonLayout = createButtonLayout();
        buttonLayout.setWidth("100%");
        mainLayout.addComponent(buttonLayout);
        return mainLayout;
    }

    protected HorizontalLayout createButtonLayout() {
        HorizontalLayout buttonLayout = new HorizontalLayout();
        Button removeButton = new Button("Remove", new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                removeSelectedQuery();
            }
        });
        buttonLayout.addComponent(removeButton);
        buttonLayout.setComponentAlignment(removeButton, Alignment.TOP_RIGHT);
        return buttonLayout;
    }

    private void removeSelectedQuery() {
        Integer queryId = getSelectedId();
        if (queryId == null) {
            MessageBox.Show("Select a query", getApplication().getMainWindow());
            return;
        }
        executor.setQueryType(QueryType.INSERT_OR_UPDATE);
        executor.setUsingConnectionPool(true);
        executor.runQuery(String.format(REMOVE_QUERY_SQL, queryId));
        updateTable();
    }

    private void updateTable() {
        mainLayout.removeComponent(queriesTable);
        queriesTable = getSavedQueriesTable();
        mainLayout.addComponent(queriesTable, 1);
    }

    private ResultsTable getSavedQueriesTable() {
        executor.setQueryType(QueryType.SELECT);
        executor.setUsingConnectionPool(true);
        executor.runQuery(SAVED_QUERIES_SQL);
        ResultsTable resultsTable = executor.getQueryResultTable();
        resultsTable.setMultiSelect(false);
        resultsTable.setSelectable(true);
        return resultsTable;
    }

    private TextField createFilterField() {
        TextField filterField = new TextField(null, "");
        filterField.setWidth("100%");
        filterField.addListener(this);
        filterField.setImmediate(true);
        return filterField;
    }

    @Override
    public void textChange(FieldEvents.TextChangeEvent event) {
        filterTable(event.getText());
    }

    private void filterTable(String filterText) throws UnsupportedFilterException, Property.ConversionException, Property.ReadOnlyException {
        if (!(queriesTable.getContainerDataSource() instanceof IndexedContainer)) {
            return;
        }
        IndexedContainer container = (IndexedContainer) queriesTable.getContainerDataSource();
        container.removeAllContainerFilters();
        container.addContainerFilter(new SimpleStringFilter("NAME", filterText, true, false));
    }

    private Integer getItemId(Item item) {
        if (item == null) {
            return null;
        }
        return ((Number) item.getItemProperty("ID").getValue()).intValue();
    }

    protected Integer getSelectedId() {
        return getItemId(getSelectedItem());
    }

    private Item getSelectedItem() {
        return queriesTable.getItem(queriesTable.getValue());
    }

    protected Integer getFirstId() {
        return getItemId(getFirstItem());
    }

    private Item getFirstItem() {
        Iterator<?> idsIterator = queriesTable.getItemIds().iterator();
        if (!idsIterator.hasNext()) {
            return null;
        }
        return queriesTable.getItem(idsIterator.next());
    }

    protected SqlExecutor getExecutor() {
        return executor;
    }

    protected String getFilterText() {
        return (String) filterField.getValue();
    }

}
