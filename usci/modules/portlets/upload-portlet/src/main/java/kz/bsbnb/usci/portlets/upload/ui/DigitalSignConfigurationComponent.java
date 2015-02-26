package kz.bsbnb.usci.portlets.upload.ui;

import kz.bsbnb.usci.portlets.upload.PortletEnvironmentFacade;
import com.vaadin.data.Property;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.VerticalLayout;

/**
 *
 * @author Aidar.Myrzahanov
 */
class DigitalSignConfigurationComponent extends VerticalLayout{
    
    private PortletEnvironmentFacade environment;

    public DigitalSignConfigurationComponent(PortletEnvironmentFacade portletEnvironment) {
        this.environment = portletEnvironment;
    }
    
    @Override
    public void attach() {
        CheckBox toggleCheckBox = new CheckBox(environment.getResourceString(Localization.SEND_USING_DIGITAL_SIGNATURE.getKey()), environment.isUsingDigitalSign());
        toggleCheckBox.setEnabled(false);
        toggleCheckBox.addListener(new Property.ValueChangeListener() {

            public void valueChange(Property.ValueChangeEvent event) {
                environment.setUsingDigitalSign((Boolean) event.getProperty().getValue());
            }
        });
        addComponent(toggleCheckBox);
        setComponentAlignment(toggleCheckBox, Alignment.MIDDLE_CENTER);
    }

}
