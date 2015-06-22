package com.bsbnb.creditregistry.portlets.notifications.ui;

//import com.bsbnb.creditregistry.dm.maintenance.mail.UserMailTemplate;
import com.bsbnb.creditregistry.portlets.notifications.PortalEnvironmentFacade;
import com.bsbnb.creditregistry.portlets.notifications.data.DataProvider;
import com.bsbnb.vaadin.messagebox.MessageBox;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import kz.bsbnb.usci.eav.model.mail.UserMailTemplate;

import java.util.List;

/**
 *
 * @author Aidar.Myrzahanov
 */
public class SettingsComponent extends VerticalLayout {

    private final DataProvider provider;
    private final PortalEnvironmentFacade environment;
    private List<UserMailTemplate> userSettings;

    public SettingsComponent(DataProvider provider, PortalEnvironmentFacade environment) {
        this.provider = provider;
        this.environment = environment;
    }

    @Override
    public void attach() {
        userSettings = provider.getMailSettings(environment.getUserId());
        
        Label headerLabel = new Label(environment.getResourceString(Localization.SETTINGS_HEADER), Label.CONTENT_XHTML);

        VerticalLayout checkBoxLayout = new VerticalLayout();
        checkBoxLayout.setSpacing(false);

        for (final UserMailTemplate userMailTemplate : userSettings) {
            CheckBox settingCheckBox = new CheckBox(userMailTemplate.getMailTemplate().getNameRu(), new Button.ClickListener() {

                public void buttonClick(Button.ClickEvent event) {
                    userMailTemplate.setEnabled((Boolean) event.getButton().getValue());
                }
            });
            settingCheckBox.setValue(userMailTemplate.isEnabled());
            checkBoxLayout.addComponent(settingCheckBox);
            checkBoxLayout.setComponentAlignment(settingCheckBox, Alignment.MIDDLE_LEFT);
        }

        checkBoxLayout.setSizeUndefined();

        Button saveButton = new Button(environment.getResourceString(Localization.SAVE), new Button.ClickListener() {
            public void buttonClick(Button.ClickEvent event) {
                saveSettings();
            }
        });

        setSpacing(true);
        addComponent(headerLabel);
        setComponentAlignment(headerLabel, Alignment.MIDDLE_CENTER);
        addComponent(checkBoxLayout);
        setComponentAlignment(checkBoxLayout, Alignment.MIDDLE_CENTER);
        addComponent(saveButton);
        setComponentAlignment(saveButton, Alignment.MIDDLE_CENTER);
    }

    private void saveSettings() {
        provider.saveUserSettings(userSettings);
        MessageBox.Show(environment.getResourceString(Localization.SETTINGS_SAVED_MESSAGE),
                environment.getResourceString(Localization.SETTINGS_SAVED_MESSAGE),
                getApplication().getMainWindow());
    }
}
