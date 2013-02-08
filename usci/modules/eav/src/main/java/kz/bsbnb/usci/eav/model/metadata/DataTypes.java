package kz.bsbnb.usci.eav.model.metadata;

import java.util.Date;

/**
 * Holds codes for EAV entity attribute types
 *
 * @author a.tkachenko
 * @version 1.0, 17.01.2013
 */
public enum DataTypes
{
    INTEGER,
    DATE,
    STRING,
    BOOLEAN,
    DOUBLE;

    public static Class<?> getDataTypeClass(DataTypes dataType) {
        switch(dataType) {
            case INTEGER:
                return Integer.class;
            case DATE:
                return Date.class;
            case STRING:
                return String.class;
            case BOOLEAN:
                return Boolean.class;
            case DOUBLE:
                return Double.class;
            default:
                throw new IllegalArgumentException("Unknown type. Can not be returned an appropriate class.");
        }
    }

    public Class<?> getDataTypeClass() {
        return getDataTypeClass(this);
    }

}
