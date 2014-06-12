package com.bsbnb.creditregistry.portlets.approval;

import com.bsbnb.creditregistry.portlets.approval.ui.Localization;
import com.liferay.portal.model.User;
import java.util.List;

/**
 *
 * @author Aidar.Myrzahanov
 */
public interface PortletEnvironmentFacade {

    public String getResourceString(Localization localization);

    public long getUserID();

    public boolean isLanguageKazakh();

    public boolean isNbUser();

    public boolean isBankUser();

    public List<User> getPortalUsers();

    public String getUsername();

    public boolean isApprovalAuthority();

    public boolean isAdministrator();
}
