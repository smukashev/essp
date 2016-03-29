package kz.bsbnb.usci.portlet.report.dm;

/**
 *
 * @author Aidar.Myrzahanov
 */
public enum ParameterType {
    NUMBER,DATE,STRING,LIST,TIME, OPTION;
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
        } else if("OPTION".equalsIgnoreCase(value)) {
            result=OPTION;
        }
        return result;
    }
}
