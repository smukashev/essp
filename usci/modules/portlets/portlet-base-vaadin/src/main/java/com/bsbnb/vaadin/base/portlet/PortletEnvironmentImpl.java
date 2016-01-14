package com.bsbnb.vaadin.base.portlet;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.model.Role;
import com.liferay.portal.model.User;
import com.liferay.portal.util.PortalUtil;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.TreeSet;
import javax.portlet.RenderRequest;

/**
 *
 * @author Aidar.Myrzahanov
 */
public class PortletEnvironmentImpl implements PortletEnvironment {

    private static final String BUNDLE_NAME = "content.Language";

    private final RenderRequest request;
    private final Set<String> roles;
    private final User user;
    private final ResourceBundle bundle;

    public PortletEnvironmentImpl(RenderRequest request) throws PortalException, SystemException {
        this.request = request;
        user = PortalUtil.getUser(request);
        roles = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
        for (Role role : user.getRoles()) {
            roles.add(role.getName());
        }
        bundle = ResourceBundle.getBundle(BUNDLE_NAME, user.getLocale());
    }

    @Override
    public boolean checkIfUserHasRole(String rolename) {
        return roles.contains(rolename);
    }

    @Override
    public boolean isUserAdmin() {
        return checkIfUserHasRole("Administrator");
    }

    @Override
    public RenderRequest getRequest() {
        return request;
    }

    @Override
    public long getUserId() {
        return user.getUserId();
    }

    @Override
    public String getString(String key) {
        return bundle.getString(key);
    }

    @Override
    public String getString(Localizable localizable) {
        return bundle.getString(localizable.getKey());
    }

    @Override
    public boolean isBankUser() {
        return checkIfUserHasRole("BankUser");
    }

}
