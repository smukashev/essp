package com.bsbnb.creditregistry.portlets.protocol.ui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import jxl.write.WriteException;

//import com.bsbnb.creditregistry.dm.ref.Creditor;
//import com.bsbnb.creditregistry.dm.ref.SubjectType;
import com.bsbnb.creditregistry.portlets.protocol.PortletEnvironmentFacade;
import static com.bsbnb.creditregistry.portlets.protocol.ProtocolApplication.log;
import com.bsbnb.creditregistry.portlets.protocol.ProtocolPortletEnvironmentFacade;
import com.bsbnb.creditregistry.portlets.protocol.ProtocolPortletResource;
import com.bsbnb.creditregistry.portlets.protocol.data.DataProvider;
import com.bsbnb.creditregistry.portlets.protocol.data.InputInfoDisplayBean;
import com.bsbnb.creditregistry.portlets.protocol.data.ProtocolDisplayBean;
import com.bsbnb.creditregistry.portlets.protocol.data.SharedDisplayBean;
import com.bsbnb.creditregistry.portlets.protocol.export.ExportException;
import com.bsbnb.creditregistry.portlets.protocol.export.ProtocolExporter;
import com.bsbnb.creditregistry.portlets.protocol.export.TxtProtocolNumbersExporter;
import com.bsbnb.creditregistry.portlets.protocol.export.XlsProtocolExporter;
import com.bsbnb.creditregistry.portlets.protocol.export.XmlProtocolExporter;
import com.bsbnb.creditregistry.portlets.protocol.export.ZippedProtocolExporter;
import com.bsbnb.vaadin.filterableselector.FilterableSelect;
import com.bsbnb.vaadin.filterableselector.SelectionCallback;
import com.bsbnb.vaadin.filterableselector.Selector;
import com.bsbnb.vaadin.messagebox.MessageBox;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.terminal.Resource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.DateField;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Tree;
import com.vaadin.ui.VerticalLayout;
import kz.bsbnb.usci.cr.model.Creditor;
import kz.bsbnb.usci.cr.model.SubjectType;

/**
 *
 * @author Marat.Madybayev
 */
public class ProtocolLayout extends VerticalLayout {

    private static final String LOCALIZATION_PREFIX = "PROTOCOL-LAYOUT.";
    private boolean isProtocolGrouped = false;
    private Map<Object, List<ProtocolDisplayBean>> groupsMapProtocol;
    private List<ProtocolDisplayBean> listOfProtocols;
    private Set<String> prohibitedMessageTypeCodes = new HashSet<String>();
    private DataProvider provider;
    private FilterableSelect<Creditor> creditorSelector;
    private DateField reportDateField;
    private BeanItemContainer<InputInfoDisplayBean> inputInfoContainer;
    private FormattedTable filesTable;
    private VerticalLayout filesTableLayout;
    private HorizontalLayout typesOfProtocolLayout;
    private Tree groupsOfProtocolTree;
    private Panel groupsTreePanel;
    private BeanItemContainer<ProtocolDisplayBean> protocolsContainer;
    private FormattedTable tableProtocol;
    private VerticalLayout protocolLayout;
    private Label noProtocolsLabel;
    //Vaadin table columns
    private static final String[] FILES_TABLE_VISIBLE_COLUMNS = new String[]{
        "creditorName", "fileLink", "receiverDate", "startDate", "completionDate", "statusName", "reportDate"};
    //All used columns
    private static final String[] FILES_TABLE_COLUMN_NAMES = new String[]{
        "creditorName", "fileLink", "fileName", "receiverDate", "completionDate", "statusName", "startDate", "reportDate"};
    //Exported table columns
    private static final String[] FILES_TABLE_COLUMNS_TO_EXPORT = new String[]{
        "creditorName", "fileName", "receiverDate", "startDate", "completionDate", "statusName", "reportDate"};
    private static final String[] PROTOCOL_TABLE_COLUMNS = new String[]{
        "statusIcon", "message", "note"};
    private static final String[] EXTENDED_PROTOCOL_TABLE_COLUMNS = new String[]{
        "statusIcon", "typeName", "description", "primaryContractDate", "message", "note"
    };
    private static final String[] EXPORT_PROTOCOL_TABLE_COLUMNS = new String[]{
        "description", "primaryContractDate", "typeName", "messageTypeName", "message", "note"
    };

