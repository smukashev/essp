package com.bsbnb.usci.portlets.crosscheck;

import com.bsbnb.usci.portlets.crosscheck.data.BeanDataProvider;
import com.bsbnb.usci.portlets.crosscheck.data.DataException;
import com.bsbnb.usci.portlets.crosscheck.ui.CrossCheckLayout;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.model.Role;
import com.liferay.portal.model.User;
import com.liferay.portal.util.PortalUtil;
import com.liferay.portlet.PortletPreferencesFactoryUtil;
import com.vaadin.Application;
import com.vaadin.terminal.gwt.server.PortletApplicationContext2;
import com.vaadin.terminal.gwt.server.PortletApplicationContext2.PortletListener;
import com.vaadin.ui.Label;
import com.vaadin.ui.Window;
import com.vaadin.ui.Window.Notification;

import javax.portlet.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CrossCheckApplication extends Application {

    private static final long serialVersionUID = 2096197512742005243L;
    public static final String CONTEXT_NAME = "usci-crosscheck-portlet";
    public static final Logger log = Logger.getLogger(CrossCheckApplication.class.getName());

    DateFormat formatter = new SimpleDateFormat("dd.MM.yyyy");

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

                boolean hasRights = false;

                try {
                    User user = PortalUtil.getUser(PortalUtil.getHttpServletRequest(request));
                    if(user != null) {
                        for (Role role : user.getRoles()) {
                            if (role.getName().equals("Administrator") || role.getName().equals("BankUser")
                                    || role.getName().equals("NationalBankEmployee"))
                                hasRights = true;
                        }
                    }
                } catch (PortalException e) {
                    e.printStackTrace();
                } catch (SystemException e) {
                    e.printStackTrace();
                }

                if(!hasRights)
                    return;

                String portletInstanceId = (String) request.getAttribute(WebKeys.PORTLET_ID);
                PortletPreferences prefs = PortletPreferencesFactoryUtil.getPortletSetup(request, portletInstanceId);
                String viewType = prefs.getValue("type", "WORK");
                String businessRulesUrl = prefs.getValue(ConfigurationActionImpl.BUSINESS_RULES_URL_KEY, "");
                User user = PortalUtil.getUser(request);

                Date repDate =  (PortalUtil.getOriginalServletRequest(PortalUtil.getHttpServletRequest(request)).getParameter("repDate")!=null)?
                        formatter.parse(PortalUtil.getOriginalServletRequest(PortalUtil.getHttpServletRequest(request)).getParameter("repDate")):new Date();
                String creditorId =  PortalUtil.getOriginalServletRequest(PortalUtil.getHttpServletRequest(request)).getParameter("creditorId");

                CrossCheckPortletEnvironmentFacade portletData =
                        new CrossCheckPortletEnvironmentFacade(user, businessRulesUrl, repDate, creditorId);
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
            } catch (ParseException e) {
                e.printStackTrace();
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
