package kz.bsbnb.usci.portlets.query;

import com.bsbnb.vaadin.messagebox.MessageBox;
import com.vaadin.event.ShortcutAction;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import org.apache.log4j.Logger;

import java.util.logging.Level;

/**
 *
 * @author Aidar.Myrzahanov
 */
class SaveQueryDialog extends FavoritesDialog implements Button.ClickListener {

    private static final String UPDATE_SQL = "UPDATE MAINTENANCE.SAVED_QUERY SET SQL_TEXT='%s' WHERE ID = %s";
    private static final String INSERT_SQL = "INSERT INTO MAINTENANCE.SAVED_QUERY(ID, NAME, SQL_TEXT) "
            + "VALUES(MAINTENANCE.SAVED_QUERY_SEQ.NEXTVAL,'%s','%s')";

    private final String sqlText;

    public final Logger logger = Logger.getLogger(SaveQueryDialog.class);

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
        logger.info("Save query dialog query: "+ query);
        SqlExecutor executor = getExecutor();
        executor.setQueryType(QueryType.INSERT_OR_UPDATE);
        executor.setUsingConnectionPool(true);
        executor.runQuery(query);
    }

}
