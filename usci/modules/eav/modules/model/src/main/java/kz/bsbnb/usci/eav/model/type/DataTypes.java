package kz.bsbnb.usci.eav.model.type;

import kz.bsbnb.usci.eav.util.Errors;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public enum DataTypes {
    INTEGER,
    DATE,
    STRING,
    BOOLEAN,
    DOUBLE;

    public static final String DATE_FORMAT_SLASH = "yyyy-MM-dd";
    public static final String DATE_FORMAT_DOT = "dd.MM.yyyy";

    public static final DateFormat dateFormatSlash = new SimpleDateFormat(DATE_FORMAT_SLASH);
    public static final DateFormat dateFormatDot = new SimpleDateFormat(DATE_FORMAT_DOT);

    public static Class<?> getDataTypeClass(DataTypes dataType) {
        switch (dataType) {
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
                throw new IllegalArgumentException(Errors.compose(Errors.E49));
        }
    }

    public static Object getCastObject(DataTypes typeCode, String value) {
        switch(typeCode) {
            case INTEGER:
                return Integer.parseInt(value);
            case DATE:
                Date date = null;

                try {
                    date = dateFormatSlash.parse(value);
                } catch (ParseException e) {
                    try {
                        date = dateFormatDot.parse(value);
                    } catch (ParseException ex) {
                        e.printStackTrace();
                    }
                }

                return date;
            case STRING:
                return value;
            case BOOLEAN:
                try {
                    int i = Integer.parseInt(value);
                    return i == 1;
                } catch (Exception e) {
                    return Boolean.parseBoolean(value);
                }
            case DOUBLE:
                return Double.parseDouble(value);
            default:
                throw new IllegalArgumentException(Errors.compose(Errors.E127));
        }
    }

    public Class<?> getDataTypeClass() {
        return getDataTypeClass(this);
    }
}
