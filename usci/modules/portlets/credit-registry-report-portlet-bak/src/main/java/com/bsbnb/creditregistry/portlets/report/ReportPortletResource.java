package com.bsbnb.creditregistry.portlets.report;

import com.vaadin.terminal.ExternalResource;

/**
 *
 * @author Marat Madybayev
 */
public class ReportPortletResource extends ExternalResource {
    public static final ReportPortletResource OK_ICON = new ReportPortletResource("accept.png");
    public static final ReportPortletResource EXCEL_ICON = new ReportPortletResource("excel.png");
    public static final ReportPortletResource TABLE_ICON = new ReportPortletResource("table.png");
    public static final ReportPortletResource ARCHIVE_ICON = new ReportPortletResource("archive.png");
    public static final ReportPortletResource ARROW_LEFT_ICON = new ReportPortletResource("arrow_left.png");
    public static final ReportPortletResource DOWNLOAD_ICON = new ReportPortletResource("download.png");
    public static final ReportPortletResource REFRESH_ICON = new ReportPortletResource("refresh.png");
    
    private ReportPortletResource(String sourceURL) {
        super("/" + ReportApplication.CONTEXT_NAME + "/" + sourceURL);
    }
}
