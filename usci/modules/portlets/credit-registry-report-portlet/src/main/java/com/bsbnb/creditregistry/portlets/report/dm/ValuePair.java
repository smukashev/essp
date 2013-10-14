package com.bsbnb.creditregistry.portlets.report.dm;

/**
 *
 * @author Aidar.Myrzahanov
 */
public class ValuePair {
    private String value;
    private String displayName;
    
    public ValuePair(String value, String displayName) {
        this.value = value;
        this.displayName = displayName;
    }

    /**
     * @return the value
     */
    public String getValue() {
        return value;
    }

    /**
     * @param value the value to set
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * @return the displayName
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * @param displayName the displayName to set
     */
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
    
    @Override
    public String toString() {
        return displayName;
    }
}
