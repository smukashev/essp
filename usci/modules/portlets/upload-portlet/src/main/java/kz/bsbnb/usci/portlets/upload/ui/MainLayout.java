package kz.bsbnb.usci.portlets.upload.ui;

import kz.bsbnb.usci.portlets.upload.PortletEnvironmentFacade;
import com.vaadin.ui.TabSheet;
import kz.bsbnb.usci.portlets.upload.data.BeanDataProvider;
import kz.bsbnb.usci.portlets.upload.data.DataProvider;

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
        DataProvider provider = new BeanDataProvider();
        Tab singleUploadTab = addTab(new SingleUploadComponent(portletEnvironment));
        singleUploadTab.setCaption(portletEnvironment.getResourceString(Localization.SINGLE_UPLOAD_TAB_CAPTION.getKey()));
        
        Tab multiUploadTab = addTab(new MultiUploadComponent(portletEnvironment));
        multiUploadTab.setCaption(portletEnvironment.getResourceString(Localization.MULTIPLE_UPLOAD_TAB_CAPTION.getKey()));
        
        Tab digitalSigningConfigurationTab = addTab(new DigitalSignConfigurationComponent(portletEnvironment));
        digitalSigningConfigurationTab.setCaption(portletEnvironment.getResourceString(Localization.DIGITAL_SIGN_TAB_CAPTION.getKey()));

        if (portletEnvironment.isNB()) {
            Tab configurationTab = addTab(new ConfigurationComponent(portletEnvironment, provider));
            configurationTab.setCaption(portletEnvironment.getResourceString(Localization.CONFIGURATION_TAB_CAPTION.getKey()));

            Tab defaultDateConfigTab = addTab(new DefaultDateConfComponent(portletEnvironment, provider));
            defaultDateConfigTab.setCaption("Настройка первой отчетной даты");
        }
    }
    
}
