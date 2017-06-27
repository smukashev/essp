package kz.bsbnb.usci.portlet.report;

import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.model.Role;
import com.liferay.portal.model.User;
import com.liferay.portal.util.PortalUtil;
import com.liferay.portlet.PortletPreferencesFactoryUtil;
import com.vaadin.Application;
import com.vaadin.terminal.gwt.server.PortletApplicationContext2;
import com.vaadin.terminal.gwt.server.PortletApplicationContext2.PortletListener;
import com.vaadin.ui.Window;
import kz.bsbnb.usci.eav.util.Errors;
import kz.bsbnb.usci.portlet.report.dm.DatabaseConnect;
import kz.bsbnb.usci.portlet.report.ui.MainLayout;
import org.apache.log4j.Logger;

import javax.portlet.*;
import java.security.AccessControlException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.ResourceBundle;

public class ReportApplication extends Application {

    private static final long serialVersionUID = 2096197512742005243L;
    private static final Logger logger = Logger.getLogger(ReportApplication.class);
    private static long startTimeMillis = System.currentTimeMillis();
    public static final String CONTEXT_NAME = "credit-registry-report-portlet";
    private static String viewType = "REPORT";
    private static final SimpleDateFormat DEFAULT_DATE_FORMAT = new SimpleDateFormat("dd.MM.yyyy");
    private static Date defaultReportDate = null;
    private static ResourceBundle bundle;

    public static void setStartTime() {
        startTimeMillis = System.currentTimeMillis();
        logger.info("Start time: "+ startTimeMillis);
    }

    public static void logTime(String message) {
         logger.info(message+" : "+(System.currentTimeMillis() - startTimeMillis));
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
            getMainWindow().showNotification(Errors.getError(Errors.E287), Window.Notification.TYPE_ERROR_MESSAGE);
        }

    }

    private class SamplePortletListener implements PortletListener {

        private static final long serialVersionUID = -5984011853767129565L;

        @Override
        public void handleRenderRequest(RenderRequest request, RenderResponse response, Window window) {
            setTheme("custom");
            Window mainWindow = new Window();
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

                /*if (!hasRights)
                    throw new AccessControlException(Errors.compose(Errors.E238));*/

                String portletInstanceId = (String) request.getAttribute(WebKeys.PORTLET_ID);

                PortletPreferences prefs = PortletPreferencesFactoryUtil.getPortletSetup(request, portletInstanceId);

                viewType = prefs.getValue("type", "REPORT");
                String defaultReportDateString = prefs.getValue("defaultreportdate", "01.04.2013");
                try {
                    defaultReportDate = DEFAULT_DATE_FORMAT.parse(defaultReportDateString);
                    logger.info("Parsed default date: " + defaultReportDate);
                } catch (ParseException pe) {
                    logger.info("Failed to parse date config", pe);
                }
                logger.info("Items view type: " + viewType);

                bundle = ResourceBundle.getBundle("content.Language", user.getLocale());
                DatabaseConnect connect = new DatabaseConnect(user);
                MainLayout layout = new MainLayout(connect);
                layout.setWidth("100%");
                mainWindow.addComponent(layout);
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
