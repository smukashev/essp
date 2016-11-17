package com.bsbnb.creditregistry.portlets.queue.ui;

import com.bsbnb.creditregistry.portlets.queue.PortalEnvironmentFacade;
import com.bsbnb.creditregistry.portlets.queue.QueueApplication;
import com.bsbnb.creditregistry.portlets.queue.QueueApplicationResource;
import com.bsbnb.creditregistry.portlets.queue.QueuePortalEnvironmentFacade;
import com.bsbnb.creditregistry.portlets.queue.data.DataProvider;
import com.bsbnb.creditregistry.portlets.queue.data.InputInfoDisplayBean;
import com.bsbnb.vaadin.filterableselector.FilterableSelect;
import com.bsbnb.vaadin.filterableselector.SelectionCallback;
import com.bsbnb.vaadin.filterableselector.Selector;
import com.bsbnb.vaadin.formattedtable.FormattedTable;
import com.bsbnb.vaadin.messagebox.MessageBox;
import com.vaadin.data.Container;
import com.vaadin.data.Property;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.ui.*;
import kz.bsbnb.usci.cr.model.Creditor;
import kz.bsbnb.usci.eav.model.Batch;
import org.apache.log4j.Logger;
import org.hibernate.annotations.Check;

import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by bauka on 5/18/16.
 */
public class MaintenanceComponent  extends VerticalLayout {
    private static final String LOCALIZATION_PREFIX = "MAINTENANCE-COMPONENT.";
    private final PortalEnvironmentFacade environment;
    private final DataProvider dataProvider;
    private ComboBox orderBox;
    private FilterableSelect<Creditor> creditorsSelect;
    private VerticalLayout previewLayout;
    public final Logger logger = Logger.getLogger(AdministratorComponent.class);
    private DateField reportDateField;
    private FormattedTable filesTable;
    private BeanItemContainer<InputInfoDisplayBean> inputInfoContainer;

    private static final String[] FILES_TABLE_VISIBLE_COLUMNS = new String[]{
            "select","creditorName", "fileLink", "receiverDate", "startDate", "completionDate", "statusName", "reportDate"};

    private static final String[] FILES_TABLE_COLUMN_NAMES = new String[]{
            "select","creditorName", "fileLink", "fileName", "receiverDate", "completionDate", "statusName", "startDate",
            "reportDate"};
    private List<InputInfoDisplayBean> inputInfoList;

    public MaintenanceComponent(PortalEnvironmentFacade environment, DataProvider dataProvider) {
        this.environment = environment;
        this.dataProvider = dataProvider;
    }

