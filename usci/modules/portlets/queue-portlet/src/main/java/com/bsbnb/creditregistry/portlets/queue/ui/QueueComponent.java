package com.bsbnb.creditregistry.portlets.queue.ui;

import com.bsbnb.creditregistry.portlets.queue.PortalEnvironmentFacade;
import com.bsbnb.creditregistry.portlets.queue.QueueApplicationResource;
import com.bsbnb.creditregistry.portlets.queue.data.DataProvider;
import com.bsbnb.creditregistry.portlets.queue.data.QueueFileInfo;
import com.bsbnb.vaadin.filterableselector.FilterableSelect;
import com.bsbnb.vaadin.filterableselector.SelectionCallback;
import com.bsbnb.vaadin.filterableselector.Selector;
import com.bsbnb.vaadin.messagebox.MessageBox;
import com.bsbnb.vaadin.messagebox.MessageBoxButtons;
import com.bsbnb.vaadin.messagebox.MessageBoxListener;
import com.bsbnb.vaadin.messagebox.MessageBoxType;
import com.bsbnb.vaadin.messagebox.MessageResult;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.ProgressIndicator;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TabSheet.Tab;
import com.vaadin.ui.VerticalLayout;
import kz.bsbnb.usci.cr.model.Creditor;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;

/**
 *
 * @author Aidar.Myrzahanov
 */
public class QueueComponent extends VerticalLayout {

    private final PortalEnvironmentFacade env;
    private final DataProvider dataProvider;
    private QueueTable table;
    private Button toXLSButton;
    private FilterableSelect<Creditor> creditorsSelect;
    private TabSheet queueTabSheet;
    private VerticalLayout summaryLayout;
    private Button removeFilesFromQueueButton;
    private HorizontalLayout autoUpdateLayout;
    private boolean autoUpdateEnabled = false;
    private static final long AUTO_UPDATE_THREAD_TIMEOUT_MILLIS = 1800*1000;
    public final Logger logger = Logger.getLogger(QueueComponent.class);

    public QueueComponent(PortalEnvironmentFacade environment, DataProvider dataProvider) {
        this.env = environment;
        this.dataProvider = dataProvider;
    }

