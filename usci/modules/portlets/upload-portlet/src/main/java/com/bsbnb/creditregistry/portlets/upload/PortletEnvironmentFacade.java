package com.bsbnb.creditregistry.portlets.upload;

/**
 *
 * @author Aidar.Myrzahanov
 */
public interface PortletEnvironmentFacade {

    public String getResourceString(String key);

    public long getUserID();
    
    public boolean isLanguageKazakh();
    
    public boolean isUsingDigitalSign();
    
    public void setUsingDigitalSign(boolean value);
    
}
