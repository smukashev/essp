package com.bsbnb.creditregistry.portlets.report;

import com.liferay.portal.kernel.portlet.ConfigurationAction;
import com.liferay.portal.kernel.servlet.SessionMessages;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portlet.PortletPreferencesFactoryUtil;
import java.util.logging.Level;
import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletConfig;
import javax.portlet.PortletPreferences;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

/**
 *
 * @author Aidar.Myrzahanov
 */
public class ConfigurationActionImpl implements ConfigurationAction {

    @Override
    public void processAction(PortletConfig pc, ActionRequest ar, ActionResponse ar1) throws Exception {
        String portletResource = ParamUtil.getString(ar, "portletResource");

        PortletPreferences prefs = PortletPreferencesFactoryUtil.getPortletSetup(ar, portletResource);
        String typeValue = ar.getParameter("type");
        ReportApplication.log.log(Level.INFO, typeValue);
        if (typeValue != null) {
            ReportApplication.log.log(Level.INFO, "Setting: {0}", typeValue);
            prefs.setValue("type", typeValue);
        }

        String defaultReportDateString = ar.getParameter("defaultreportdate");
        ReportApplication.log.log(Level.INFO, defaultReportDateString);
        if (defaultReportDateString != null) {
            ReportApplication.log.log(Level.INFO, "Default report date string: {0}", defaultReportDateString);
            prefs.setValue("defaultreportdate", defaultReportDateString);
        }
        prefs.store();

        SessionMessages.add(ar, pc.getPortletName() + ".doConfigure");
    }

    @Override
    public String render(PortletConfig pc, RenderRequest rr, RenderResponse rr1) throws Exception {
        return "/configuration.jsp";
    }
}
