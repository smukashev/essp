package com.bsbnb.creditregistry.portlets.report;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.EventRequest;
import javax.portlet.EventResponse;
import javax.portlet.PortletPreferences;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;

import com.bsbnb.creditregistry.portlets.report.dm.DatabaseConnect;
import com.bsbnb.creditregistry.portlets.report.ui.MainLayout;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.model.User;
import com.liferay.portal.util.PortalUtil;
import com.liferay.portlet.PortletPreferencesFactoryUtil;
import com.vaadin.Application;
import com.vaadin.terminal.gwt.server.PortletApplicationContext2;
import com.vaadin.terminal.gwt.server.PortletApplicationContext2.PortletListener;
import com.vaadin.ui.Window;
import com.vaadin.ui.Window.Notification;

public class ReportApplication extends Application {

    private static final long serialVersionUID = 2096197512742005243L;
    public static final Logger log = Logger.getLogger(ReportApplication.class.getCanonicalName());
    private static long startTimeMillis = System.currentTimeMillis();
    public static final String CONTEXT_NAME = "credit-registry-report-portlet";
    private static String viewType = "REPORT";
    private static final SimpleDateFormat DEFAULT_DATE_FORMAT = new SimpleDateFormat("dd.MM.yyyy");
    private static Date defaultReportDate = null;
    private static ResourceBundle bundle;
    
    
    public static void setStartTime() {
        startTimeMillis = System.currentTimeMillis();
        log.log(Level.INFO, "Start time: {0}", startTimeMillis);
    }
    
    public static void logTime(String message) {
        log.log(Level.INFO, "{0}: {1}", new Object[]{ message, System.currentTimeMillis()-startTimeMillis});
    }
    
    public static void logTime() {
        logTime("Current time");
    }

    public static String getReportType() {
        return viewType;
    }

    public static Locale getApplicationLocale() {
        return bundle.getLocale();
    }

    public static String getResourceString(String key) {
        return bundle.getString(key);
    }

    public static Date getDefaultReportDate() {
        return defaultReportDate;
    }

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
            setTheme("custom");
            Window mainWindow = new Window();
            try {
                User user = PortalUtil.getUser(request);
                if (user != null) {
                    try {
                        String portletInstanceId = (String) request.getAttribute(WebKeys.PORTLET_ID);

                        PortletPreferences prefs = PortletPreferencesFactoryUtil.getPortletSetup(request, portletInstanceId);

                        viewType = prefs.getValue("type", "REPORT");
                        String defaultReportDateString = prefs.getValue("defaultreportdate", "");
                        try {
                            if (defaultReportDateString.trim().equals("")) {
                                defaultReportDate = new Date();
                            } else {
                                defaultReportDate = DEFAULT_DATE_FORMAT.parse(defaultReportDateString);
                            }
                            log.log(Level.INFO, "Parsed default date: {0}", defaultReportDate);
                        } catch (ParseException pe) {
                            log.log(Level.INFO, "Failed to parse date config", pe);
                        }
                        log.log(Level.INFO, "Items view type: {0}", viewType);
                    } catch (SystemException se) {
                        log.log(Level.WARNING, null, se);
                    }
                    bundle = ResourceBundle.getBundle("content.Language", request.getLocale());
                    DatabaseConnect connect = new DatabaseConnect(user);
                    MainLayout layout = new MainLayout(connect);
                    layout.setWidth("100%");
                    mainWindow.addComponent(layout);
                    setMainWindow(mainWindow);
                }
            } catch (PortalException pe) {
                log.log(Level.SEVERE, "Failed to access user", pe);
            } catch(SystemException se) {
                log.log(Level.SEVERE, "Failed to access user", se);
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