    public ProtocolLayout(DataProvider provider) {
        this.provider = provider;
    }

    @Override
    public void attach() {
        List<Creditor> creditorList = provider.getCreditorsList();
        //Проверка на наличие доступа к протоколу
        if (creditorList == null || creditorList.isEmpty()) {
            //Добавляем сообщение об ошибке
            Label errorMessageLabel = new Label(Localization.MESSAGE_NO_CREDITORS.getValue());
            addComponent(errorMessageLabel);
            //выход
            return;
        }
        //Создаем элемент отображения кредиторов КредиторИнфо
        //
        //creditorSelector
        //
        creditorSelector = new FilterableSelect<Creditor>(creditorList, new Selector<Creditor>() {

            public String getCaption(Creditor item) {
                return item.getName();
            }

            public Object getValue(Creditor item) {
                return item.getId();
            }

            public String getType(Creditor item) {
                SubjectType subjectType = item.getSubjectType();
                return PortletEnvironmentFacade.get().isLanguageKazakh() ? subjectType.getNameKz() : subjectType.getNameRu();
            }
        });
        creditorSelector.setImmediate(true);
        //
        //reportDateField
        //

        reportDateField = new DateField(Localization.REPORT_DATE_CAPTION.getValue());
        reportDateField.setDateFormat("dd.MM.yyyy");
        //
        //showProtocolButton
        //
        Button showProtocolButton = new Button(Localization.SHOW_BUTTON_CAPTION.getValue(), new Button.ClickListener() {

            public void buttonClick(ClickEvent event) {
                loadCreditorInfo();
            }
        });
        showProtocolButton.setImmediate(true);
        //Создаем таблицу файлов
        //
        //filesTableHeaderLabel
        //
        Label headerOfFilesTableLabel = new Label("<h4>" + Localization.FILES_TABLE_CAPTION.getValue() + "</h4>", Label.CONTENT_XHTML);
        //
        //filesTableExportToXLSButton
        //
        Button filesTableExportToXLSButton = new Button(Localization.EXPORT_TO_XLS_CAPTION.getValue(), new Button.ClickListener() {

            public void buttonClick(ClickEvent event) {
                try {
                    filesTable.downloadXls("files.xls", FILES_TABLE_COLUMNS_TO_EXPORT, getResourceStrings(FILES_TABLE_COLUMNS_TO_EXPORT));
                } catch (IOException ioe) {
                    log.log(Level.WARNING, "Input info export failed", ioe);
                    MessageBox.Show(Localization.MESSAGE_EXPORT_FAILED.getValue(), getWindow());
                } catch (WriteException we) {
                    log.log(Level.WARNING, "Input info export failed", we);
                    MessageBox.Show(Localization.MESSAGE_EXPORT_FAILED.getValue(), getWindow());
                }
            }
        });
        filesTableExportToXLSButton.setIcon(ProtocolPortletResource.EXCEL_ICON);
        //
        //filesTableHeaderLayout
        //
        HorizontalLayout filesTableHeaderLayout = new HorizontalLayout();
        filesTableHeaderLayout.addComponent(headerOfFilesTableLabel);
        filesTableHeaderLayout.setComponentAlignment(headerOfFilesTableLabel, Alignment.MIDDLE_LEFT);
        filesTableHeaderLayout.addComponent(filesTableExportToXLSButton);
        filesTableHeaderLayout.setComponentAlignment(filesTableExportToXLSButton, Alignment.MIDDLE_RIGHT);
        filesTableHeaderLayout.setWidth("100%");
        //
        //filesTable
        //
        filesTable = new FormattedTable();

        inputInfoContainer = new BeanItemContainer<InputInfoDisplayBean>(InputInfoDisplayBean.class);
        filesTable.setContainerDataSource(inputInfoContainer);
        filesTable.setVisibleColumns(FILES_TABLE_COLUMN_NAMES);
        filesTable.setColumnHeaders(getResourceStrings(FILES_TABLE_COLUMN_NAMES));
        filesTable.setVisibleColumns(FILES_TABLE_VISIBLE_COLUMNS);
        filesTable.setWidth("100%");
        filesTable.setSelectable(true);
        filesTable.setMultiSelect(false);
        filesTable.setImmediate(true);
        filesTable.addDateFormat("receiverDate", "dd/MM/yyyy HH:mm:ss");
        filesTable.addDateFormat("reportDate", "dd/MM/yyyy");
        filesTable.addDateFormat("completionDate", "dd/MM/yyyy HH:mm:ss");
        filesTable.addDateFormat("startDate", "dd/MM/yyyy HH:mm:ss");
        filesTable.addListener(new Property.ValueChangeListener() {

            public void valueChange(ValueChangeEvent event) {
                if (event.getProperty().getValue() != null) {
                    showProtocol((InputInfoDisplayBean) event.getProperty().getValue());
                }
            }
        });
        filesTable.sort(new String[]{"receiverDate"}, new boolean[]{false});
        //
        //filesTableLayout
        //
        filesTableLayout = new VerticalLayout();
        filesTableLayout.addComponent(filesTableHeaderLayout);
        filesTableLayout.addComponent(filesTable);
        filesTableLayout.setSpacing(false);
        filesTableLayout.setWidth("100%");
        filesTableLayout.setVisible(false);
        //
        //headerProtocolLabel
        //
        Label headerProtocolLabel = new Label("<h4>" + Localization.PROTOCOL_TABLE_CAPTION.getValue() + "</h4>", Label.CONTENT_XHTML);
        //
        //protocolTypesLayout
        //
        typesOfProtocolLayout = new HorizontalLayout();
        typesOfProtocolLayout.setImmediate(true);
        //
        //protocolsTableExportToXLSButton
        //
        Button exportProtocolToXLSButton = new Button(Localization.EXPORT_TO_XLS_CAPTION.getValue(), new Button.ClickListener() {

            public void buttonClick(ClickEvent event) {
                ProtocolExporter exporter = new XlsProtocolExporter(EXPORT_PROTOCOL_TABLE_COLUMNS, getResourceStrings(EXPORT_PROTOCOL_TABLE_COLUMNS));
                List<ProtocolDisplayBean> visibleProtocols = Arrays.asList(tableProtocol.getItemIds().toArray(new ProtocolDisplayBean[0]));
                exportProtocolData(exporter, visibleProtocols);
            }
        });
        exportProtocolToXLSButton.setIcon(ProtocolPortletResource.EXCEL_ICON);
        //
        //protocolTableExportToTXTButton
        //
        Button exportProtocolToTxtButton = new Button(Localization.EXPORT_NUMBERS.getValue(), new Button.ClickListener() {

            public void buttonClick(ClickEvent event) {
                exportProtocolData(new TxtProtocolNumbersExporter());
            }
        });
        exportProtocolToTxtButton.setIcon(ProtocolPortletResource.TXT_ICON);
        //
        //protocolTableExportToXMLButton
        //
        Button exportProtocolToXMLButton = new Button(Localization.EXPORT_TO_XML.getValue(), new Button.ClickListener() {

            public void buttonClick(ClickEvent event) {
                exportProtocolData(new XmlProtocolExporter());
            }
        });
        exportProtocolToXMLButton.setIcon(ProtocolPortletResource.XML_ICON);
        //
        //showGroupsToggleButton
        //
        final Button showGroupsToggleButton = new Button(Localization.GROUP_PROTOCOL_RECORDS_BUTTON_CAPTION.getValue());
        showGroupsToggleButton.setIcon(ProtocolPortletResource.TREE_ICON);
        showGroupsToggleButton.setImmediate(true);
        showGroupsToggleButton.addListener(new Button.ClickListener() {

            public void buttonClick(ClickEvent event) {
                isProtocolGrouped = !isProtocolGrouped;
                if (isProtocolGrouped) {
                    showGroupsToggleButton.setCaption(Localization.UNGROUP_PROTOCOL_RECORDS_BUTTON_CAPTION.getValue());
                    showGroupsToggleButton.setStyleName("v-button v-pressed");
                } else {
                    showGroupsToggleButton.setCaption(Localization.GROUP_PROTOCOL_RECORDS_BUTTON_CAPTION.getValue());
                    showGroupsToggleButton.setStyleName("v-button");
                }
                InputInfoDisplayBean inputInfo = (InputInfoDisplayBean) filesTable.getValue();
                if (inputInfo == null) {
                    return;
                }
                showProtocol(inputInfo);
            }
        });
        //
        //protocolTableHeaderLayout
        //
        CssLayout headerProtocolLayout = new CssLayout() {

            @Override
            protected String getCss(Component c) {
                if (c instanceof HorizontalLayout) {
                    return "float: left";
                }
                if (c instanceof Button) {
                    return "float: right";
                }
                return null;
            }
        };
        headerProtocolLayout.addComponent(typesOfProtocolLayout);
        headerProtocolLayout.addComponent(showGroupsToggleButton);
        headerProtocolLayout.addComponent(exportProtocolToXLSButton);
        headerProtocolLayout.addComponent(exportProtocolToTxtButton);
        headerProtocolLayout.addComponent(exportProtocolToXMLButton);
        headerProtocolLayout.setWidth("100%");
        //
        //protocolGroupsTree
        //
        groupsOfProtocolTree = new Tree();
        groupsOfProtocolTree.setHeight("100%");
        groupsOfProtocolTree.setWidth("200px");
        groupsOfProtocolTree.setImmediate(true);
        groupsOfProtocolTree.addListener(new Property.ValueChangeListener() {

            public void valueChange(ValueChangeEvent event) {
                if (event.getProperty() == null || event.getProperty().getValue() == null) {
                    return;
                }
                Object itemId = event.getProperty().getValue();
                log.log(Level.INFO, "Clicked item id: {0}", itemId);
                List<ProtocolDisplayBean> protocols = groupsMapProtocol.get(itemId);
                if (protocols == null) {
                    return;
                }
                protocolsContainer.removeAllItems();
                protocolsContainer.addAll(protocols);
            }
        });
        //
        //groupsTreePanel
        //
        groupsTreePanel = new Panel();
        groupsTreePanel.addComponent(groupsOfProtocolTree);
        groupsTreePanel.setScrollable(true);
        groupsTreePanel.setWidth("300px");
        groupsTreePanel.setHeight("100%");
        //
        //protocolTable
        //
        tableProtocol = new FormattedTable();
        tableProtocol.setStyleName("wordwrap-table");
        protocolsContainer = new BeanItemContainer<ProtocolDisplayBean>(ProtocolDisplayBean.class);
        tableProtocol.setContainerDataSource(protocolsContainer);
        tableProtocol.addDateFormat("primaryContractDate", "dd.MM.yyyy");
        tableProtocol.setImmediate(true);
        tableProtocol.setSizeFull();
        //
        //protocolTableLayout
        //
        HorizontalLayout layoutOfProtocolTable = new HorizontalLayout();
        layoutOfProtocolTable.addComponent(groupsTreePanel);
        layoutOfProtocolTable.addComponent(tableProtocol);
        layoutOfProtocolTable.setExpandRatio(tableProtocol, 1.0f);
        layoutOfProtocolTable.setWidth("100%");
        layoutOfProtocolTable.setImmediate(true);
        //
        //noProtocolsLabel
        //
        noProtocolsLabel = new Label(Localization.NO_PROTOCOLS_BY_INPUT_INFO_MESSAGE.getValue(), Label.CONTENT_XHTML);
        noProtocolsLabel.setVisible(false);
        noProtocolsLabel.setImmediate(true);
        //
        //protocolLayout
        //
        protocolLayout = new VerticalLayout();
        protocolLayout.addComponent(headerProtocolLabel);
        protocolLayout.addComponent(headerProtocolLayout);
        protocolLayout.addComponent(layoutOfProtocolTable);
        protocolLayout.setSpacing(false);
        protocolLayout.setWidth("100%");
        protocolLayout.setVisible(false);
        protocolLayout.setImmediate(true);
        //Добавляем КредиторИнфо и таблицы 
        setSpacing(true);
        addComponent(creditorSelector);
        if (creditorList.size() == 1) {
            loadCreditorInfo(creditorList);
        } else {
            addComponent(reportDateField);
            addComponent(showProtocolButton);
        }
        addComponent(filesTableLayout);
        addComponent(noProtocolsLabel);
        addComponent(protocolLayout);
        setWidth("100%");
    }

