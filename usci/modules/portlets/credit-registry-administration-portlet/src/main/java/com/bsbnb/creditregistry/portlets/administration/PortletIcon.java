package com.bsbnb.creditregistry.portlets.administration;

import com.vaadin.terminal.ExternalResource;

/**
 *
 * @author Marat Madybayev
 */
public final class PortletIcon extends ExternalResource {
    public static final String CONTEXT_NAME = "credit-registry-administration-portlet";
    
    public static final PortletIcon DOWN_ICON = new PortletIcon("icons/down_1.png");
    public static final PortletIcon UP_ICON = new PortletIcon("icons/up_1.png");
    
    private PortletIcon(String sourceURL) {
        super("/" + CONTEXT_NAME + "/" + sourceURL);
    }
}
