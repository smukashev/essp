package kz.bsbnb.usci.eav.model.type;

import kz.bsbnb.usci.eav.util.Errors;

import javax.xml.crypto.Data;
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

    private static final DateFormat dateFormatSlash = new SimpleDateFormat(DATE_FORMAT_SLASH);
    private static final DateFormat dateFormatDot = new SimpleDateFormat(DATE_FORMAT_DOT);

    public synchronized static Date parseDate(String s) throws ParseException {
        try {
            return dateFormatDot.parse(s);
        } catch (ParseException e) {
            return dateFormatSlash.parse(s);
        }
    }

    public synchronized static Date parseSplashDate(String s) throws ParseException {
        return dateFormatSlash.parse(s);
    }

    public synchronized static String formatDate(Date d) {
        return dateFormatDot.format(d);
    }

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
                    synchronized (DataTypes.class) {
                        date = dateFormatSlash.parse(value);
                    }
                } catch (ParseException e) {
                    try {
                        synchronized (DataTypes.class) {
                            date = dateFormatDot.parse(value);
                        }
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
