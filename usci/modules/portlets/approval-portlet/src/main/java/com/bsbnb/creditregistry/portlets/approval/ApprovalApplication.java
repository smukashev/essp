package com.bsbnb.creditregistry.portlets.approval;

import com.bsbnb.creditregistry.portlets.approval.data.BeanDataProvider;
import com.bsbnb.creditregistry.portlets.approval.ui.MainLayout;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.model.Role;
import com.liferay.portal.model.User;
import com.liferay.portal.util.PortalUtil;
import com.vaadin.Application;
import com.vaadin.terminal.gwt.server.PortletApplicationContext2;
import com.vaadin.terminal.gwt.server.PortletApplicationContext2.PortletListener;
import com.vaadin.ui.Window;
import com.vaadin.ui.Window.Notification;
import kz.bsbnb.usci.eav.util.Errors;
import org.apache.log4j.Logger;

import javax.portlet.*;
import java.security.AccessControlException;
import java.util.ResourceBundle;

public class ApprovalApplication extends Application {

    private static final long serialVersionUID = 2096197512742005243L;
    private ResourceBundle bundle;
    private final Logger logger = org.apache.log4j.Logger.getLogger(ApprovalApplication.class);

    @Override
    public void init() {
        setTheme("custom");
        setMainWindow(new Window());
        if (getContext() instanceof PortletApplicationContext2) {
            PortletApplicationContext2 ctx = (PortletApplicationContext2) getContext();
            ctx.addPortletListener(this, new SamplePortletListener());
        } else {
            getMainWindow().showNotification(Errors.getError(Errors.E287), Window.Notification.TYPE_ERROR_MESSAGE);
        }
    }

    private class SamplePortletListener implements PortletListener {
        private static final long serialVersionUID = -5984011853767129565L;

        @Override
        public void handleRenderRequest(RenderRequest request, RenderResponse response, Window window) {
            try {
                boolean hasRights = false;

                User user = PortalUtil.getUser(PortalUtil.getHttpServletRequest(request));
                if (user != null) {
                    for (Role role : user.getRoles()) {
                        if (role.getName().equals("Administrator") || role.getName().equals("BankUser")
                                || role.getName().equals("NationalBankEmployee"))
                            hasRights = true;
                    }
                }

                if (!hasRights) {
                    throw new AccessControlException(Errors.compose(Errors.E238));
                }

                setTheme("custom");

                Window mainWindow = new Window();
                mainWindow.addComponent(new MainLayout(new BeanDataProvider(),
                        new ApprovalPortletEnvironmentFacade(user)));
                setMainWindow(mainWindow);

                bundle = ResourceBundle.getBundle("content.Language", request.getLocale());
                response.setTitle(bundle.getString("WINDOW-TITLE"));
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                String exceptionMessage = e.getMessage() != null ? e.getMessage() : e.toString();
                getMainWindow().showNotification(Errors.decompose(exceptionMessage), Window.Notification.TYPE_ERROR_MESSAGE);
            }
        }

        @Override
        public void handleActionRequest(ActionRequest request,
                ActionResponse response, Window window) {
        }

        @Override
        public void handleEventRequest(EventRequest request,
                EventResponse response, Window window) {
        }

        @Override
        public void handleResourceRequest(ResourceRequest request,
                ResourceResponse response, Window window) {
        }
    }
}