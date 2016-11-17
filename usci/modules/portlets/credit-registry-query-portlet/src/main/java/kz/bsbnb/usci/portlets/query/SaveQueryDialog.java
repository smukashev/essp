package kz.bsbnb.usci.portlets.query;

import com.bsbnb.vaadin.messagebox.MessageBox;
import com.vaadin.event.ShortcutAction;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;

import java.util.logging.Level;

/**
 *
 * @author Aidar.Myrzahanov
 */
class SaveQueryDialog extends FavoritesDialog implements Button.ClickListener {

    private static final String UPDATE_SQL = "UPDATE SAVED_QUERY_MAINTENANCE SET SQL_TEXT='%s' WHERE ID = %s";
    private static final String INSERT_SQL = "INSERT INTO SAVED_QUERY_MAINTENANCE(ID, NAME, SQL_TEXT) "
            + "VALUES(SAVED_QUERY_MAINTENANCE_SEQ.NEXTVAL,'%s','%s')";

    private final String sqlText;

    SaveQueryDialog(SqlExecutor executor, String sqlText) {
        super(executor);
        this.sqlText = sqlText.replace("'", "''");
    }

    @Override
    protected HorizontalLayout createButtonLayout() {
        HorizontalLayout buttonLayout = super.createButtonLayout();
        Button saveButton = createSaveButton();
        buttonLayout.addComponent(saveButton, 0);
        buttonLayout.setComponentAlignment(saveButton, Alignment.TOP_LEFT);
        return buttonLayout;
    }

    private Button createSaveButton() {
        Button saveButton = new Button("Save", this);
        saveButton.setClickShortcut(ShortcutAction.KeyCode.ENTER);
        return saveButton;
    }

    @Override
    public void buttonClick(Button.ClickEvent event) {
        saveQuery();
    }

    private void saveQuery() {
        Integer id = getSelectedId();
        if (id != null) {
            saveExistingQuery(id);
        } else {
            String queryName = getFilterText();
            if (queryName.isEmpty()) {
                MessageBox.Show("Enter name", getApplication().getMainWindow());
                return;
            }
            saveNewQuery(queryName);
        }
        close();
    }

    private void saveExistingQuery(Integer queryId) {
        runQuery(String.format(UPDATE_SQL, sqlText, queryId));
    }

    private void saveNewQuery(String name) {
        runQuery(String.format(INSERT_SQL, name, sqlText));
    }

    private void runQuery(String query) {
        SqlExecutor executor = getExecutor();
        executor.setQueryType(QueryType.INSERT_OR_UPDATE);
        executor.setUsingConnectionPool(true);
        executor.runQuery(query,true);
    }

}