    private void exportProtocolData(ProtocolExporter exporter, List<ProtocolDisplayBean> data) {
        ZippedProtocolExporter zippedExporter = new ZippedProtocolExporter(exporter);
        zippedExporter.setProtocols(data);
        zippedExporter.setApplication(getApplication());
        InputInfoDisplayBean selectedInputInfo = (InputInfoDisplayBean) filesTable.getValue();
        if (selectedInputInfo != null) {
            String filename = selectedInputInfo.getFileName();
            if (filename.endsWith(".zip")) {
                filename = filename.substring(0, filename.length() - 4);
            }
            zippedExporter.setFilenamePrefix(filename);
        }
        try {
            Resource resource = zippedExporter.getResource();
            (getWindow()).open(resource);
        } catch (ExportException ee) {
            log.log(Level.INFO, "Failed to export", ee);
            MessageBox.Show(Localization.MESSAGE_EXPORT_FAILED.getValue(), getWindow());
        }
    }

    private String[] getResourceStrings(String[] keys) {
        String[] result = new String[keys.length];
        for (int keyIndex = 0; keyIndex < keys.length; keyIndex++) {
            result[keyIndex] = ProtocolPortletEnvironmentFacade.get().getResourceString(LOCALIZATION_PREFIX + keys[keyIndex]);
        }
        return result;
    }

