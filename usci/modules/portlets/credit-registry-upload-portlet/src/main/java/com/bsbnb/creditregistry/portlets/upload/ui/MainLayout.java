package com.bsbnb.creditregistry.portlets.upload.ui;

import com.bsbnb.creditregistry.portlets.upload.PortletEnvironmentFacade;
import com.vaadin.ui.TabSheet;

/**
 *
 * @author Aidar.Myrzahanov
 */
public class MainLayout extends TabSheet{
    
    private PortletEnvironmentFacade portletEnvironment;
    
    public MainLayout(PortletEnvironmentFacade portletEnvironment) {
        this.portletEnvironment = portletEnvironment;
    }
    
    @Override 
    public void attach() {
        Tab singleUploadTab = addTab(new SingleUploadComponent(portletEnvironment));
        singleUploadTab.setCaption(portletEnvironment.getResourceString(Localization.SINGLE_UPLOAD_TAB_CAPTION.getKey()));
        
        Tab multiUploadTab = addTab(new MultiUploadComponent(portletEnvironment));
        multiUploadTab.setCaption(portletEnvironment.getResourceString(Localization.MULTIPLE_UPLOAD_TAB_CAPTION.getKey()));
        
        Tab digitalSigningConfigurationTab = addTab(new DigitalSignConfigurationComponent(portletEnvironment));
        digitalSigningConfigurationTab.setCaption(portletEnvironment.getResourceString(Localization.DIGITAL_SIGN_TAB_CAPTION.getKey()));
    }
    
}
