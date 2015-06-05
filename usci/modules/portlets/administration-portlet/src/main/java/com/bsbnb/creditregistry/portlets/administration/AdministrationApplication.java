package com.bsbnb.creditregistry.portlets.administration;

import com.bsbnb.creditregistry.portlets.administration.data.BeanDataProvider;
import com.bsbnb.creditregistry.portlets.administration.ui.MainSplitPanel;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.model.Role;
import com.liferay.portal.model.User;
import com.liferay.portal.util.PortalUtil;
import com.vaadin.Application;
import com.vaadin.terminal.gwt.server.PortletApplicationContext2;
import com.vaadin.terminal.gwt.server.PortletRequestListener;
import com.vaadin.ui.Label;
import com.vaadin.ui.Window;
import org.apache.log4j.Logger;
import org.apache.log4j.Priority;

import javax.portlet.*;
import java.util.ResourceBundle;
import java.util.logging.Level;

/**
 * @author Marat Madybayev
 */
public class AdministrationApplication extends Application implements PortletRequestListener {

    public static final org.apache.log4j.Logger log = Logger.getLogger(AdministrationApplication.class.getName());
    private ResourceBundle bundle;

    @Override
    public void init() {
        if (getContext() instanceof PortletApplicationContext2) {
            PortletApplicationContext2 ctx =
                    (PortletApplicationContext2) getContext();

            ctx.addPortletListener(this, new SamplePortletListener());
        } else {
            getMainWindow().showNotification("Not inited via Portal!", Window.Notification.TYPE_ERROR_MESSAGE);
        }

        setMainWindow(new Window());
    }

    public void onRequestStart(PortletRequest request, PortletResponse response) {
        bundle = ResourceBundle.getBundle("content.Language", request.getLocale());
    }

    public void onRequestEnd(PortletRequest request, PortletResponse response) {
    }


    private class SamplePortletListener implements PortletApplicationContext2.PortletListener {

        private static final long serialVersionUID = -5984011853767129565L;

        @Override
        public void handleRenderRequest(RenderRequest request, RenderResponse response, Window window) {
            try {

                User user = PortalUtil.getUser(request);
                Window mainWindow = new Window();
                Label errorMessageLabel = new Label("Нет прав для просмотра");

                if(user == null) {
                    mainWindow.addComponent(errorMessageLabel);
                } else {
                    boolean isAdmin = false;
                    for(Role role : user.getRoles()) {
                        if(role.getDescriptiveName().equals("Administrator"))
                            isAdmin = true;
                    }

                    if(!isAdmin) {
                        mainWindow.addComponent(errorMessageLabel);
                    } else {
                        BeanDataProvider provider = new BeanDataProvider();
                        MainSplitPanel sp = new MainSplitPanel(bundle, provider);
                        mainWindow.addComponent(sp);
                    }
                }

                setMainWindow(mainWindow);

            } catch (PortalException pe) {
                log.log(Priority.FATAL,"",pe);
            } catch (SystemException se) {
                log.log(Priority.FATAL, "", se);
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
