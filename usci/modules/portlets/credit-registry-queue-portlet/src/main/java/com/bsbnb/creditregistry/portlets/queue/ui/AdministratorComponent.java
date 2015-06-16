package com.bsbnb.creditregistry.portlets.queue.ui;

import com.bsbnb.creditregistry.portlets.queue.PortalEnvironmentFacade;
import static com.bsbnb.creditregistry.portlets.queue.QueueApplication.log;
import com.bsbnb.creditregistry.portlets.queue.data.DataProvider;
import com.bsbnb.creditregistry.portlets.queue.data.QueueFileInfo;
import com.bsbnb.vaadin.messagebox.MessageBox;
import com.vaadin.data.util.AbstractBeanContainer;
import com.vaadin.data.util.BeanContainer;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Label;
import com.vaadin.ui.TwinColSelect;
import com.vaadin.ui.VerticalLayout;
import kz.bsbnb.usci.cr.model.Creditor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;

/**
 *
 * @author Aidar.Myrzahanov
 */
public class AdministratorComponent extends VerticalLayout {

    private final PortalEnvironmentFacade environment;
    private final DataProvider dataProvider;
    private ComboBox orderBox;
    private TwinColSelect creditorsSelect;
    private VerticalLayout previewLayout;

    public AdministratorComponent(PortalEnvironmentFacade environment, DataProvider dataProvider) {
        this.environment = environment;
        this.dataProvider = dataProvider;
    }

    @Override
    public void attach() {
        setImmediate(true);
    }

    public void initializeUI() {
        removeAllComponents();
        log.log(Level.INFO, "Loading admin interface");
        try {
            Button manageButton = new Button(environment.getString(Localization.START_QUEUE_PROCESSING), new Button.ClickListener() {
                public void buttonClick(ClickEvent event) {

                }
            });
            manageButton.setImmediate(true);

            orderBox = new ComboBox(environment.getString(Localization.ORDER_COMBOBOX_CAPTION));
           /* QueueOrderType[] orders = QueueOrderType.values();
            for (QueueOrderType order : orders) {
                orderBox.addItem(order);
                orderBox.setItemCaption(order, environment.getString(order.getDescription()));
            }*/
            orderBox.setImmediate(true);
            orderBox.setNullSelectionAllowed(false);
            orderBox.setWidth("400px");
            // orderBox.setValue(config.getOrderType());

            List<Creditor> creditors = dataProvider.getCreditors(environment.getUserId(), environment.isUserAdmin());
            log.log(Level.INFO, "Creditors number: {0}", creditors.size());
            final HashMap<Long, Creditor> creditorsById = new HashMap<>();
            for (Creditor creditor : creditors) {
                creditorsById.put(creditor.getId(), creditor);
            }
            List<Integer> selectedCreditorIds = new ArrayList<>();
            /* for (int priorityCreditorId : config.getPriorityCreditorIds()) {
                selectedCreditorIds.add(priorityCreditorId);
            } */
            BeanContainer<Integer, Creditor> creditorsContainer = new BeanContainer<>(Creditor.class);

           creditorsContainer.setBeanIdResolver(new AbstractBeanContainer.BeanIdResolver<Integer, Creditor>() {
                public Integer getIdForBean(Creditor bean) {
                    return bean.getId().intValue();
                }
            });
            creditorsContainer.addAll(creditors);
            creditorsSelect = new TwinColSelect(environment.getString(Localization.PRIORITY_CREDITORS_SELECT_CAPTION), creditorsContainer);
            creditorsSelect.setMultiSelect(true);
            creditorsSelect.setItemCaptionPropertyId("name");
            for (Integer selectedCreditorId : selectedCreditorIds) {
                creditorsSelect.select(selectedCreditorId);
            }
            creditorsSelect.setImmediate(true);
            creditorsSelect.setWidth("100%");

            Button saveButton = new Button(environment.getString(Localization.SAVE), new Button.ClickListener() {
                public void buttonClick(ClickEvent event) {
                    /*DatabaseQueueConfiguration config = new DatabaseQueueConfiguration(dataProvider);
                    config.setOrderType(getSelectedOrder());
                    config.setPriorityCreditorIds(getSelectedCreditorIds());
                    MessageBox.Show(environment.getString(Localization.SETTINGS_SAVED_MESSAGE), getApplication().getMainWindow());*/
                }
            });

            previewLayout = new VerticalLayout();
            previewLayout.setImmediate(true);

            Button showPreviewButton = new Button(environment.getString(Localization.SHOW_PREVIEW_BUTTON_CAPTION), new Button.ClickListener() {
                public void buttonClick(ClickEvent event) {
                    showPreview();
                }
            });

            addComponent(manageButton);
            setComponentAlignment(manageButton, Alignment.TOP_CENTER);
            addComponent(new Label("<hr/>", Label.CONTENT_XHTML));
            addComponent(orderBox);
            setComponentAlignment(orderBox, Alignment.TOP_CENTER);
            addComponent(creditorsSelect);
            setComponentAlignment(creditorsSelect, Alignment.TOP_CENTER);
            addComponent(saveButton);
            setComponentAlignment(saveButton, Alignment.TOP_CENTER);
            addComponent(new Label("<hr/>", Label.CONTENT_XHTML));
            addComponent(showPreviewButton);
            setComponentAlignment(showPreviewButton, Alignment.TOP_CENTER);

            addComponent(previewLayout);
            setSpacing(true);
        } catch (Exception ce) {
            log.log(Level.WARNING, "", ce);
        }
    }

    private void showPreview() {
        /*log.log(Level.INFO, "Showing queue preview");
        previewLayout.removeAllComponents();
        QueueConfiguration selectedConfiguration = new QueueConfiguration() {
            @Override
            public int getFilesInProcessing() throws ConfigurationException {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public long getLastLaunchMillis() throws ConfigurationException {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public QueueOrderType getOrderType() {
                return getSelectedOrder();
            }

            @Override
            public int getParallelLimit() throws ConfigurationException {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public List<Integer> getPriorityCreditorIds() {
                return getSelectedCreditorIds();
            }

            @Override
            public String getWsdlLocation() throws ConfigurationException {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public void setLastLaunchMillis(long millis) throws ConfigurationException {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public void setOrderType(QueueOrderType orderType) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public void setPriorityCreditorIds(List<Integer> ids) {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        };
        List<QueueFileInfo> files = dataProvider.getQueue(dataProvider.getCreditors(environment.getUserId(), environment.isUserAdmin()));
        QueueHandler handler = new QueueHandler(dataProvider, selectedConfiguration);
        List<QueueFileInfo> orderedFiles = handler.getOrderedFiles(files);
        QueueTable previewTable = new QueueTable(environment);
        previewTable.load(orderedFiles);
        previewLayout.addComponent(previewTable);
        log.log(Level.INFO, "Queue preview showed");*/
    }

    private List<Integer> getSelectedCreditorIds() {
        List<Integer> priorityCreditorIds = new ArrayList<Integer>();
        Object selectedValue = creditorsSelect.getValue();
        if (selectedValue instanceof Collection) {
            Collection selectedCollection = (Collection) selectedValue;
            for (Object obj : selectedCollection) {
                if (obj instanceof Integer) {
                    priorityCreditorIds.add((Integer) obj);
                }
            }
        }
        return priorityCreditorIds;
    }
}
