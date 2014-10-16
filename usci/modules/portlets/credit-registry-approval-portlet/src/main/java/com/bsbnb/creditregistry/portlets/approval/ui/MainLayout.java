package com.bsbnb.creditregistry.portlets.approval.ui;

import com.bsbnb.creditregistry.dm.ref.Creditor;
import com.bsbnb.creditregistry.portlets.approval.PortletEnvironmentFacade;
import com.bsbnb.creditregistry.portlets.approval.data.DataProvider;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import java.util.List;

public class MainLayout extends VerticalLayout {

    private DataProvider provider;
    private PortletEnvironmentFacade environment;

    public MainLayout(DataProvider provider, PortletEnvironmentFacade environment) {
        this.provider = provider;
        this.environment = environment;
    }

    @Override
    public void attach() {
        String errorMessage = null;
        if (environment.isNbUser()) {
            CreditorsListLayout creditorsListLayout = new CreditorsListLayout(provider, environment);
            addComponent(creditorsListLayout);
        } else if (environment.isBankUser()) {
            List<Creditor> userCreditors = provider.getCreditorsList(environment.getUserID());
            if (userCreditors.size() == 1) {
                ReportDateLayout reportDateLayout = new ReportDateLayout(provider, environment, userCreditors.get(0));
                addComponent(reportDateLayout);
            } else if (userCreditors.isEmpty()) {
                errorMessage = environment.getResourceString(Localization.MESSAGE_USER_HAS_NO_ACCESS_TO_CREDITORS);
            } else {
                errorMessage = environment.getResourceString(Localization.MESSAGE_USER_HAS_ACCESS_TO_MULTIPLE_CREDITORS);
            }
        } else {
            errorMessage = environment.getResourceString(Localization.MESSAGE_ONLY_BANK_AND_NB_USERS_CAN_ACCESS_APP);
        }
        if (errorMessage != null) {
            Label errorMessageLabel = new Label("<h2>" + errorMessage + "</h2>", Label.CONTENT_XHTML);
            addComponent(errorMessageLabel);
        }
    }
}
