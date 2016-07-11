package com.bsbnb.creditregistry.portlets.queue.ui;

import com.bsbnb.creditregistry.portlets.queue.PortalEnvironmentFacade;
import com.bsbnb.creditregistry.portlets.queue.data.DataProvider;
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
    private Tab adminTab;
    private AdministratorComponent administratorComponent;
    private MaintenanceComponent maintenanceComponent;
    private Tab maintenanceTab;

    public MainLayout(PortalEnvironmentFacade environment, DataProvider dataProvider) {
        this.environment = environment;
        this.dataProvider = dataProvider;
    }

    @Override
    public void attach() {

        QueueComponent queueComponent = new QueueComponent(environment, dataProvider);

        if (environment.isUserAdmin()) {
            TabSheet mainTabSheet = new TabSheet();
            Tab queueTab = mainTabSheet.addTab(queueComponent);
            queueTab.setCaption(environment.getString(Localization.QUEUE_TAB_NAME));
            administratorComponent = new AdministratorComponent(environment, dataProvider);
            adminTab = mainTabSheet.addTab(administratorComponent);
            adminTab.setCaption(environment.getString(Localization.ADMIN_TAB_NAME));

            maintenanceComponent = new MaintenanceComponent(environment, dataProvider);
            maintenanceTab = mainTabSheet.addTab(maintenanceComponent);
            maintenanceTab.setCaption(environment.getString(Localization.ADMIN_TAB_MAINTENANCE_NAME));

            mainTabSheet.setSizeFull();
            mainTabSheet.addListener(new TabSheet.SelectedTabChangeListener() {

                public void selectedTabChange(SelectedTabChangeEvent event) {
                    TabSheet tabSheet = event.getTabSheet();
                    Tab selectedTab = tabSheet.getTab(tabSheet.getSelectedTab());
                    if (selectedTab == adminTab) {
                        administratorComponent.initializeUI();
                    } else if(selectedTab == maintenanceTab) {
                        maintenanceComponent.initializeUI();
                    }
                }
            });
            addComponent(mainTabSheet);
        } else {
            addComponent(queueComponent);
        }
    }
}
