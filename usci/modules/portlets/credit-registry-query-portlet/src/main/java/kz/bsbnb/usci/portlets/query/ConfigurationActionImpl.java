package kz.bsbnb.usci.portlets.query;

import com.liferay.portal.kernel.portlet.ConfigurationAction;
import com.liferay.portal.kernel.servlet.SessionMessages;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portlet.PortletPreferencesFactoryUtil;
import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletConfig;
import javax.portlet.PortletPreferences;
import javax.portlet.ReadOnlyException;
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
        saveParameter(ar, "PoolName", prefs);
        saveParameter(ar, "OutputDateFormat", prefs);
        prefs.store();

        SessionMessages.add(ar, pc.getPortletName() + ".doConfigure");
    }

    private void saveParameter(ActionRequest ar, final String parameterName, PortletPreferences prefs) throws ReadOnlyException {
        String value = ar.getParameter(parameterName);
        if (value != null) {
            prefs.setValue(parameterName, value);
        }
    }

    @Override
    public String render(PortletConfig pc, RenderRequest rr, RenderResponse rr1) throws Exception {
        return "/configuration.jsp";
    }
}
