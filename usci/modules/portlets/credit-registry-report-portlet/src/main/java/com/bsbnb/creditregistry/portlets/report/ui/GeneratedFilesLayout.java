package com.bsbnb.creditregistry.portlets.report.ui;

import com.bsbnb.creditregistry.portlets.report.Localization;
import com.bsbnb.creditregistry.portlets.report.ReportApplication;
import com.bsbnb.creditregistry.portlets.report.ReportPortletResource;
import com.bsbnb.creditregistry.portlets.report.dm.DatabaseConnect;
import com.bsbnb.creditregistry.portlets.report.dm.ReportController;
import com.bsbnb.creditregistry.portlets.report.dm.ReportLoad;
import com.bsbnb.creditregistry.portlets.report.dm.ReportLoadFile;
import com.bsbnb.vaadin.messagebox.MessageBox;
import com.liferay.portal.service.CompanyLocalServiceUtil;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.terminal.FileResource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.Table.ColumnGenerator;
import com.vaadin.ui.VerticalLayout;
import java.io.File;
import java.util.List;

/**
 *
 * @author Aidar.Myrzahanov
 */
class GeneratedFilesLayout extends VerticalLayout {

    private DatabaseConnect connect;

    public GeneratedFilesLayout(DatabaseConnect connect) {
        this.connect = connect;
    }
    private static final String[] LOADS_TABLE_COLUMNS = new String[]{
        "username", "reportName", "startTime", "finishTime", "note"
    };
    private static final String LOADS_TABLE_PREFIX = "LOADS-TABLE";
    private static final String[] FILES_TABLE_COLUMNS = new String[]{
        "filename", "downloadButton"
    };
    private static final String FILES_TABLE_PREFIX = "FILES-TABLE";

    private String[] getColumnHeaders(String tablePrefix, String[] columnNames) {
        String[] headers = new String[columnNames.length];
        for (int i = 0; i < columnNames.length; i++) {
            headers[i] = ReportApplication.getResourceString(tablePrefix + "." + columnNames[i]);
        }
        return headers;
    }
    private ReportController controller = new ReportController();
    private BeanItemContainer<ReportLoadFile> filesContainer;
    private VerticalLayout filesTableLayout;
    private BeanItemContainer<ReportLoad> loadsContainer;
    private FormattedTable loadsTable;

    @Override
    public void attach() {

        Label loadsTableCaption = new Label("<h2>" + Localization.LOADS_TABLE_CAPTION.getValue() + "</h2>", Label.CONTENT_XHTML);

        Button refreshButton = new Button("", new Button.ClickListener() {

            public void buttonClick(ClickEvent event) {
                loadUserLoads();
            }
        });
        refreshButton.setIcon(ReportPortletResource.REFRESH_ICON);
        refreshButton.setImmediate(true);
        refreshButton.setDescription(Localization.REFRESH_TABLE_DESCRIPTION.getValue());

        HorizontalLayout loadsTableCaptionLayout = new HorizontalLayout();
        loadsTableCaptionLayout.addComponent(loadsTableCaption);
        loadsTableCaptionLayout.addComponent(refreshButton);
        loadsTableCaptionLayout.setSpacing(true);
        loadsTableCaptionLayout.setComponentAlignment(refreshButton, Alignment.MIDDLE_RIGHT);

        loadsContainer = new BeanItemContainer<ReportLoad>(ReportLoad.class);

        loadsTable = new FormattedTable("");
        loadsTable.addDateFormat("startTime", "dd/MM/yyyy HH:mm:ss");
        loadsTable.addDateFormat("finishTime", "dd/MM/yyyy HH:mm:ss");
        loadsTable.setContainerDataSource(loadsContainer);
        loadsTable.setVisibleColumns(LOADS_TABLE_COLUMNS);
        loadsTable.setColumnHeaders(getColumnHeaders(LOADS_TABLE_PREFIX, LOADS_TABLE_COLUMNS));
        loadsTable.addListener(new ValueChangeListener() {

            public void valueChange(ValueChangeEvent event) {
                if (event.getProperty() != null) {
                    ReportLoad load = (ReportLoad) event.getProperty().getValue();
                    filesContainer.removeAllItems();
                    if (load != null) {
                        filesContainer.addAll(load.getFiles());
                        filesTableLayout.setVisible(true);
                    }
                }
            }
        });
        loadsTable.setWidth("100%");
        loadsTable.setSelectable(true);
        loadsTable.setMultiSelect(false);
        loadsTable.setImmediate(true);

        VerticalLayout loadsTableLayout = new VerticalLayout();
        loadsTableLayout.setSpacing(false);
        loadsTableLayout.addComponent(loadsTableCaptionLayout);
        loadsTableLayout.addComponent(loadsTable);


        Label filesTableCaption = new Label("<h2>" + Localization.FILES_TABLE_CAPTION.getValue() + "</h2>", Label.CONTENT_XHTML);

        filesContainer = new BeanItemContainer<ReportLoadFile>(ReportLoadFile.class);
        FormattedTable filesTable = new FormattedTable();
        filesTable.setContainerDataSource(filesContainer);
        filesTable.addGeneratedColumn("downloadButton", new ColumnGenerator() {

            public Object generateCell(Table source, Object itemId, Object columnId) {
                final ReportLoadFile file = (ReportLoadFile) itemId;
                Button content = new Button("", new Button.ClickListener() {

                    public void buttonClick(ClickEvent event) {
                        File fileToDownload = new File(file.getPath());
                        if (fileToDownload.exists()) {
                            FileResource resource = new FileResource(fileToDownload, getApplication()) {

                                @Override
                                public String getFilename() {
                                    return file.getFilename();
                                }

                                @Override
                                public String getMIMEType() {
                                    return file.getMimeType();
                                }
                            };
                            getWindow().open(resource);
                        } else {
                            MessageBox.Show(Localization.FILE_DOES_NOT_EXIST_MESSAGE.getValue(), getWindow());
                        }
                    }
                });
                content.setIcon(ReportPortletResource.DOWNLOAD_ICON);
                return content;
            }
        });
        filesTable.setVisibleColumns(FILES_TABLE_COLUMNS);
        filesTable.setColumnHeaders(getColumnHeaders(FILES_TABLE_PREFIX, FILES_TABLE_COLUMNS));
        filesTable.setWidth("100%");
        filesTable.setImmediate(true);

        filesTableLayout = new VerticalLayout();
        filesTableLayout.setSpacing(false);
        filesTableLayout.addComponent(filesTableCaption);
        filesTableLayout.addComponent(filesTable);

        filesTableLayout.setVisible(false);

        setSpacing(true);
        setMargin(true);

        addComponent(loadsTableLayout);
        addComponent(filesTableLayout);
        loadUserLoads();
    }

    private void loadUserLoads() {
        List<ReportLoad> loads = null;

//        if (connect.isUserNationalBankEmployee()) {
//            loads = controller.loadUsersLoads(connect.getUserId());
//        } else {
        loads = controller.loadUsersLoads(connect.getCoworkers());
//      }
        loadsContainer.removeAllItems();
        loadsContainer.addAll(loads);
        loadsTable.setValue(null);
        filesTableLayout.setVisible(false);
    }
}
