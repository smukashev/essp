package com.bsbnb.usci.portlets.protocol;

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

import com.bsbnb.usci.portlets.protocol.data.BeanDataProvider;
import com.bsbnb.usci.portlets.protocol.data.DataProvider;
import com.bsbnb.usci.portlets.protocol.ui.ProtocolLayout;
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

public class ProtocolApplication extends Application {
    private static final long serialVersionUID = 2096197512742005243L;
    public static final String CONTEXT_NAME = "protocol_portlet";
    public static final Logger log = Logger.getLogger(ProtocolApplication.class.getCanonicalName());

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

    private class SamplePortletListener implements PortletListener {

        private static final long serialVersionUID = -5984011853767129565L;

        @Override
        public void handleRenderRequest(RenderRequest request, RenderResponse response, Window window) {
            boolean hasRights = false;
            boolean isNB = false;
            User user = null;

            try {
                user = PortalUtil.getUser(PortalUtil.getHttpServletRequest(request));
                if(user != null) {
                    for (Role role : user.getRoles()) {
                        if (role.getName().equals("Administrator") || role.getName().equals("BankUser")
                                || role.getName().equals("NationalBankEmployee")) {
                            hasRights = true;

                            if (role.getName().equals("NationalBankEmployee")) {
                                isNB = true;
                            }
                        }
                    }
                }
            } catch (PortalException e) {
                e.printStackTrace();
            } catch (SystemException e) {
                e.printStackTrace();
            }

            if(!hasRights)
                return;

            setTheme("custom");
            log.log(Level.INFO, "User ID: {0}", user.getUserId());
            Window mainWindow = new Window();
            PortletEnvironmentFacade.set(new ProtocolPortletEnvironmentFacade(user, isNB));
            DataProvider provider = new BeanDataProvider();
            mainWindow.addComponent(new ProtocolLayout(provider));
            setMainWindow(mainWindow);
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