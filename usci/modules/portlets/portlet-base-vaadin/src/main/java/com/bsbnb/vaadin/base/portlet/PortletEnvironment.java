package com.bsbnb.vaadin.base.portlet;

import javax.portlet.RenderRequest;

/**
 *
 * @author Aidar.Myrzahanov
 */
public interface PortletEnvironment {

    public boolean isUserAdmin();

    public boolean isBankUser();

    public boolean checkIfUserHasRole(String rolename);

    public RenderRequest getRequest();

    public long getUserId();

    public String getString(String key);

    public String getString(Localizable key);

}
