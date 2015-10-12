package com.bsbnb.creditregistry.portlets.queue;

import com.vaadin.terminal.ExternalResource;

/**
 *
 * @author Aidar.Myrzahanov
 */
public class QueueApplicationResource extends ExternalResource {

    private static final String CONTEXT_NAME = "queue-portlet";
    public static final QueueApplicationResource EXCEL_ICON = new QueueApplicationResource("excel_table.png");
    public static final QueueApplicationResource DOWNLOAD_ICON = new QueueApplicationResource("download.png");
    public static final QueueApplicationResource REMOVE_ICON = new QueueApplicationResource("remove.png");
    public static final QueueApplicationResource CHECKBOX_CHECKED = new QueueApplicationResource("checkbox_checked.png");
    public static final QueueApplicationResource CHECKBOX_EMPTY = new QueueApplicationResource("checkbox_empty.png");

    public QueueApplicationResource(String sourceURL) {
        super("/" + CONTEXT_NAME + "/" + sourceURL);
    }
}
