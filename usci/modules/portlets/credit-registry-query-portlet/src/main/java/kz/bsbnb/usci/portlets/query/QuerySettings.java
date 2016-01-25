package kz.bsbnb.usci.portlets.query;

import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.model.Layout;
import com.liferay.portal.theme.ThemeDisplay;
import com.liferay.portlet.PortletPreferencesFactoryUtil;
import javax.portlet.PortletPreferences;
import javax.portlet.PortletRequest;

/**
 *
 * @author Aidar.Myrzahanov
 */
public class QuerySettings {

    private static final String DEFAUL_POOL = "jdbc/RepPool";
    private static final String DEFAULT_DATE_FORMAT = "dd.MM.yyyy HH:mm:ss";

    private final Layout layout;
    private final String portletId;
    private final PortletPreferences preferences;

    public QuerySettings(PortletRequest request) {
        ThemeDisplay themeDisplay = (ThemeDisplay) request.getAttribute(WebKeys.THEME_DISPLAY);
        layout = themeDisplay.getLayout();
        portletId = themeDisplay.getPortletDisplay().getId();
        preferences = null;
    }

    public QuerySettings(PortletPreferences preferences) {
        this.preferences = preferences;
        layout = null;
        portletId = null;
    }

    /**
     * @return the poolName
     */
    public String getPoolName() {
        return getPreferences().getValue("PoolName", DEFAUL_POOL);
    }

    /**
     * @return the outputDateFormat
     */
    public String getOutputDateFormat() {
        return getPreferences().getValue("OutputDateFormat", DEFAULT_DATE_FORMAT);
    }

    private PortletPreferences getPreferences() {
        if (preferences != null) {
            return preferences;
        }
        try {
            return PortletPreferencesFactoryUtil.getPortletSetup(layout, portletId, "");
        } catch (SystemException ex) {
            throw new RuntimeException(ex);
        }
    }
}
