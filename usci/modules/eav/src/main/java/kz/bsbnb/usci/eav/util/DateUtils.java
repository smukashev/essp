package kz.bsbnb.usci.eav.util;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * @author a.motov
 */
public class DateUtils {

    public static final long MILLISECONDS_PER_DAY = 24L * 60 * 60 * 1000;

    public static Date plus(final Date date, int field, int amount) {
        final Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(date.getTime());
        calendar.add(field, amount);
        return calendar.getTime();
    }

    public static Date nowPlus(int field, int amount) {
        final Calendar calendar = Calendar.getInstance();
        calendar.add(field, amount);
        return calendar.getTime();
    }

    public static int compareBeginningOfTheDay(Date comparingDate, Date anotherDate) {
        final Date newComparingDate = new Date(comparingDate.getTime());
        final Date newAnotherDate = new Date(anotherDate.getTime());
        toBeginningOfTheDay(newComparingDate);
        toBeginningOfTheDay(newAnotherDate);
        return newComparingDate.compareTo(newAnotherDate);
    }

    public static void toBeginningOfTheDay(final Date date) {
        final long oldTime = date.getTime();
        final long timeZoneOffset = TimeZone.getDefault().getOffset(oldTime);

        date.setTime(((oldTime + timeZoneOffset) / MILLISECONDS_PER_DAY) * MILLISECONDS_PER_DAY - timeZoneOffset);
    }


}
