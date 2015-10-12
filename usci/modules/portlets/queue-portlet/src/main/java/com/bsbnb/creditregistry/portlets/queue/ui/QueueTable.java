package com.bsbnb.creditregistry.portlets.queue.ui;

import com.bsbnb.creditregistry.portlets.queue.PortalEnvironmentFacade;
import com.bsbnb.creditregistry.portlets.queue.QueueApplicationResource;
import com.bsbnb.creditregistry.portlets.queue.data.QueueFileInfo;
import com.bsbnb.vaadin.formattedtable.FormattedTable;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.ui.Table;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author Aidar.Myrzahanov
 */
public class QueueTable extends FormattedTable implements QueueFileInfo.SelectionStateChangedListener {

    private final static String[] TABLE_COLUMNS = new String[]{"rownum", "creditorName", "shortFilename", "status", "receiverDate", "protocolCount"};
    private final static String[] BANK_TABLE_COLUMNS = new String[]{"selectionCheckBox", "rownum", "shortFilename", "status", "receiverDate", "protocolCount"};
    private final static String[] EXPORT_COLUMNS = new String[]{"rownum", "creditorName", "filename", "status", "receiverDate", "protocolCount"};
    private final PortalEnvironmentFacade environment;
    private final BeanItemContainer<QueueFileInfo> container;
    private Set<QueueFileInfo> selectedFiles;

    public QueueTable(PortalEnvironmentFacade environment) {
        super(null);
        this.environment = environment;
        container = new BeanItemContainer<QueueFileInfo>(QueueFileInfo.class) {
            @Override
            public Collection<?> getSortableContainerPropertyIds() {
                return getContainerPropertyIds();
            }
        };
        setContainerDataSource(container);
        setStyleName("queue-table");
        selectedFiles = new HashSet<QueueFileInfo>();
        setImmediate(true);
    }

    private String[] getLocalizedColumnHeaders(Object[] columns) {
        String[] columnHeaders = new String[columns.length];
        for (int i = 0; i < columns.length; i++) {
            columnHeaders[i] = environment.getString("TABLE." + columns[i]);
        }
        return columnHeaders;
    }

    @Override
    public void attach() {
        if (environment.isBankUser()) {
            setVisibleColumns(BANK_TABLE_COLUMNS);
            setColumnIcon("selectionCheckBox", QueueApplicationResource.CHECKBOX_EMPTY);
            setColumnWidth("selectionCheckBox", 20);
            addListener(new ItemClickEvent.ItemClickListener() {

                public void itemClick(ItemClickEvent event) {
                    QueueFileInfo file = (QueueFileInfo) event.getItemId();
                    file.setSelected(!file.isSelected());
                }
            });
        } else {
            setVisibleColumns(TABLE_COLUMNS);
            setColumnWidth("creditorName", 200);
        }
        setColumnAlignment("protocolCount", Table.ALIGN_RIGHT);
        setColumnHeaders(getLocalizedColumnHeaders(getVisibleColumns()));

        addFormat("receiverDate", "dd.MM.yyyy HH:mm:ss");
        setWidth("100%");
    }

    @Override
    public String getColumnHeader(final Object propertyId) {
        final String originalHeader = super.getColumnHeader(propertyId);
        if (originalHeader != null) {
            final String layoutedHeader = originalHeader.replaceAll("\n", "<br />");
            return layoutedHeader;
        }
        return originalHeader;
    }

    public void load(List<QueueFileInfo> files) {
        if (environment.isBankUser()) {
            setColumnIcon("selectionCheckBox", QueueApplicationResource.CHECKBOX_EMPTY);
        }
        container.removeAllItems();
        container.addAll(files);
        selectedFiles = new HashSet<QueueFileInfo>(files.size());
        for (QueueFileInfo queueFileInfo : files) {
            queueFileInfo.addSelectionStateChangedListener((QueueFileInfo.SelectionStateChangedListener) this);
        }
    }

    public void downloadXLS() {
        downloadXls("queue.xls", EXPORT_COLUMNS, getLocalizedColumnHeaders(EXPORT_COLUMNS));
    }

    @Override
    public void sort(Object[] propertyIds, boolean[] ascending) throws UnsupportedOperationException {
        if (propertyIds.length == 1 && "selectionCheckBox".equals(propertyIds[0])) {
            boolean selectionValue = false;
            for (QueueFileInfo file : container.getItemIds()) {
                if (!file.isSelected()) {
                    selectionValue = true;
                    break;
                }
            }
            for (QueueFileInfo file : container.getItemIds()) {
                file.setSelected(selectionValue);
            }
            setSortContainerPropertyId(null);
            return;
        }
        super.sort(propertyIds, ascending);
    }

    @Override
    public void selectionStateChanged(QueueFileInfo sender) {
        if (sender.isSelected()) {
            selectedFiles.add(sender);
        } else {
            selectedFiles.remove(sender);
        }
        if (selectedFiles.size() == container.getItemIds().size()) {
            setColumnIcon("selectionCheckBox", QueueApplicationResource.CHECKBOX_CHECKED);
        } else {
            setColumnIcon("selectionCheckBox", QueueApplicationResource.CHECKBOX_EMPTY);
        }
    }

    public List<QueueFileInfo> getSelectedFiles() {
        return Collections.unmodifiableList(new ArrayList<QueueFileInfo>(selectedFiles));
    }
}
