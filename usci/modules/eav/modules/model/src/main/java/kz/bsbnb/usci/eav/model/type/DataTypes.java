package kz.bsbnb.usci.eav.model.type;

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

    /**
     * Returns appropriate class for given DataTypes enum value
     *
     * INTEGER - Integer
     * DATE - java.util.Date
     * STRING - String
     * BOOLEAN - Boolean
     * DOUBLE - Double
     *
     * @param dataType
     * @return
     */
    public static Class<?> getDataTypeClass(DataTypes dataType)
    {
        switch(dataType)
        {
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

    /**
     * Same as getDataTypeClass for current instance
     *
     * @return
     */
    public Class<?> getDataTypeClass()
    {
        return getDataTypeClass(this);
    }
}