    @Override
    public void attach() {
        setSpacing(false);
        List<Creditor> userCreditors = dataProvider.getCreditors(env.getUserId(), env.isUserAdmin());
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
        toXLSButton = new Button(env.getString(Localization.EXPORT_TO_XLS_CAPTION), new Button.ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {
                table.downloadXLS();
            }
        });
        toXLSButton.setIcon(QueueApplicationResource.EXCEL_ICON);
        toXLSButton.setVisible(false);
        Button loadButton = new Button(env.getString(Localization.LOAD), new Button.ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {
                loadTable();
                if(queueTabSheet!=null) {
                    queueTabSheet.setSelectedTab(table);
                }
            }
        });

        CheckBox enableQueueAutoUpdateCheckBox = new CheckBox(env.getString(Localization.AUTO_UPDATE), new Button.ClickListener() {

            public void buttonClick(ClickEvent event) {
                if (event.getButton().booleanValue()) {
                    autoUpdateEnabled = true;
                    ProgressIndicator indicator = new ProgressIndicator();
                    indicator.setPollingInterval(2000);
                    indicator.setIndeterminate(true);
                    Thread updateThread = new Thread(new Runnable() {

                        public void run() {
                            long startTimeMillis = System.currentTimeMillis();
                            while (autoUpdateEnabled&&System.currentTimeMillis()-startTimeMillis<AUTO_UPDATE_THREAD_TIMEOUT_MILLIS) {
                                loadTable();
                                try {
                                    Thread.sleep(1000);
                                } catch (InterruptedException ie) {
                                    logger.warn(null, ie);
                                }
                            }
                            autoUpdateEnabled = false;
                            autoUpdateLayout.removeAllComponents();
                        }
                    });
                    updateThread.start();
                    autoUpdateLayout.addComponent(indicator);
                } else {
                    autoUpdateEnabled = false;
                    autoUpdateLayout.removeAllComponents();
                }
            }
        });
        enableQueueAutoUpdateCheckBox.setImmediate(true);

        autoUpdateLayout = new HorizontalLayout();

        removeFilesFromQueueButton = new Button(env.getString(Localization.REMOVE_FILES_FROM_QUEUE_BUTTON_CAPTION), new Button.ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {
                confirmSelectedFilesRemoval();
            }
        });
        removeFilesFromQueueButton.setIcon(QueueApplicationResource.REMOVE_ICON);
        removeFilesFromQueueButton.setEnabled(false);
        HorizontalLayout buttonsLayout = new HorizontalLayout();
        buttonsLayout.addComponent(loadButton);
        buttonsLayout.setComponentAlignment(loadButton, Alignment.MIDDLE_LEFT);
        buttonsLayout.addComponent(enableQueueAutoUpdateCheckBox);
        buttonsLayout.setComponentAlignment(enableQueueAutoUpdateCheckBox, Alignment.MIDDLE_LEFT);
        buttonsLayout.addComponent(autoUpdateLayout);
        buttonsLayout.setComponentAlignment(autoUpdateLayout, Alignment.MIDDLE_LEFT);
        /*todo: add support to delete files from the queue
        if (env.isBankUser()) {
            buttonsLayout.addComponent(removeFilesFromQueueButton);
            buttonsLayout.setComponentAlignment(removeFilesFromQueueButton, Alignment.MIDDLE_LEFT);
        }*/
        buttonsLayout.addComponent(toXLSButton);
        buttonsLayout.setComponentAlignment(toXLSButton, Alignment.MIDDLE_RIGHT);
        buttonsLayout.setWidth("100%");
        buttonsLayout.setSpacing(false);
        table = new QueueTable(env) {
            @Override
            public void selectionStateChanged(QueueFileInfo file) {
                super.selectionStateChanged(file);
                setHasSelectedFiles(!getSelectedFiles().isEmpty());
            }
        };
        table.setVisible(false);

        addComponent(creditorsSelect);
        addComponent(buttonsLayout);

        if (userCreditors.size() == 1) {
            addComponent(table);
            loadTable();
        } else {
            queueTabSheet = new TabSheet();
            Tab queueTab = queueTabSheet.addTab(table);
            queueTab.setCaption(env.getString(Localization.FILES_TAB_CAPTION));

            summaryLayout = new VerticalLayout();
            Tab summaryTab = queueTabSheet.addTab(summaryLayout);
            summaryTab.setCaption(env.getString(Localization.SUMMARY_TAB_CAPTION));
            queueTabSheet.setVisible(false);
            addComponent(queueTabSheet);
        }
    }

    protected void loadTable() {
        creditorsSelect.getSelectedElements(new SelectionCallback<Creditor>() {
            @Override
            public void selected(List<Creditor> selectedItems) {
                List<QueueFileInfo> queueList = dataProvider.getQueue(selectedItems);
                Collections.sort(queueList, new Comparator<QueueFileInfo>() {
                    public int compare(QueueFileInfo o1, QueueFileInfo o2) {
                        if (o1.getStatus() == null) {
                            return -1;
                        }
                        int statusComparison = o1.getStatus().compareTo(o2.getStatus());
                        if (statusComparison != 0) {
                            return statusComparison;
                        }
                        return ((Integer) o1.getRownum()).compareTo(o2.getRownum());
                    }
                });
                table.load(queueList);
                removeFilesFromQueueButton.setEnabled(false);
                table.setVisible(true);
                toXLSButton.setVisible(true);
                if (queueTabSheet != null) {
                    queueTabSheet.setVisible(true);
                    loadSummary(queueList);
                }
            }
        });
    }

    /*
     * Метод заполняет краткое резюме по очереди загрузки
     */
    private void loadSummary(List<QueueFileInfo> files) {
        StringBuilder summaryBuilder = new StringBuilder();
        if (files.isEmpty()) {
            summaryBuilder.append("<b>").append(env.getString(Localization.QUEUE_IS_EMPTY)).append("</b>");
        } else {
            summaryBuilder.append("<b>").append(env.getString(Localization.SUMMARY_HEADER))
                    .append(" - ").append(files.size())
                    .append("</b>").append("<br/>");
            TreeMap<String, Integer> filesCountByCreditor = new TreeMap<String, Integer>();
            for (QueueFileInfo file : files) {
                if (filesCountByCreditor.containsKey(file.getCreditorName())) {
                    int currentValue = filesCountByCreditor.get(file.getCreditorName());
                    filesCountByCreditor.put(file.getCreditorName(), currentValue + 1);
                } else {
                    filesCountByCreditor.put(file.getCreditorName(), 1);
                }
            }
            ArrayList<Map.Entry<String, Integer>> entries = new ArrayList<Map.Entry<String, Integer>>(filesCountByCreditor.entrySet());
            Collections.sort(entries, new Comparator<Map.Entry<String, Integer>>() {

                public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {
                    if (o1.getValue() == o2.getValue()) {
                        if (o1.getKey() == null) {
                            return 1;
                        }
                        return -o1.getKey().compareTo(o2.getKey());
                    }
                    if (o1.getValue() == null) {
                        return 1;
                    }
                    return -o1.getValue().compareTo(o2.getValue());
                }
            });
            for (Map.Entry<String, Integer> entry : entries) {
                summaryBuilder.append(entry.getKey()).append(" - ").append(entry.getValue()).append("<br/>");
            }
        }
        Label summaryLabel = new Label(summaryBuilder.toString(), Label.CONTENT_XHTML);
        summaryLayout.removeAllComponents();
        summaryLayout.addComponent(summaryLabel);
    }

    private void setHasSelectedFiles(boolean hasSelectedFiles) {
        removeFilesFromQueueButton.setEnabled(hasSelectedFiles);
    }

    private void confirmSelectedFilesRemoval() {
        if (table.getSelectedFiles().isEmpty()) {
            MessageBox.Show(env.getString(Localization.NO_FILES_TO_REMOVE_MESSAGE_TEXT), env.getString(Localization.NO_FILES_TO_REMOVE_MESSAGE_CAPTION), getApplication().getMainWindow());
            return;
        }
        String confirmationCaption = env.getString(Localization.INPUT_INFO_REMOVAL_CONFIRMATION_CAPTION);
        boolean isSingleFile = table.getSelectedFiles().size() == 1;
        String confirmationTextTemplate;
        String confirmationText;
        if (isSingleFile) {
            confirmationTextTemplate = env.getString(Localization.INPUT_INFO_REMOVAL_CONFIRMATION_TEXT);
            confirmationText = String.format(confirmationTextTemplate, table.getSelectedFiles().get(0).getFilename());
        } else {
            confirmationTextTemplate = env.getString(Localization.INPUT_INFOS_REMOVAL_CONFIRMATION_TEXT);
            confirmationText = String.format(confirmationTextTemplate, table.getSelectedFiles().size());
        }
        MessageBox.Show(confirmationText,
                confirmationCaption,
                new MessageBoxListener() {
                    @Override
                    public void messageResult(MessageResult result) {
                        if (result == MessageResult.Yes) {
                            System.out.println("REMOVE FROM QUEUE");
                        }
                    }
                },
                MessageBoxButtons.YesNo,
                MessageBoxType.Question,
                getApplication().getMainWindow());
    }
}
