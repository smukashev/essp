package com.bsbnb.creditregistry.portlets.notifications.ui;

import com.bsbnb.creditregistry.portlets.notifications.PortalEnvironmentFacade;
import com.bsbnb.creditregistry.portlets.notifications.data.DataProvider;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TabSheet.SelectedTabChangeEvent;
import com.vaadin.ui.TabSheet.Tab;
import com.vaadin.ui.VerticalLayout;

/**
 *
 * @author Aidar.Myrzahanov
 */
public class MainLayout extends VerticalLayout {

    private final PortalEnvironmentFacade environment;
    private final DataProvider dataProvider;

    public MainLayout(PortalEnvironmentFacade environment, DataProvider dataProvider) {
        this.environment = environment;
        this.dataProvider = dataProvider;
    }

    @Override
    public void attach() {
        TabSheet mainTabSheet = new TabSheet();

        NotificationsComponent notificationsComponent = new NotificationsComponent(environment, dataProvider);
        Tab notificationsTab = mainTabSheet.addTab(notificationsComponent);
        notificationsTab.setCaption(environment.getResourceString(Localization.NOTIFICATIONS_TAB_NAME));

        SettingsComponent settingsComponent = new SettingsComponent(dataProvider, environment);
        Tab settingsTab = mainTabSheet.addTab(settingsComponent);
        settingsTab.setCaption(environment.getResourceString(Localization.SETTINGS_TAB_NAME));

        if (environment.isUserAdmin()) {
            final AdministratorComponent administratorComponent = new AdministratorComponent(environment, dataProvider);
            final Tab adminTab = mainTabSheet.addTab(administratorComponent);
            adminTab.setCaption(environment.getResourceString(Localization.ADMIN_TAB_NAME));
            mainTabSheet.setSizeFull();
            mainTabSheet.addListener(new TabSheet.SelectedTabChangeListener() {
                public void selectedTabChange(SelectedTabChangeEvent event) {
                    TabSheet tabSheet = event.getTabSheet();
                    Tab selectedTab = tabSheet.getTab(tabSheet.getSelectedTab());
                    if (selectedTab == adminTab) {
                        administratorComponent.initializeUI();
                    }
                }
            });
        }

        //mainTabSheet.setSelectedTab(settingsTab);
        addComponent(mainTabSheet);
    }
}
