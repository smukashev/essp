package kz.bsbnb.usci.portlets.upload.ui;

import com.bsbnb.vaadin.messagebox.MessageBox;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.TwinColSelect;
import com.vaadin.ui.VerticalLayout;
import kz.bsbnb.usci.cr.model.Creditor;
import kz.bsbnb.usci.portlets.upload.PortletEnvironmentFacade;
import kz.bsbnb.usci.portlets.upload.data.DataProvider;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author Aidar.Myrzahanov
 */
public class ConfigurationComponent extends VerticalLayout {

    private final PortletEnvironmentFacade env;
    private final DataProvider provider;

    public ConfigurationComponent(PortletEnvironmentFacade env, DataProvider provider) {
        this.env = env;
        this.provider = provider;
    }

    @Override
    public void attach() {
        List<Creditor> organizations = provider.getOrganizations();
        BeanItemContainer<Creditor> organizationsContainer = new BeanItemContainer<Creditor>(Creditor.class, organizations);
        Set<Integer> idsSet = new HashSet<Integer>(provider.getIdsForOrganizationsUsingDigitalSigning());
        final TwinColSelect organizationsSelect = new TwinColSelect(env.getResourceString(Localization.DIGITAL_SIGNING_ORGANIZATIONS_SELECT_CAPTION.getKey()), organizationsContainer);
        organizationsSelect.setItemCaptionPropertyId("name");
        organizationsSelect.setMultiSelect(true);
        for (Creditor organization : organizations) {
            if (idsSet.contains(organization.getId().intValue())) {
                organizationsSelect.select(organization);
            }
        }
        organizationsSelect.setImmediate(true);
        organizationsSelect.setWidth("100%");

        Button saveButton = new Button(env.getResourceString(Localization.SAVE_CONFIGURATION_BUTTON_CAPTION.getKey()), new Button.ClickListener() {

            public void buttonClick(Button.ClickEvent event) {
                Object value = organizationsSelect.getValue();
                if (value instanceof Collection) {
                    List<Creditor> selectedOrganizations = new ArrayList<Creditor>((Collection<Creditor>) value);
                    provider.saveOrganizationsUsingDigitalSigning(selectedOrganizations);
                    MessageBox.Show(env.getResourceString(Localization.CONFIGURATION_SAVED_MESSAGE.getKey()), getWindow());
                }
            }
        });

        addComponent(organizationsSelect);
        setComponentAlignment(organizationsSelect, Alignment.MIDDLE_CENTER);
        addComponent(saveButton);
        setComponentAlignment(saveButton, Alignment.MIDDLE_CENTER);
    }
}
