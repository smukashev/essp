package kz.bsbnb.usci.eav.util;

import kz.bsbnb.usci.eav.model.type.DataTypes;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class DataUtils {
    private static final long MILLISECONDS_PER_DAY = 24L * 60 * 60 * 1000;

    public static int compareBeginningOfTheDay(final Date comparingDate, final Date anotherDate) {
        final Date newComparingDate = new Date(comparingDate.getTime());
        final Date newAnotherDate = new Date(anotherDate.getTime());
        toBeginningOfTheDay(newComparingDate);
        toBeginningOfTheDay(newAnotherDate);
        return newComparingDate.compareTo(newAnotherDate);
    }

    public static void toBeginningOfTheMonth (Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        date.setTime(cal.getTime().getTime());
    }

    public static void moveMonthIfNecessary(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        int day = cal.get(Calendar.DAY_OF_MONTH);
        if(day != 1) {
            cal.add(Calendar.MONTH, 1);
            date.setTime(cal.getTime().getTime());
        }
    }

    public static void takeMonth(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
            cal.add(Calendar.MONTH, -1);
            date.setTime(cal.getTime().getTime());
    }
    public static void toBeginningOfTheDay(final Date date) {
        final long oldTime = date.getTime();
        final long timeZoneOffset = TimeZone.getDefault().getOffset(oldTime);

        date.setTime(((oldTime + timeZoneOffset) / MILLISECONDS_PER_DAY) * MILLISECONDS_PER_DAY - timeZoneOffset);
    }

    public static void toBeginningOfTheSecond(final Date date) {
        final long oldTime = date.getTime();
        date.setTime(oldTime - oldTime % 1000);
    }

    public static java.util.Date convert(java.sql.Date date) {
        return date == null ? null : new java.util.Date(date.getTime());
    }

    public static java.sql.Date convert(java.util.Date date) {
        return date == null ? null : new java.sql.Date(date.getTime());
    }

    public static java.util.Date convert(Timestamp timestamp) {
        return timestamp == null ? null : new java.util.Date(timestamp.getTime());
    }

    public static java.sql.Date convertToSQLDate(Timestamp timestamp) {
        return timestamp == null ? null : new java.sql.Date(timestamp.getTime());
    }

    public static Timestamp convertToTimestamp(java.util.Date date) {
        return date == null ? null : new Timestamp(date.getTime());
    }

    public static Byte convert(boolean b) {
        return b ? Byte.valueOf("1") : 0;
    }

    public static boolean convert(Byte value) {
        return value.equals(Byte.valueOf("1"));
    }
}
