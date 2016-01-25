package com.bsbnb.vaadin.base.portlet;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.vaadin.Application;
import com.vaadin.terminal.gwt.server.PortletApplicationContext2;
import com.vaadin.terminal.gwt.server.PortletApplicationContext2.PortletListener;
import com.vaadin.ui.Window;
import com.vaadin.ui.Window.Notification;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.EventRequest;
import javax.portlet.EventResponse;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;

/**
 *
 * @author Aidar.Myrzahanov
 */
public abstract class BaseApplication extends Application implements PortletListener {

    private static final Logger log = Logger.getLogger(BaseApplication.class.getCanonicalName());

    @Override
    public void init() {
        setMainWindow(new Window());

        if (getContext() instanceof PortletApplicationContext2) {
            PortletApplicationContext2 ctx
                    = (PortletApplicationContext2) getContext();

            ctx.addPortletListener(this, this);
        } else {
            getMainWindow().showNotification("Not inited via Portal!", Notification.TYPE_ERROR_MESSAGE);
        }

    }

    @Override
    public void handleRenderRequest(RenderRequest request, RenderResponse response, Window window) {
        try {
            PortletEnvironment env = new PortletEnvironmentImpl(request);
            Window appWindow = createWindow(env);
            setMainWindow(appWindow);
        } catch (PortalException pe) {
            log.log(Level.SEVERE, "", pe);
        } catch (SystemException se) {
            log.log(Level.SEVERE, "", se);
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

    protected abstract Window createWindow(PortletEnvironment env);

}
