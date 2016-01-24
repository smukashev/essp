package kz.bsbnb.usci.portlets.query;

import com.bsbnb.vaadin.messagebox.MessageBox;
import com.vaadin.data.util.ObjectProperty;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.PasswordField;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import org.hibernate.jdbc.util.BasicFormatterImpl;

/**
 *
 * @author Aidar.Myrzahanov
 */
class QueryComponent extends VerticalLayout implements OpenQueryListener {

    private TextArea queryArea;
    private VerticalLayout optionsLayout;
    private VerticalLayout contentPanel;
    private Button toXLSButton;

    private ResultsTable resultsTable;

    private final SqlExecutor executor;
    private final ObjectProperty<Boolean> isTextProperty = new ObjectProperty<Boolean>(false);

    QueryComponent(SqlExecutor executor) {
        this.executor = executor;
    }

    @Override
    public void attach() {
        queryArea = createTextArea("");
        queryArea.setCaption("Query");

        contentPanel = new VerticalLayout();
        contentPanel.setSpacing(false);

        addComponent(queryArea);
        addComponent(createToolsLayout());
        optionsLayout = createOptionsLayout();
        addComponent(optionsLayout);
        addComponent(contentPanel);
        setWidth("100%");
        setHeight("100%");
        setSpacing(false);
    }

    private VerticalLayout createOptionsLayout() {
        TextField connectionStringField = new TextField("Connection string", executor.getConnectionStringProperty());
        connectionStringField.setImmediate(true);
        connectionStringField.setWidth("100%");
        TextField usernameField = new TextField("Username", executor.getUsernameProperty());
        usernameField.setImmediate(true);
        PasswordField passwordField = new PasswordField("Password", executor.getPasswordProperty());
        passwordField.setImmediate(true);

        VerticalLayout layout = new VerticalLayout();
        layout.addComponent(connectionStringField);
        layout.addComponent(usernameField);
        layout.addComponent(passwordField);
        layout.setVisible(false);
        layout.setSpacing(false);
        return layout;
    }

    private HorizontalLayout createToolsLayout() {
        HorizontalLayout toolsLayout = new HorizontalLayout();
        toolsLayout.setSpacing(false);
        toolsLayout.setWidth("100%");
        HorizontalLayout queryToolsLayout = createQueryToolsLayout();
        toolsLayout.addComponent(queryToolsLayout);
        toolsLayout.setComponentAlignment(queryToolsLayout, Alignment.MIDDLE_LEFT);
        HorizontalLayout userToolsLayout = createUserToolsLayout();
        toolsLayout.addComponent(userToolsLayout);
        toolsLayout.setComponentAlignment(userToolsLayout, Alignment.MIDDLE_RIGHT);
        return toolsLayout;
    }

