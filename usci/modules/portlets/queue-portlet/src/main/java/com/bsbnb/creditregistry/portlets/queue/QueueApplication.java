package com.bsbnb.creditregistry.portlets.queue;

import com.bsbnb.creditregistry.portlets.queue.data.BeanDataProvider;
import com.bsbnb.creditregistry.portlets.queue.ui.MainLayout;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.model.Role;
import com.liferay.portal.model.User;
import com.liferay.portal.util.PortalUtil;
import com.vaadin.Application;
import com.vaadin.terminal.gwt.server.PortletApplicationContext2;
import com.vaadin.terminal.gwt.server.PortletApplicationContext2.PortletListener;
import com.vaadin.ui.Label;
import com.vaadin.ui.Window;
import com.vaadin.ui.Window.Notification;
import org.apache.log4j.Logger;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.EventRequest;
import javax.portlet.EventResponse;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;

public class QueueApplication extends Application {

    private static final long serialVersionUID = 2096197512742005243L;
    public final Logger logger = Logger.getLogger(QueueApplication.class);
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
            User user = null;

            boolean hasRights = false;

            try {
                user = PortalUtil.getUser(PortalUtil.getHttpServletRequest(request));
                if(user != null) {
                    for (Role role : user.getRoles()) {
                        if (role.getName().equals("Administrator") || role.getName().equals("NationalBankEmployee"))
                            hasRights = true;
                    }
                }
            } catch (PortalException e) {
                logger.error(null,e);
            } catch (SystemException e) {
                logger.error(null,e);
            }

            if(!hasRights)
                return;

            if(user == null) {
                Label errorMessageLabel = new Label("Нет прав для просмотра");
                mainWindow.addComponent(errorMessageLabel);
            } else {
                QueuePortalEnvironmentFacade queuePortalEnvironmentFacade = new QueuePortalEnvironmentFacade(user);
                BeanDataProvider dataProvider = new BeanDataProvider();
                mainWindow.addComponent(new MainLayout(queuePortalEnvironmentFacade, dataProvider));
                setMainWindow(mainWindow);
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