    private void setProtocolColumns(String[] columns) {
        tableProtocol.setVisibleColumns(columns);
        tableProtocol.setColumnHeaders(getResourceStrings(columns));
        tableProtocol.setColumnWidth("note", 300);
    }

    private void showProtocol(InputInfoDisplayBean ii) throws UnsupportedOperationException {
        groupsOfProtocolTree.removeAllItems();
        protocolsContainer.removeAllItems();
        typesOfProtocolLayout.removeAllComponents();
        groupsMapProtocol = new HashMap<Object, List<ProtocolDisplayBean>>();
        if (isProtocolGrouped) {
            showGroupedProtocol(ii);
        } else {
            showProtocolTable(ii);
        }
    }

    private void showProtocolTable(InputInfoDisplayBean ii) throws UnsupportedOperationException {
        typesOfProtocolLayout.setVisible(true);
        groupsTreePanel.setVisible(false);

        prohibitedMessageTypeCodes.clear();
        listOfProtocols = provider.getProtocolsByInputInfo(ii);
        Set<String> messageTypeCodes = new HashSet<String>();
        if (listOfProtocols.isEmpty()) {
            noProtocolsLabel.setVisible(true);
            protocolLayout.setVisible(false);
        } else {
            for (final ProtocolDisplayBean protocol : listOfProtocols) {
                tableProtocol.addItem(protocol);
                if (!messageTypeCodes.contains(protocol.getMessageTypeCode())) {
                    messageTypeCodes.add(protocol.getMessageTypeCode());
                    Button filterButton = new Button("", new Button.ClickListener() {

                        public void buttonClick(ClickEvent event) {
                            Button button = event.getButton();
                            String styleName = button.getStyleName();
                            if ("v-button v-pressed".equals(styleName)) {
                                button.setStyleName("v-button");
                                prohibitedMessageTypeCodes.add(protocol.getMessageTypeCode());
                            } else {
                                button.setStyleName("v-button v-pressed");
                                prohibitedMessageTypeCodes.remove(protocol.getMessageTypeCode());
                            }
                            updateProtocolTable();
                        }
                    });
                    filterButton.setStyleName("v-button v-pressed");
                    filterButton.setIcon(protocol.getStatusIcon().getSource());
                    filterButton.setDescription(protocol.getMessageTypeName());
                    filterButton.setImmediate(true);
                    typesOfProtocolLayout.addComponent(filterButton);
                }
            }
            setProtocolColumns(EXTENDED_PROTOCOL_TABLE_COLUMNS);
            noProtocolsLabel.setVisible(false);
            protocolLayout.setVisible(true);
        }
    }

