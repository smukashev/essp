package com.bsbnb.creditregistry.portlets.report.export;

import java.util.Date;
import java.util.List;
import java.util.logging.Level;

import com.bsbnb.creditregistry.portlets.report.Localization;
import com.bsbnb.creditregistry.portlets.report.ReportApplication;
import static com.bsbnb.creditregistry.portlets.report.ReportApplication.log;
import com.bsbnb.creditregistry.portlets.report.ReportPortletResource;
import com.bsbnb.creditregistry.portlets.report.ui.ConstantValues;
import com.bsbnb.creditregistry.portlets.report.ui.CustomDataSource;
import com.bsbnb.vaadin.messagebox.MessageBox;
import com.bsbnb.vaadin.messagebox.MessageBoxListener;
import com.bsbnb.vaadin.messagebox.MessageResult;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.Label;
import com.vaadin.ui.Link;
import com.vaadin.ui.Table;

/**
 *
 * @author Aidar.Myrzahanov
 */
public class BanksWithDataTableReportExporter extends TableReportExporter {

    private String LOCALIZATION_CONTEXT = "BANKS-WITH-DATA-REPORT-COMPONENT";

    private String getLocalizedString(String key) {
        return ReportApplication.getResourceString(LOCALIZATION_CONTEXT + "." + key);
    }

    @Override
    protected Table getTable(CustomDataSource customDataSource) {
        if (getApplication() != null) {
            log.log(Level.INFO, "Theme: {0}", getApplication().getTheme());
        }
        final Table table = super.getTable(customDataSource);

        table.setStyleName("colored-table");
        table.setCellStyleGenerator(new Table.CellStyleGenerator() {

            public String getStyle(Object itemId, Object propertyId) {
                Item item = table.getItem(itemId);
                String statusIDString = item.getItemProperty("STATUS-ID").getValue().toString();
                int statusID = (int) Double.parseDouble(statusIDString);
                switch (statusID) {
                    case ConstantValues.REPORT_STATUS_OK:
                        return "lightblue";
                    case ConstantValues.CROSS_CHECK_SUCCESS:
                        return "lightgreen";
                    case ConstantValues.CROSS_CHECK_FAIL:
                        return "red";
                    case ConstantValues.ORGANIZATION_APPROVED:
                        return "orange";
                }
                return "white";
            }
        });
        String buttonColumnName = getLocalizedString("BUTTON-COLUMN-CAPTION");
        String buttonCaption = getLocalizedString("CHANGE-STATUS-BUTTON-CAPTION");
        table.addContainerProperty(buttonColumnName, Component.class, null);

        table.addGeneratedColumn("STATUS-NAME", new Table.ColumnGenerator() {

            public Component generateCell(Table source, Object itemId, Object columnId) {
                Item item = table.getItem(itemId);
                String creditorIdString = item.getItemProperty("CREDITOR-ID").getValue().toString();
                int creditorId = (int) Double.parseDouble(creditorIdString);
                String statusName = item.getItemProperty("STATUS-NAME").getValue().toString();
                List<Object> parameters = getTargetReportComponent().getParameterValues();
                if (parameters.size() > 0 && parameters.get(0) instanceof Date) {
                    Date reportDate = (Date) parameters.get(0);
                    Link link = new CrossCheckLink(statusName, creditorId, reportDate);
                    return link;
                }
                return null;
            }
        });
        for (Object itemId : table.getItemIds()) {
            Item item = table.getItem(itemId);
            String reportIdString = item.getItemProperty("ID").getValue().toString();
            final int reportID = (int) Double.parseDouble(reportIdString);
            Property buttonProperty = item.getItemProperty(buttonColumnName);
            String statusIDString = item.getItemProperty("STATUS-ID").getValue().toString();
            int statusID = (int) Double.parseDouble(statusIDString);
            Component changeStatusColumnValue = null;
            if (statusID != ConstantValues.REPORT_STATUS_OK) {
                if (getTargetReportComponent().getConnect().isUserNationalBankEmployee()) {
                    Button button = new Button(buttonCaption);
                    button.addListener(new Button.ClickListener() {

                        public void buttonClick(ClickEvent event) {
                            promptReportApprove(reportID, ConstantValues.REPORT_STATUS_OK);
                        }
                    });
                    changeStatusColumnValue = button;
                } else if (table.getItemIds().size() == 1) {
                    if (statusID == ConstantValues.ORGANIZATION_APPROVED) {
                        String restoreStatusButtonCaption = getLocalizedString("RESTORE-STATUS-BUTTON-CAPTION");
                        Button button = new Button(restoreStatusButtonCaption);
                        button.addListener(new Button.ClickListener() {

                            public void buttonClick(ClickEvent event) {
                                promptReportApprove(reportID, ConstantValues.REPORT_STATUS_IN_PROGRESS);
                            }
                        });
                        changeStatusColumnValue = button;
                    } else {
                        Button button = new Button(buttonCaption);
                        button.addListener(new Button.ClickListener() {

                            public void buttonClick(ClickEvent event) {
                                promptReportApprove(reportID, ConstantValues.ORGANIZATION_APPROVED);
                            }
                        });
                        changeStatusColumnValue = button;
                    }
                } else {
                    changeStatusColumnValue = new Label(Localization.NOT_APPROVED.getValue());
                }

            } else if (getTargetReportComponent().getConnect().isUserNationalBankEmployee()) {
                String restoreStatusButtonCaption = getLocalizedString("RESTORE-STATUS-BUTTON-CAPTION");
                Button button = new Button(restoreStatusButtonCaption);
                button.addListener(new Button.ClickListener() {

                    public void buttonClick(ClickEvent event) {
                        promptReportRestore(reportID, ConstantValues.REPORT_STATUS_IN_PROGRESS);
                    }
                });
                changeStatusColumnValue = button;
            } else {
                Embedded okImage = new Embedded("", ReportPortletResource.OK_ICON);
                Object usernameObj = item.getItemProperty("USERNAME").getValue();
                String username = usernameObj == null ? "" : usernameObj.toString();
                okImage.setDescription(username);
                changeStatusColumnValue = okImage;
            }
            buttonProperty.setValue(changeStatusColumnValue);
        }

        table.setVisibleColumns(new String[]{"NAME", "STATUS-NAME", buttonColumnName, "ACTUAL-COUNT", "BEGIN-DATE", "END-DATE"});
        table.setColumnAlignment(buttonColumnName, Table.ALIGN_CENTER);
        table.setColumnWidth("NAME", 200);
        table.setColumnWidth("ACTUAL-COUNT", 140);
        table.setColumnWidth("BEGIN-DATE", 150);
        table.setColumnWidth("END-DATE", 140);
        table.setColumnWidth("STATUS-NAME", 250);

        table.setPageLength(Math.min(10, customDataSource.getItemIds().size()));
        table.setHeight(null);
        table.setWidth("100%");
        return table;
    }

