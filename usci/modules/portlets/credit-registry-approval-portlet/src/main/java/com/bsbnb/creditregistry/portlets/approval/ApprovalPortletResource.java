package com.bsbnb.creditregistry.portlets.approval;

import com.vaadin.terminal.ExternalResource;

/**
 *
 * @author Aidar Myrzahanov
 */
public class ApprovalPortletResource extends ExternalResource {
    
    private static final String CONTEXT_NAME = "credit-registry-approval-portlet";
    
    public static final ApprovalPortletResource EXCEL_ICON = new ApprovalPortletResource("excel.png");
    public static final ApprovalPortletResource ARROW_LEFT_ICON = new ApprovalPortletResource("arrow_left.png");
    public static final ApprovalPortletResource ATTACHMENT_ICON = new ApprovalPortletResource("attach.png");
    public static final ApprovalPortletResource ARROW_BACK_ICON = new ApprovalPortletResource("arrow_back.png");
    public static final ApprovalPortletResource ARROW_FORWARD_ICON = new ApprovalPortletResource("arrow_forward.png");
    
    private ApprovalPortletResource(String sourceURL) {
        super("/" + CONTEXT_NAME + "/" + sourceURL);
    }
}
