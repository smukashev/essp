package com.bsbnb.usci.portlets.crosscheck.dm;

import kz.bsbnb.usci.eav.util.Errors;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;
import javax.xml.bind.DatatypeConverter;

/**
 *
 * @author Aidar.Myrzahanov
 */
public class DataTypeUtil {
    
    public static final String SHORT_DATE_FORMAT = "dd.MM.yyyy";
    public static final String LONG_DATE_FORMAT = "dd MMMM yyyy HH:mm:ss";
    
    public static final String DECIMAL_SEPARATOR = ".";
    public static final String THOUSAND_SEPARATOR = ",";
    public static final String NUMBER_FORMAT = "#,###.##";

    public static final long MILLISECONDS_PER_DAY = 24L * 60 * 60 * 1000;

    public static Date plus(final Date date, int field, int amount) {
        final Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(date.getTime());
        calendar.add(field, amount);
        return calendar.getTime();
    }
    
    public static void toBeginningOfTheDay(final Date date) {
        final long oldTime = date.getTime();
        final long timeZoneOffset = TimeZone.getDefault().getOffset(oldTime);

        date.setTime(((oldTime + timeZoneOffset) / MILLISECONDS_PER_DAY) * MILLISECONDS_PER_DAY - timeZoneOffset);
    }
}

