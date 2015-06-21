package com.bsbnb.creditregistry.portlets.notifications;

import com.vaadin.terminal.ExternalResource;

/**
 *
 * @author Aidar.Myrzahanov
 */
public class NotificationsApplicationResource extends ExternalResource {

    private static final String CONTEXT_NAME = "credit-registry-queue-portlet";
    public static final NotificationsApplicationResource EXCEL_ICON = new NotificationsApplicationResource("excel_table.png");
    public static final NotificationsApplicationResource REFRESH_ICON = new NotificationsApplicationResource("refresh.png");
    public static final NotificationsApplicationResource DOWNLOAD_ICON = new NotificationsApplicationResource("download.png");

    public NotificationsApplicationResource(String sourceURL) {
        super("/" + CONTEXT_NAME + "/" + sourceURL);
    }
}
