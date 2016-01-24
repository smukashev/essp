package kz.bsbnb.usci.portlets.query;

import com.vaadin.terminal.ExternalResource;

/**
 *
 * @author Marat Madybayev
 */
class QueryPortletIcon extends ExternalResource {

    private static final String CONTEXT_NAME = "credit-registry-query-portlet";

    public static final QueryPortletIcon SAVE = new QueryPortletIcon("disk.png");
    public static final QueryPortletIcon OPEN = new QueryPortletIcon("folder.png");
    public static final QueryPortletIcon EXCEL = new QueryPortletIcon("excel.png");
    public static final QueryPortletIcon SETTINGS = new QueryPortletIcon("settings.png");
    public static final QueryPortletIcon BEAUTIFY = new QueryPortletIcon("beautify.png");

    private QueryPortletIcon(String sourceURL) {
        super("/" + CONTEXT_NAME + "/" + sourceURL);
    }
}
