package com.bsbnb.usci.portlets.crosscheck;

import java.util.Date;

/**
 *
 * @author Aidar.Myrzahanov
 */
public abstract class PortletEnvironmentFacade {

    private static PortletEnvironmentFacade instance;
    
    public static void set(PortletEnvironmentFacade facade) {
        instance = facade;
    }
    
    public static PortletEnvironmentFacade get() {
        return instance;
    }
     

    public abstract String getCurrentLanguage();

    public abstract String getResourceString(String key);
   
    public abstract long getUserID();
    
    public abstract String getBusinessRulesUrl();

    public abstract Date getRepDate();

    public abstract String getCreditorId();

}
