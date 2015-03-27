package com.bsbnb.usci.portlets.crosscheck;

import com.vaadin.terminal.ExternalResource;

/**
 *
 * @author Aidar.Myrzahanov
 */
public class CrossCheckPortletResource extends ExternalResource {

    public CrossCheckPortletResource(String sourceURL) {
        super("/" + CrossCheckApplication.CONTEXT_NAME + "/" + sourceURL);
    }
}
