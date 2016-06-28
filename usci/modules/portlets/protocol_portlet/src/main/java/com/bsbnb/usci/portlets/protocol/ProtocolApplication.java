package com.bsbnb.usci.portlets.protocol;

import com.bsbnb.usci.portlets.protocol.data.BeanDataProvider;
import com.bsbnb.usci.portlets.protocol.data.DataProvider;
import com.bsbnb.usci.portlets.protocol.ui.ProtocolLayout;
import com.liferay.portal.model.Role;
import com.liferay.portal.model.User;
import com.liferay.portal.util.PortalUtil;
import com.vaadin.Application;
import com.vaadin.terminal.DownloadStream;
import com.vaadin.terminal.FileResource;
import com.vaadin.terminal.gwt.server.PortletApplicationContext2;
import com.vaadin.terminal.gwt.server.PortletApplicationContext2.PortletListener;
import com.vaadin.ui.Window;
import kz.bsbnb.usci.eav.model.json.BatchFullJModel;
import kz.bsbnb.usci.eav.util.Errors;
import org.apache.log4j.Logger;

import javax.portlet.*;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.math.BigInteger;
import java.security.AccessControlException;

public class ProtocolApplication extends Application {
    private static final long serialVersionUID = 2096197512742005243L;
    public static final String CONTEXT_NAME = "protocol_portlet";
    public final Logger logger = Logger.getLogger(ProtocolApplication.class);

    @Override
    public void init() {
        Thread.setDefaultUncaughtExceptionHandler( new Thread.UncaughtExceptionHandler(){
            public void uncaughtException(Thread t, Throwable e) {
                System.out.println("*****Yeah, Caught the Exception*****");
                logger.error(e.getMessage(),e);
            }
        });

        setTheme("custom");
        setMainWindow(new Window());
        if (getContext() instanceof PortletApplicationContext2) {
            PortletApplicationContext2 ctx = (PortletApplicationContext2) getContext();
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
                boolean isNB = false;

                User user = PortalUtil.getUser(PortalUtil.getHttpServletRequest(request));
                if (user != null) {
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

                if (!hasRights)
                    throw new AccessControlException(Errors.compose(Errors.E238));

                setTheme("custom");
                logger.info("User ID: " + user.getUserId());
                Window mainWindow = new Window();
                PortletEnvironmentFacade.set(new ProtocolPortletEnvironmentFacade(user, isNB));
                DataProvider provider = new BeanDataProvider();
                mainWindow.addComponent(new ProtocolLayout(provider));
                setMainWindow(mainWindow);

                if (user != null) {
                    for (Role role : user.getRoles()) {
                        if (role.getName().equals("Administrator")) {
                            String batchIdString = PortalUtil.getOriginalServletRequest(PortalUtil.getHttpServletRequest(request)).getParameter("batchId");
                            if (batchIdString != null) {
                                for (String batchId : batchIdString.split("\\|")) {
                                    DownloadBatch(mainWindow, provider, batchId);
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                String exceptionMessage = e.getMessage() != null ? e.getMessage() : e.toString();
                getMainWindow().showNotification(Errors.decompose(exceptionMessage), Window.Notification.TYPE_ERROR_MESSAGE);
            }
        }

        private void DownloadBatch(final Window mainWindow, DataProvider provider, String batchIdString) {
            final BigInteger batchId = new BigInteger(batchIdString);

            final BatchFullJModel batchFullJModel = provider.getBatchFullModel(batchId);

            final String fileName = "batch_" + batchId + ".zip";
            final File batchFile = new File(fileName);

            FileResource resource = new FileResource(batchFile, mainWindow.getApplication()) {
                @Override
                public DownloadStream getStream() {
                    final DownloadStream ds = new DownloadStream(
                            new ByteArrayInputStream(batchFullJModel.getContent()),
                            "application/zip",
                            fileName
                    );

                    ds.setParameter("Content-Length", String.valueOf(batchFullJModel.getContent()));
                    ds.setCacheTime(DownloadStream.DEFAULT_CACHETIME);
                    return ds;
                }
            };

            mainWindow.getWindow().open(resource, "_blank");
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