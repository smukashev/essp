package com.bsbnb.creditregistry.portlets.crosscheck;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.EventRequest;
import javax.portlet.EventResponse;
import javax.portlet.PortletContext;
import javax.portlet.PortletException;
import javax.portlet.PortletPreferences;
import javax.portlet.PortletRequestDispatcher;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;

import com.bsbnb.creditregistry.portlets.crosscheck.data.BeanDataProvider;
import com.bsbnb.creditregistry.portlets.crosscheck.data.DataException;
import com.bsbnb.creditregistry.portlets.crosscheck.ui.CrossCheckLayout;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.model.User;
import com.liferay.portal.util.PortalUtil;
import com.liferay.portlet.PortletPreferencesFactoryUtil;
import com.vaadin.Application;
import com.vaadin.terminal.gwt.server.PortletApplicationContext2;
import com.vaadin.terminal.gwt.server.PortletApplicationContext2.PortletListener;
import com.vaadin.ui.Label;
import com.vaadin.ui.Window;
import com.vaadin.ui.Window.Notification;

public class CrossCheckApplication extends Application {

    private static final long serialVersionUID = 2096197512742005243L;
    public static final String CONTEXT_NAME = "credit-registry-crosscheck-portlet";
    public static final Logger log = Logger.getLogger(CrossCheckApplication.class.getName());

    @Override
    public void init() {
        setTheme("custom");
        setMainWindow(new Window());

        if (getContext() instanceof PortletApplicationContext2) {
            PortletApplicationContext2 ctx =
                    (PortletApplicationContext2) getContext();

            ctx.addPortletListener(this, new SamplePortletListener());
        } else {
            getMainWindow().showNotification("Not inited via Portal!", Notification.TYPE_ERROR_MESSAGE);
        }

    }

    private class SamplePortletListener implements PortletListener {

        private static final long serialVersionUID = -5984011853767129565L;

        @Override
        public void handleRenderRequest(RenderRequest request, RenderResponse response, Window window) {
            try {
                String portletInstanceId = (String) request.getAttribute(WebKeys.PORTLET_ID);

                PortletPreferences prefs = PortletPreferencesFactoryUtil.getPortletSetup(request, portletInstanceId);

                String viewType = prefs.getValue("type", "WORK");
                log.log(Level.INFO, "Items view type: {0}", viewType);
                String businessRulesUrl = prefs.getValue(ConfigurationActionImpl.BUSINESS_RULES_URL_KEY, "");
                User user = PortalUtil.getUser(request);
                CrossCheckPortletEnvironmentFacade portletData = new CrossCheckPortletEnvironmentFacade(user, businessRulesUrl);
                PortletEnvironmentFacade.set(portletData);
                setTheme("custom");
                Window mainWindow = new Window(portletData.getResourceString("CrossCheckApplication"));
                try {
                    mainWindow.addComponent(new CrossCheckLayout(viewType, portletData, new BeanDataProvider(portletData)));
                } catch (DataException de) {
                    mainWindow.addComponent(new Label(de.getMessage().replaceAll("\n", "<br/>")));
                }
                setMainWindow(mainWindow);
            } catch (PortalException pe) {
                log.log(Level.WARNING, "", pe);
            } catch (SystemException se) {
                log.log(Level.WARNING, "", se);
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
}