    public void initializeUI() {
        removeAllComponents();
        logger.info("Loading maintenance interface");

        List<Creditor> userCreditors = dataProvider.getCreditors(environment.getUserId(), environment.isUserAdmin());
        creditorsSelect = new FilterableSelect<>(userCreditors, new Selector<Creditor>() {
            public String getCaption(Creditor item) {
                return item.getName();
            }

            public Object getValue(Creditor item) {
                return item.getId();
            }

            public String getType(Creditor item) {
                if(item.getSubjectType() != null)
                    return item.getSubjectType().getNameRu();

                return "null";
            }
        });

        addComponent(creditorsSelect);

        reportDateField = new DateField(Localization.REPORT_DATE_CAPTION.name());
        reportDateField.setDateFormat("dd.MM.yyyy");

        filesTable = new FormattedTable();
        inputInfoContainer = new BeanItemContainer<>(InputInfoDisplayBean.class);
        filesTable.setContainerDataSource(inputInfoContainer);

        filesTable.addGeneratedColumn("select", new Table.ColumnGenerator() {
            @Override
            public Object generateCell(Table table, Object o, Object o1) {
                final InputInfoDisplayBean inputInfoBean = ((InputInfoDisplayBean) o);

                Button checkBox = new CheckBox("", new Button.ClickListener() {

                    @Override
                    public void buttonClick(Button.ClickEvent clickEvent) {
                        inputInfoBean.setSelected(clickEvent.getButton().booleanValue());
                    }
                });

                checkBox.setImmediate(true);
                return checkBox;
            }
        });

        filesTable.setVisibleColumns(FILES_TABLE_COLUMN_NAMES);
        filesTable.setColumnHeaders(getResourceStrings(FILES_TABLE_COLUMN_NAMES));
        filesTable.setVisibleColumns(FILES_TABLE_VISIBLE_COLUMNS);
        filesTable.setWidth("100%");
        filesTable.setVisible(false);
        filesTable.addFormat("receiverDate", "dd/MM/yyyy HH:mm:ss");
        filesTable.addFormat("reportDate", "dd/MM/yyyy");
        filesTable.addFormat("completionDate", "dd/MM/yyyy HH:mm:ss");
        filesTable.addFormat("startDate", "dd/MM/yyyy HH:mm:ss");

        final Button sendFilesButton = new Button("Отправить", new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent clickEvent) {
                List<Long> batchIds = new LinkedList<>();

                Iterator<InputInfoDisplayBean> iterator = inputInfoList.iterator();

                while(iterator.hasNext()) {
                    if(!iterator.next().isSelected())
                        iterator.remove();
                }

                for (InputInfoDisplayBean inputInfoDisplayBean : inputInfoList) {
                        batchIds.add(inputInfoDisplayBean.getInputInfo().getId().longValue());
                }

                dataProvider.approveAndSend(batchIds);
                dataProvider.sendApprovedNotification(inputInfoList);

                creditorsSelect.getSelectedElements(new SelectionCallback<Creditor>() {
                    @Override
                    public void selected(List<Creditor> creditors) {
                        Date reportDate = (Date) reportDateField.getValue();
                        inputInfoList = dataProvider.getMaintenanceInfo(creditors, reportDate);
                        inputInfoContainer.removeAllItems();
                        inputInfoContainer.addAll(inputInfoList);
                        filesTable.setVisible(true);
                    }
                });

                MessageBox.Show("Успешно отправлено", getWindow());

            }
        });

        sendFilesButton.setVisible(false);


        final Button declineFilesButton = new Button("Отклонить", new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent clickEvent) {
                List<Long> batchIds = new LinkedList<>();

                Iterator<InputInfoDisplayBean> iterator = inputInfoList.iterator();

                while(iterator.hasNext()) {
                    if(!iterator.next().isSelected())
                        iterator.remove();
                }

                for (InputInfoDisplayBean inputInfoDisplayBean : inputInfoList) {
                    batchIds.add(inputInfoDisplayBean.getInputInfo().getId().longValue());
                }

                dataProvider.declineAndSend(batchIds);
                dataProvider.sendDeclinedNotification(inputInfoList);

                creditorsSelect.getSelectedElements(new SelectionCallback<Creditor>() {
                    @Override
                    public void selected(List<Creditor> creditors) {
                        Date reportDate = (Date) reportDateField.getValue();
                        inputInfoList = dataProvider.getMaintenanceInfo(creditors, reportDate);
                        inputInfoContainer.removeAllItems();
                        inputInfoContainer.addAll(inputInfoList);
                        filesTable.setVisible(true);
                    }
                });

                MessageBox.Show("Успешно откланен", getWindow());

            }
        });

        declineFilesButton.setVisible(false);

        final Button loadButton = new Button(environment.getString(Localization.LOAD), new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                creditorsSelect.getSelectedElements(new SelectionCallback<Creditor>() {
                    @Override
                    public void selected(List<Creditor> creditors) {
                        Date reportDate = (Date) reportDateField.getValue();
                        inputInfoList = dataProvider.getMaintenanceInfo(creditors, reportDate);
                        if(inputInfoList.size() < 1) {
                            MessageBox.Show("Нет данных", getWindow());
                            return;
                        }
                        inputInfoContainer.removeAllItems();
                        inputInfoContainer.addAll(inputInfoList);
                        filesTable.setVisible(true);
                        sendFilesButton.setVisible(true);
                        declineFilesButton.setVisible(true);
                    }
                });
                //loadTable();
                /*if(queueTabSheet!=null) {
                    queueTabSheet.setSelectedTab(table);
                }*/
            }
        });

        addComponent(loadButton);
        addComponent(filesTable);

        HorizontalLayout horizontalLayout = new HorizontalLayout();
        horizontalLayout.addComponent(sendFilesButton);
        horizontalLayout.addComponent(declineFilesButton);

        addComponent(horizontalLayout);
    }

    private String[] getResourceStrings(String[] keys) {
        String[] result = new String[keys.length];
        for (int keyIndex = 0; keyIndex < keys.length; keyIndex++) {
            result[keyIndex] = QueuePortalEnvironmentFacade.get().getResourceString(LOCALIZATION_PREFIX + keys[keyIndex]);
        }
        return result;
    }
}
