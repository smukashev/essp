package com.bsbnb.usci.portlets.crosscheck;

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

import com.bsbnb.usci.portlets.crosscheck.data.BeanDataProvider;
import com.bsbnb.usci.portlets.crosscheck.data.DataException;
import com.bsbnb.usci.portlets.crosscheck.ui.CrossCheckLayout;
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
    public static final String CONTEXT_NAME = "usci-crosscheck-portlet";
    public static final Logger log = Logger.getLogger(CrossCheckApplication.class.getName());

    public static final String CORE_SCHEMA = "CORE";
    public static final String SHOWCASE_SCHEMA = "SHOWCASE";

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
                String businessRulesUrl = prefs.getValue(ConfigurationActionImpl.BUSINESS_RULES_URL_KEY, "");
                User user = PortalUtil.getUser(request);

                CrossCheckPortletEnvironmentFacade portletData =
                        new CrossCheckPortletEnvironmentFacade(user, businessRulesUrl);
                PortletEnvironmentFacade.set(portletData);

                setTheme("custom");

                Window mainWindow = new Window(portletData.getResourceString("CrossCheckApplication"));
                try {
                    mainWindow.addComponent(new CrossCheckLayout(viewType, portletData,
                            new BeanDataProvider(portletData)));
                    
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
}
