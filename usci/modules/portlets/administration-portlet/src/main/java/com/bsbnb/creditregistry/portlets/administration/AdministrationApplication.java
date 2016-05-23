package com.bsbnb.creditregistry.portlets.administration;

import com.bsbnb.creditregistry.portlets.administration.data.BeanDataProvider;
import com.bsbnb.creditregistry.portlets.administration.ui.MainSplitPanel;
import com.liferay.portal.model.Role;
import com.liferay.portal.model.User;
import com.liferay.portal.util.PortalUtil;
import com.vaadin.Application;
import com.vaadin.terminal.gwt.server.PortletApplicationContext2;
import com.vaadin.terminal.gwt.server.PortletRequestListener;
import com.vaadin.ui.Window;
import kz.bsbnb.usci.eav.util.Errors;
import org.apache.log4j.Logger;

import javax.portlet.*;
import java.security.AccessControlException;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * @author Marat Madybayev
 */
public class AdministrationApplication extends Application implements PortletRequestListener {

    private final Logger logger = Logger.getLogger(AdministrationApplication.class);

    private ResourceBundle bundle;

    @Override
    public void init() {
        if (getContext() instanceof PortletApplicationContext2) {
            PortletApplicationContext2 ctx =
                    (PortletApplicationContext2) getContext();

            ctx.addPortletListener(this, new SamplePortletListener());
        } else {
            getMainWindow().showNotification(Errors.getError(Errors.E287), Window.Notification.TYPE_ERROR_MESSAGE);
        }

        setMainWindow(new Window());
    }

    public void onRequestStart(PortletRequest request, PortletResponse response) {
        bundle = ResourceBundle.getBundle("content.Language", new Locale("ru", "RU"));
    }

    public void onRequestEnd(PortletRequest request, PortletResponse response) {
    }


    private class SamplePortletListener implements PortletApplicationContext2.PortletListener {

        private static final long serialVersionUID = -5984011853767129565L;

        @Override
        public void handleRenderRequest(RenderRequest request, RenderResponse response, Window window) {
            try {
                boolean hasRights = false;

                User user = PortalUtil.getUser(PortalUtil.getHttpServletRequest(request));
                if (user != null) {
                    for (Role role : user.getRoles()) {
                        if (role.getName().equals("Administrator"))
                            hasRights = true;
                    }
                }

                if (!hasRights)
                    throw new AccessControlException(Errors.compose(Errors.E238));


                setTheme("custom");
                Window mainWindow = new Window();
                BeanDataProvider provider = new BeanDataProvider();
                MainSplitPanel sp = new MainSplitPanel(bundle, provider);
                mainWindow.addComponent(sp);
                setMainWindow(mainWindow);
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