    private HorizontalLayout createUserToolsLayout() {
        toXLSButton = new Button(null, new Button.ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {
                downloadResultsAsXlsAndHandleErrors();
            }

        });
        toXLSButton.setIcon(QueryPortletIcon.EXCEL);
        toXLSButton.setDescription("Export results table to XLS");
        toXLSButton.setEnabled(false);
        Button prettyPrintButton = new Button(null, new Button.ClickListener() {

            public void buttonClick(ClickEvent event) {
                prettyPrintQuery();
            }
        });
        prettyPrintButton.setIcon(QueryPortletIcon.BEAUTIFY);
        prettyPrintButton.setDescription("Beautify SQL");
        Button saveButton = new Button(null);
        saveButton.setIcon(QueryPortletIcon.SAVE);
        saveButton.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {
                showSaveQueryDialog();
            }
        });
        saveButton.setDescription("Save current query");
        Button openButton = new Button(null);
        openButton.setIcon(QueryPortletIcon.OPEN);
        openButton.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {
                showOpenQueryDialog();
            }
        });
        openButton.setDescription("Open saved query");
        HorizontalLayout userToolsLayout = new HorizontalLayout();
        userToolsLayout.setSpacing(false);
        userToolsLayout.addComponent(toXLSButton);
        userToolsLayout.addComponent(prettyPrintButton);
        userToolsLayout.addComponent(saveButton);
        userToolsLayout.addComponent(openButton);
        return userToolsLayout;
    }

    private void downloadResultsAsXlsAndHandleErrors() {
        if (resultsTable != null) {
            resultsTable.downloadXls("table.xls");
        } else {
            MessageBox.Show("Nothing to export", getWindow());
        }
    }

    private HorizontalLayout createQueryToolsLayout() {
        Button selectButton = new Button("Select", new Button.ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {
                runQuery(QueryType.SELECT);
            }
        });
        Button updateButton = new Button("Update", new Button.ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {
                runQuery(QueryType.INSERT_OR_UPDATE);
            }
        });
        Button showPlanButton = new Button("Plan", new Button.ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {
                runQuery(QueryType.EXPLAIN_PLAN);
            }
        });
        Button showOptionsButton = new Button(null, new Button.ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {
                toggleOptionsButton(event.getButton());
            }
        });
        showOptionsButton.setIcon(QueryPortletIcon.SETTINGS);
        showOptionsButton.setDescription("Show/hide connection options");
        TextField limitBox = new TextField("Limit", executor.getLimitProperty());
        limitBox.setWidth("50px");
        TextField timeoutBox = new TextField("Timeout", executor.getTimeoutProperty());
        timeoutBox.setWidth("50px");
        CheckBox isTextCheckBox = new CheckBox("Text", isTextProperty);
        isTextCheckBox.setImmediate(true);
        HorizontalLayout queryToolsLayout = new HorizontalLayout();
        queryToolsLayout.setSpacing(false);
        queryToolsLayout.addComponent(selectButton);
        queryToolsLayout.addComponent(updateButton);
        queryToolsLayout.addComponent(showPlanButton);
        queryToolsLayout.addComponent(limitBox);
        queryToolsLayout.addComponent(timeoutBox);
        queryToolsLayout.addComponent(isTextCheckBox);
        queryToolsLayout.addComponent(showOptionsButton);
        return queryToolsLayout;
    }

    private void toggleOptionsButton(Button button) {
        if ("v-button v-pressed".equals(button.getStyleName())) {
            button.setStyleName("v-button");
        } else {
            button.setStyleName("v-button v-pressed");
        }
        optionsLayout.setVisible(!optionsLayout.isVisible());
    }

    public void runQuery(QueryType queryType) {
        String query = queryArea.getValue().toString();
        executor.setQueryType(queryType);
        executor.setUsingConnectionPool(!optionsLayout.isVisible());
        executor.runQuery(query);
        contentPanel.removeAllComponents();
        toXLSButton.setEnabled(false);
        if (executor.getExceptionMessage() != null) {
            TextArea exceptionArea = new TextArea("Exception");
            exceptionArea.setValue(executor.getExceptionMessage());
            exceptionArea.setWidth("100%");
            exceptionArea.setWordwrap(true);
            contentPanel.addComponent(exceptionArea);
            if (executor.getSqlErrorPosition() > 0) {
                queryArea.setCursorPosition(executor.getSqlErrorPosition());
            }
        } else {
            if (queryType == QueryType.SELECT) {
                if (isTextProperty.getValue()) {
                    contentPanel.addComponent(createTextArea(executor.getTextResults()));
                } else {
                    resultsTable = executor.getQueryResultTable();
                    contentPanel.addComponent(resultsTable);
                }
                addContentPanelMessage("Rows count", executor.getRowsCount());
                toXLSButton.setEnabled(true);
            } else if (queryType == QueryType.INSERT_OR_UPDATE) {
                addContentPanelMessage("Affected rows count", executor.getAffectedRowsCount());
            } else if (queryType == QueryType.EXPLAIN_PLAN) {
                contentPanel.addComponent(createTextArea(executor.getTextResults()));
            }
            addContentPanelMessage("Execution time", executor.getExecutionTimeMillis());
        }
    }

    private TextArea createTextArea(String value) {
        TextArea area = new TextArea(null, value);
        area.setImmediate(true);
        area.setWidth("100%");
        area.setRows(10);
        area.setStyleName("sql-area");
        return area;
    }

    private void addContentPanelMessage(String caption, Object value) {
        Label messageLabel = new Label("<b>" + caption + ":</b> " + value, Label.CONTENT_XHTML);
        contentPanel.addComponent(messageLabel);
    }

    private void showSaveQueryDialog() throws IllegalArgumentException {
        String text = (String) queryArea.getValue();
        getWindow().addWindow(new SaveQueryDialog(executor, (text == null ? "" : text)));
    }

    private void showOpenQueryDialog() throws IllegalArgumentException, NullPointerException {
        getWindow().addWindow(new OpenQueryDialog(executor, this));
    }

    @Override
    public void openQuery(String queryText) {
        queryArea.setValue(queryText);
    }

    private void prettyPrintQuery() {
        queryArea.setValue(formatSql((String) queryArea.getValue()));
    }

    private String formatSql(String sqlText) {
        String sql = (new BasicFormatterImpl()).format(sqlText);
        String[] lines = sql.split("\n");
        StringBuilder formattedText = new StringBuilder();
        for (String line : lines) {
            if (line.trim().isEmpty()) {
                continue;
            }
            if (line.startsWith("    ")) {
                formattedText.append(line.substring(4));
            } else {
                formattedText.append(line);
            }
            formattedText.append('\n');
        }
        return formattedText.toString();
    }
}
