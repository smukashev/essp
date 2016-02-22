package kz.bsbnb.usci.eav.model.type;

import kz.bsbnb.usci.eav.Errors;

import java.text.ParseException;
import java.text.SimpleDateFormat;
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

    private static SimpleDateFormat dateFormat1 = new SimpleDateFormat("dd.MM.yyyy");
    private static SimpleDateFormat dateFormat2 = new SimpleDateFormat("yyyy-MM-dd");

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
                throw new IllegalArgumentException(String.valueOf(Errors.E49));
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

    public static Object fromString(DataTypes type, String value){

        switch (type)
        {
            case INTEGER:
                return Integer.parseInt(value);
            case STRING:
                return value;
            case DOUBLE:
                return Double.parseDouble(value);
            case BOOLEAN:
                return Boolean.parseBoolean(value);
            case DATE:
                Date date = null;

                try {
                    date = dateFormat1.parse(value);
                } catch (ParseException e) {
                    try {
                        date = dateFormat2.parse(value);
                    } catch (ParseException ex) {
                        e.printStackTrace();
                    }
                }

                return date;
            default:
                throw new IllegalArgumentException(String.valueOf(Errors.E49));
        }
    }
}