    private void showGroupedProtocol(InputInfoDisplayBean ii) throws UnsupportedOperationException {
        typesOfProtocolLayout.setVisible(false);
        Map<SharedDisplayBean, Map<String, List<ProtocolDisplayBean>>> protocolMap = provider.getProtocolsByInputInfoGrouped(ii);
        Object firstItemId = null;
        for (Entry<SharedDisplayBean, Map<String, List<ProtocolDisplayBean>>> typeEntry : protocolMap.entrySet()) {
            Object typeItemId = groupsOfProtocolTree.addItem();
            if (firstItemId == null) {
                firstItemId = typeItemId;
            }
            if ("CREDIT".equals(typeEntry.getKey().getCode())) {
                groupsOfProtocolTree.setItemIcon(typeItemId, ProtocolPortletResource.CREDIT_CARD_ICON);
            } else if ("PORTFOLIO".equals(typeEntry.getKey().getCode())) {
                groupsOfProtocolTree.setItemIcon(typeItemId, ProtocolPortletResource.BRIEFCASE_ICON);
            }
            groupsOfProtocolTree.setItemCaption(typeItemId, typeEntry.getKey().getName());
            groupsOfProtocolTree.setChildrenAllowed(typeItemId, false);
            for (Entry<String, List<ProtocolDisplayBean>> entry : typeEntry.getValue().entrySet()) {
                if (entry.getKey() == null || entry.getKey().isEmpty()) {
                    groupsMapProtocol.put(typeItemId, entry.getValue());
                } else {
                    groupsOfProtocolTree.setChildrenAllowed(typeItemId, true);
                    Object keyItemId = groupsOfProtocolTree.addItem();
                    groupsOfProtocolTree.setItemCaption(keyItemId, entry.getKey());
                    groupsOfProtocolTree.setParent(keyItemId, typeItemId);
                    groupsOfProtocolTree.setChildrenAllowed(keyItemId, false);
                    groupsMapProtocol.put(keyItemId, entry.getValue());
                }

            }
            groupsOfProtocolTree.expandItem(typeItemId);
            if (!groupsOfProtocolTree.hasChildren(typeItemId) && !groupsMapProtocol.containsKey(typeItemId)) {
                groupsOfProtocolTree.removeItem(typeItemId);
            }
        }
        int groupItemsCount = groupsOfProtocolTree.getItemIds().size();
        log.log(Level.INFO, "Group items count: {0}", groupItemsCount);
        if (groupItemsCount == 0) {
            noProtocolsLabel.setVisible(true);
            protocolLayout.setVisible(false);
            groupsTreePanel.setVisible(false);
        } else {
            noProtocolsLabel.setVisible(false);
            protocolLayout.setVisible(true);
            groupsTreePanel.setVisible(true);
            groupsOfProtocolTree.select(firstItemId);
        }
        setProtocolColumns(PROTOCOL_TABLE_COLUMNS);
    }

