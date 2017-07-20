package com.bsbnb.usci.portlets.crosscheck;

import com.bsbnb.usci.portlets.crosscheck.data.BeanDataProvider;
import com.bsbnb.usci.portlets.crosscheck.data.DataException;
import com.bsbnb.usci.portlets.crosscheck.ui.CrossCheckLayout;
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
import kz.bsbnb.usci.eav.util.Errors;
import org.apache.log4j.Logger;

import javax.portlet.*;
import java.security.AccessControlException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ResourceBundle;

public class CrossCheckApplication extends Application {

    private static final long serialVersionUID = 2096197512742005243L;
    public static final String CONTEXT_NAME = "usci-crosscheck-portlet";
    private final Logger logger = Logger.getLogger(CrossCheckApplication.class);
    private ResourceBundle bundle;
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

                String portletInstanceId = (String) request.getAttribute(WebKeys.PORTLET_ID);
                PortletPreferences prefs = PortletPreferencesFactoryUtil.getPortletSetup(request, portletInstanceId);
                String viewType = prefs.getValue("type", "WORK");
                String businessRulesUrl = prefs.getValue(ConfigurationActionImpl.BUSINESS_RULES_URL_KEY, "");
                //User user = PortalUtil.getUser(request);

                Date repDate=null;
                if(PortalUtil.getOriginalServletRequest(PortalUtil.getHttpServletRequest(request)).getParameter("repDate")!=null)
                    repDate = formatter.parse(PortalUtil.getOriginalServletRequest(PortalUtil.getHttpServletRequest(request)).getParameter("repDate"));
                String creditorId =  PortalUtil.getOriginalServletRequest(PortalUtil.getHttpServletRequest(request)).getParameter("creditorId");

                CrossCheckPortletEnvironmentFacade portletData =
                        new CrossCheckPortletEnvironmentFacade(user, businessRulesUrl, repDate, creditorId);
                PortletEnvironmentFacade.set(portletData);

                setTheme("custom");

                Window mainWindow = new Window(portletData.getResourceString("CrossCheckApplication"));
                try {
                    mainWindow.addComponent(new CrossCheckLayout(viewType, portletData, new BeanDataProvider(portletData), request.getLocale()));
                } catch (DataException de) {
                    logger.error(Errors.decompose(de.getMessage()));
                    mainWindow.addComponent(new Label(de.getMessage().replaceAll("\n", "<br/>")));
                }
                setMainWindow(mainWindow);

                bundle = ResourceBundle.getBundle("content.Language", request.getLocale());
                response.setTitle(bundle.getString("WindowsTitle"));
            } catch (Exception e) {
                logger.error(e.getMessage(),e);
                String exceptionMessage = e.getMessage() != null ? e.getMessage() : e.getClass().getName();
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
