package com.bsbnb.usci.portlets.audit;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.EventRequest;
import javax.portlet.EventResponse;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;

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
import org.apache.log4j.Logger;

public class AuditApplication extends Application {
    
    //TODO: Использовать audit-dm
    private static final long serialVersionUID = 2096197512742005243L;

    private final Logger logger = org.apache.log4j.Logger.getLogger(AuditApplication.class);

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
                logger.error(e.getMessage(),e);
            } catch (SystemException e) {
                logger.error(e.getMessage(),e);
            }

            if(!hasRights)
                return;
            try{
                Window mainWindow = new Window();
                mainWindow.setTheme("custom");
                mainWindow.addComponent(new AuditLayout(request.getLocale()));
                setMainWindow(mainWindow);
            }catch(Exception e){
                logger.error(e.getMessage(),e);
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