    private void updateProtocolTable() {
        protocolsContainer.removeAllItems();
        List<ProtocolDisplayBean> filteredProtocols = new ArrayList<ProtocolDisplayBean>();
        for (ProtocolDisplayBean protocol : listOfProtocols) {
            if (!prohibitedMessageTypeCodes.contains(protocol.getMessageTypeCode())) {
                filteredProtocols.add(protocol);
            }
        }
        protocolsContainer.addAll(filteredProtocols);
    }

    private void loadCreditorInfo() {
        creditorSelector.getSelectedElements(new SelectionCallback<Creditor>() {

            public void selected(List<Creditor> creditors) {
                loadCreditorInfo(Arrays.asList(creditors.toArray(new Creditor[0])));
            }
        });
    }

    private void loadCreditorInfo(List<Creditor> creditors) {
        filesTableLayout.setVisible(false);
        protocolLayout.setVisible(false);
        tableProtocol.removeAllItems();
        inputInfoContainer.removeAllItems();
        Date reportDate = (Date) reportDateField.getValue();
        List<InputInfoDisplayBean> inputInfoList = provider.getInputInfosByCreditors(creditors, reportDate);
        inputInfoContainer.addAll(inputInfoList);
        if (!inputInfoList.isEmpty()) {
            filesTableLayout.setVisible(true);
        } else {
            MessageBox.Show(Localization.MESSAGE_NO_DATA.getValue(), getWindow());
        }
    }

    private void exportProtocolData(ProtocolExporter exporter) {
        List<ProtocolDisplayBean> data = provider.getProtocolsByInputInfo((InputInfoDisplayBean) filesTable.getValue());
        exportProtocolData(exporter, data);
    }
}
