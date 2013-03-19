package kz.bsbnb.usci.eav.util;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * @author a.motov
 */
public class DateUtils
{
    public static final long MILLISECONDS_PER_DAY = 24L * 60 * 60 * 1000;

    public static Date plus(final Date date, int field, int amount)
    {
        final Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(date.getTime());
        calendar.add(field, amount);
        return calendar.getTime();
    }

    public static Date nowPlus(int field, int amount)
    {
        final Calendar calendar = Calendar.getInstance();
        calendar.add(field, amount);
        return calendar.getTime();
    }

    public static int compareBeginningOfTheDay(Date comparingDate, Date anotherDate)
    {
        final Date newComparingDate = new Date(comparingDate.getTime());
        final Date newAnotherDate = new Date(anotherDate.getTime());
        toBeginningOfTheDay(newComparingDate);
        toBeginningOfTheDay(newAnotherDate);
        return newComparingDate.compareTo(newAnotherDate);
    }

    public static void toBeginningOfTheDay(final Date date)
    {
        final long oldTime = date.getTime();
        final long timeZoneOffset = TimeZone.getDefault().getOffset(oldTime);

        date.setTime(((oldTime + timeZoneOffset) / MILLISECONDS_PER_DAY) * MILLISECONDS_PER_DAY - timeZoneOffset);
    }

    public static long cutOffTime(final java.sql.Date date)
    {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }

    public static java.util.Date convert(java.sql.Date date) {
        return date == null ? null : new java.util.Date(date.getTime());
    }

    public static java.sql.Date convert(java.util.Date date) {
        return date == null ? null : new java.sql.Date(date.getTime());
    }

}
