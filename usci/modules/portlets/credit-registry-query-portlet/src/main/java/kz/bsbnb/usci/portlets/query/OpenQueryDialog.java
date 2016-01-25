package kz.bsbnb.usci.portlets.query;

import com.bsbnb.vaadin.messagebox.MessageBox;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;

/**
 *
 * @author Aidar.Myrzahanov
 */
class OpenQueryDialog extends FavoritesDialog implements Button.ClickListener {

    private final OpenQueryListener listener;

    OpenQueryDialog(SqlExecutor executor, OpenQueryListener listener) {
        super(executor);
        this.listener = listener;
    }

    @Override
    protected HorizontalLayout createButtonLayout() {
        HorizontalLayout buttonLayout = super.createButtonLayout();
        Button openButton = createOpenButton();
        buttonLayout.addComponent(openButton, 0);
        buttonLayout.setComponentAlignment(openButton, Alignment.TOP_LEFT);
        return buttonLayout;
    }

    private Button createOpenButton() {
        Button openQueryButton = new Button("Open", this);
        openQueryButton.setClickShortcut(KeyCode.ENTER);
        return openQueryButton;
    }

    @Override
    public void buttonClick(Button.ClickEvent event) {
        openQuery();
    }

    private void openQuery() throws Property.ReadOnlyException, Property.ConversionException {
        Integer id = getSelectedId();
        if (id == null) {
            id = getFirstId();
        }
        if (id == null) {
            MessageBox.Show("Select a query", getApplication().getMainWindow());
            return;
        }
        loadQuery(id);
        close();
    }

    private void loadQuery(int id) throws Property.ReadOnlyException, Property.ConversionException {
        SqlExecutor executor = getExecutor();
        executor.setQueryType(QueryType.SELECT);
        executor.setUsingConnectionPool(true);
        executor.runQuery("SELECT SQL_TEXT FROM MAINTENANCE.SAVED_QUERY WHERE ID=" + id);
        ResultsTable table = executor.getQueryResultTable();
        Item textItem = table.getItem(table.getItemIds().toArray()[0]);
        Object textObject = textItem.getItemProperty(textItem.getItemPropertyIds().toArray()[0]).getValue();
        listener.openQuery(textObject.toString());
    }
}
