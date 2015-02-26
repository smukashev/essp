package com.bsbnb.creditregistry.portlets.upload;

import com.vaadin.terminal.ExternalResource;

/**
 *
 * @author Aidar Myrzahanov
 */
public class UploadPortletResource extends ExternalResource {
    public UploadPortletResource(String sourceURL) {
        super("/credit-registry-upload-portlet/" + sourceURL);
    }
}
