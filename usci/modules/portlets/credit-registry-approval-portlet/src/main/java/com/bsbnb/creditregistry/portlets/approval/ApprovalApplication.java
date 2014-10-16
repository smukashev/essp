package com.bsbnb.creditregistry.portlets.approval;

import com.bsbnb.creditregistry.portlets.approval.data.BeanDataProvider;
import com.bsbnb.creditregistry.portlets.approval.ui.MainLayout;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.model.User;
import com.liferay.portal.util.PortalUtil;
import com.vaadin.Application;
import com.vaadin.terminal.gwt.server.PortletApplicationContext2;
import com.vaadin.terminal.gwt.server.PortletApplicationContext2.PortletListener;
import com.vaadin.ui.Window;
import com.vaadin.ui.Window.Notification;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.EventRequest;
import javax.portlet.EventResponse;
import javax.portlet.PortletContext;
import javax.portlet.PortletException;
import javax.portlet.PortletRequestDispatcher;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;

public class ApprovalApplication extends Application {

    private static final long serialVersionUID = 2096197512742005243L;
    
    public static final Logger log = Logger.getLogger(ApprovalApplication.class.getCanonicalName());

    @Override
    public void init() {
        setTheme("custom");
        setMainWindow(new Window());
        if (getContext() instanceof PortletApplicationContext2) {
            PortletApplicationContext2 ctx = (PortletApplicationContext2) getContext();
            ctx.addPortletListener(this, new SamplePortletListener());
        } else {
            getMainWindow().showNotification("Not inited via Portal!", Notification.TYPE_ERROR_MESSAGE);
        }
    }

    /**
     * Показывает краткую страницу JSP для не-максимального режима просмотра
     */
    public void writeViewPageHtml(RenderRequest request, RenderResponse response, Window window, String path)
            throws IOException, PortletException {
        PortletContext context = null;
        // только portlet 2.0
        if (getContext() instanceof PortletApplicationContext2) {
            PortletApplicationContext2 ctx = (PortletApplicationContext2) getContext();
            context = ctx.getPortletSession().getPortletContext();
        }
        if (context != null) {
            PortletRequestDispatcher portletRequestDispatcher = context.getRequestDispatcher(path);
            if (portletRequestDispatcher != null) {
                portletRequestDispatcher.include(request, response);
            }
        }
    }

    private class SamplePortletListener implements PortletListener {

        private static final long serialVersionUID = -5984011853767129565L;

        @Override
        public void handleRenderRequest(RenderRequest request, RenderResponse response, Window window) {
            try {
                User user = PortalUtil.getUser(request);
                if (user == null) {
                    return;
                }
                setTheme("custom");
                log.log(Level.INFO, "User ID: {0}", user.getUserId());
                Window mainWindow = new Window();
                mainWindow.addComponent(new MainLayout(new BeanDataProvider(), new ApprovalPortletEnvironmentFacade(user)));
                setMainWindow(mainWindow);
            } catch (PortalException pe) {
                log.log(Level.WARNING, null, pe);
            } catch (SystemException se) {
                log.log(Level.WARNING, null, se);
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