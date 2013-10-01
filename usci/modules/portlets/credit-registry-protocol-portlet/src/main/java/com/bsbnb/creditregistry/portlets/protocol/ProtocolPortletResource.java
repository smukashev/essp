package com.bsbnb.creditregistry.portlets.protocol;

import com.vaadin.terminal.ExternalResource;

/**
 *
 * @author Aidar Myrzahanov
 */
public class ProtocolPortletResource extends ExternalResource {
    
    public static final ProtocolPortletResource OK_ICON = new ProtocolPortletResource("accept.png");
    public static final ProtocolPortletResource WARNING_ICON = new ProtocolPortletResource("error.png");
    public static final ProtocolPortletResource CRITICAL_ERROR_ICON = new ProtocolPortletResource("cancel.png");
    public static final ProtocolPortletResource INFO_ICON = new ProtocolPortletResource("exclamation.png");
    public static final ProtocolPortletResource EXCEL_ICON = new ProtocolPortletResource("excel.png");
    public static final ProtocolPortletResource TXT_ICON = new ProtocolPortletResource("txt.png");
    public static final ProtocolPortletResource XML_ICON = new ProtocolPortletResource("xml.png");
    public static final ProtocolPortletResource TREE_ICON = new ProtocolPortletResource("tree.png");
    public static final ProtocolPortletResource CREDIT_CARD_ICON = new ProtocolPortletResource("credit-card.png");
    public static final ProtocolPortletResource BRIEFCASE_ICON = new ProtocolPortletResource("briefcase.png");
    
    private ProtocolPortletResource(String sourceURL) {
        super("/" + ProtocolApplication.CONTEXT_NAME + "/" + sourceURL);
    }
}
