package com.bsbnb.creditregistry.portlets.notifications.ui;

import com.bsbnb.creditregistry.portlets.notifications.PortalEnvironmentFacade;
import com.bsbnb.creditregistry.portlets.notifications.data.DataProvider;
import com.bsbnb.creditregistry.portlets.notifications.data.NotificationDisplayBean;
import com.bsbnb.vaadin.formattedtable.FormattedTable;
import com.vaadin.data.Property;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Aidar.Myrzahanov
 */
public class NotificationsComponent extends VerticalLayout {

    private static final String[] TABLE_COLUMNS = new String[]{"typeName", "creationDate", "sendingDate", "status"};
    private final PortalEnvironmentFacade environment;
    private final DataProvider dataProvider;
    private VerticalLayout messageTextLayout;

    public NotificationsComponent(PortalEnvironmentFacade environment, DataProvider dataProvider) {
        this.environment = environment;
        this.dataProvider = dataProvider;
    }

    @Override
    public void attach() {
        String[] tableColumnHeaders = new String[TABLE_COLUMNS.length];
        for (int i = 0; i < TABLE_COLUMNS.length; i++) {
            tableColumnHeaders[i] = environment.getResourceString("NOTIFICATIONS-TABLE." + TABLE_COLUMNS[i]);
        }

        messageTextLayout = new VerticalLayout();
        messageTextLayout.setImmediate(true);
        messageTextLayout.setSpacing(false);
        List<NotificationDisplayBean> notifications = dataProvider.getUserMessages(environment.getUserId());
        BeanItemContainer<NotificationDisplayBean> notificationsContainer
                = new BeanItemContainer<NotificationDisplayBean>(NotificationDisplayBean.class, notifications);
        FormattedTable messagesTable = new FormattedTable();
        messagesTable.setContainerDataSource(notificationsContainer);
        messagesTable.setVisibleColumns(TABLE_COLUMNS);
        messagesTable.setColumnHeaders(tableColumnHeaders);
        messagesTable.addFormat("creationDate", "yyyy.MM.dd HH:mm:ss");
        messagesTable.addFormat("sendingDate", "yyyy.MM.dd HH:mm:ss");
        messagesTable.setImmediate(true);
        messagesTable.setWidth("100%");
        messagesTable.setSelectable(true);
        messagesTable.setMultiSelect(false);
        messagesTable.addListener(new Property.ValueChangeListener() {
            public void valueChange(Property.ValueChangeEvent event) {
                NotificationDisplayBean item = (NotificationDisplayBean) event.getProperty().getValue();
                if (item != null) {
                    displayMessageText(item);
                }
            }
        });

        addComponent(messageTextLayout);
        addComponent(messagesTable);
        setSpacing(false);
    }

    private void displayMessageText(NotificationDisplayBean bean) {
        messageTextLayout.removeAllComponents();
        Label messageTextLabel = new Label(dataProvider.getMessageText(bean.getMessage()), Label.CONTENT_XHTML);
        messageTextLayout.addComponent(messageTextLabel);
    }
}
