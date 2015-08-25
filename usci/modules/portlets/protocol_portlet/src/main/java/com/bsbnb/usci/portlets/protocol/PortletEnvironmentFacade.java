package com.bsbnb.usci.portlets.protocol;

/**
 *
 * @author Aidar.Myrzahanov
 */
public abstract class PortletEnvironmentFacade {
    
    private static PortletEnvironmentFacade instance;

    public static void set(PortletEnvironmentFacade provider) {
        instance = provider;
    }
    
    public static PortletEnvironmentFacade get() {
        return instance;
    }

    public abstract String getResourceString(String key);

    public abstract long getUserID();
    
    public abstract boolean isLanguageKazakh();

    public abstract boolean isNB();
    
}
