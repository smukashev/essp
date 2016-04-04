package kz.bsbnb.usci.portlet.report;

import com.liferay.portal.kernel.portlet.ConfigurationAction;
import com.liferay.portal.kernel.servlet.SessionMessages;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portlet.PortletPreferencesFactoryUtil;
import org.apache.log4j.Logger;

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

    private static final Logger logger = Logger.getLogger(ConfigurationActionImpl.class);

    @Override
    public void processAction(PortletConfig pc, ActionRequest ar, ActionResponse ar1) throws Exception {
        String portletResource = ParamUtil.getString(ar, "portletResource");

        PortletPreferences prefs = PortletPreferencesFactoryUtil.getPortletSetup(ar, portletResource);
        String typeValue = ar.getParameter("type");
        logger.info(typeValue);
        if (typeValue != null) {
            logger.info("Setting: "+ typeValue);
            prefs.setValue("type", typeValue);
        }

        String defaultReportDateString = ar.getParameter("defaultreportdate");
        logger.info(defaultReportDateString);
        if (defaultReportDateString != null) {
            logger.info("Default report date string: "+ defaultReportDateString);
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