    private void promptReportRestore(final int reportID, final int statusId) {
        MessageBox.Show(getLocalizedString("RESTORE-REPORT-PROMPT-TEXT"), getLocalizedString("RESTORE-REPORT-PROMPT-CAPTION"), new MessageBoxListener() {

            public void messageResult(MessageResult result) {
                log.log(Level.INFO, "Approve report");
                if (result == MessageResult.OK) {
                    approveReport(reportID, statusId);
                }
            }
        }, getApplication().getMainWindow());
    }

    private void promptReportApprove(final int reportID, final int statusId) {
        MessageBox.Show(getLocalizedString("APPROVE-REPORT-PROMPT-TEXT"), getLocalizedString("APPROVE-REPORT-PROMPT-CAPTION"), new MessageBoxListener() {

            public void messageResult(MessageResult result) {
                log.log(Level.INFO, "Approve report");
                if (result == MessageResult.OK) {
                    approveReport(reportID, statusId);
                }
            }
        }, getApplication().getMainWindow());
    }
    //BUG: MessageBox вызванный из listener-a другого MessageBox не закрывается

    private void approveReport(int reportID, int statusId) {
        if (getTargetReportComponent().getConnect().approveReport(reportID, statusId)) {
            log.log(Level.INFO, "Show approval successful message");
            //MessageBox.Show(getLocalizedString("APPROVED-SUCCESSFULLY-TEXT"), getLocalizedString("APPROVED-SUCCESSFULLY-CAPTION"), getApplication().getMainWindow());
        } else {
            log.log(Level.INFO, "Show approval failed message");
            //MessageBox.Show(getLocalizedString("APPROVAL-FAILED-TEXT"), getLocalizedString("APPROVAL-FAILED-CAPTION"), getApplication().getMainWindow());
        }
        loadTable();
    }
}
