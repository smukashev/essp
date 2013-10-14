package com.bsbnb.creditregistry.portlets.report.dm;

/**
 *
 * @author Aidar.Myrzahanov
 */
public enum ParameterType {
    NUMBER,DATE,STRING,LIST,TIME;
    public static ParameterType fromString(String value) {
        ParameterType result = STRING;
        if(value==null) {
            result = LIST;
        }
        else if("NUMBER".equalsIgnoreCase(value)) {
            result = NUMBER;
        } else if("DATE".equalsIgnoreCase(value)) {
            result = DATE;
        } else if("TIME".equalsIgnoreCase(value)) {
            result = TIME;
        }
        return result;
    }
}
