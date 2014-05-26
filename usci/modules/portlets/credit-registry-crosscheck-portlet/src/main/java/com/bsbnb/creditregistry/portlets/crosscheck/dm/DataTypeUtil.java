package com.bsbnb.creditregistry.portlets.crosscheck.dm;

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
    
    public static String convertDateToString(String format, Date date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(format);
        if (date == null)
            return "null";
        
        return dateFormat.format(date);
    }
    
    public static List<Date> getAvailableReportDates(Date currentReportDate, int beforeCount, int afterCount, int periodMonthCount) {
        List<Date> dates = new ArrayList<Date>();
        for (int i = beforeCount; i >= 1; i--)
            dates.add(convertDateToFirstDay(currentReportDate, -1 * periodMonthCount * i));
        dates.add(currentReportDate);
        for (int i = 1; i <= afterCount; i++)
            dates.add(convertDateToFirstDay(currentReportDate, periodMonthCount * i));
        
        return dates;
    }
    
    public static Date getCurrentReportDate(Date currentDate, int periodMonthCount) {
        Calendar currentCalendar = Calendar.getInstance();
        currentCalendar.setTimeInMillis(currentDate.getTime());
        int currentMonth = currentCalendar.get(Calendar.MONTH);
        int currentMonthNum = 0;
        
        for (int i = (12 / periodMonthCount) - 1; i >= 0 ; i--) {
            int monthNum = periodMonthCount * i;
            if (currentMonth >= monthNum) {
                currentMonthNum = monthNum;
                break;
            }
        }
        System.out.println("CURRENT_MONTH_NUM: " + currentMonthNum);
        
        return convertDateToFirstDay(currentDate, -1 * (currentMonth - currentMonthNum));
    }
    
    public static List<Date> getAvailableReportDates(Date fromDate, Date toDate) {
        fromDate = convertDateToFirstDay(fromDate, 0);
        toDate = convertDateToFirstDay(toDate, 0);
        if(fromDate.after(toDate)) {
            throw new IllegalArgumentException("First date should be less than the second");
        }
        List<Date> dates = new ArrayList<Date>();
        while(fromDate.compareTo(toDate)<=0) {
            dates.add(fromDate);
            fromDate = convertDateToFirstDay(fromDate, 1);
        }
        return dates;
    }
    
    /**
     * Создание даты с помощью класса Calendar.
     * При указании параметров стоит помнить, что месяцы в языке Java начинаются с нуля (то есть, январь равен 0).
     * @param year номер года
     * @param month номер месяца в году
     * @param date номер дня в месяце
     * @return созданная дата
     */
    public static Date createCalendarDate(int year, int month, int date) {
        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.setLenient(false);
        
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DAY_OF_MONTH, date);
        
        return calendar.getTime();
        
    }
    
    public static Date parseDate(String s) {
    	return DatatypeConverter.parseDate(s).getTime();
    }
    	  
    public static String printDate(Date dt) {
    	Calendar cal = new GregorianCalendar();
    	cal.setTime(dt);
    	return DatatypeConverter.printDate(cal);
    }
    
    /**
     * Форматирует дату в формате по-умолчанию.
     * @param date Дата для форматирования.
     * @return Отформатированная дата.
     */
    public static String formatDate(Date date) {
        if (date == null)
            return null;
        return DateFormat.getDateInstance().format(date);
    }
    
    public static String formatDate(Date date, String dateFormat) {
        if (date == null || dateFormat == null)
            return null;
        
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dateFormat);
        return simpleDateFormat.format(date);
    }
    
    /**
     * Конвертирует дату к первому числу следующего месяца.
     * @param date Исходная дата.
     * @return Отконвертированная дата.
     */
    public static Date convertDateToFirstDay(Date date, int month) {
        if (date == null)
            return null;
        
        Calendar currentCalendar = Calendar.getInstance();
        currentCalendar.setTimeInMillis(date.getTime());
        
        currentCalendar.set(Calendar.MONTH, currentCalendar.get(Calendar.MONTH) + month);
        currentCalendar.set(Calendar.YEAR, currentCalendar.get(Calendar.YEAR));
        currentCalendar.set(Calendar.DATE, 1);
        
        return currentCalendar.getTime();
    }
    
    /**
     * TODO javadoc 
     */
    public static Date convertCalendarToDate(Calendar calendar){
    	
    	return new Date(calendar.getTimeInMillis());
    }
    
    /**
     * TODO javadoc
     */
    public static Calendar convertDateToCalendar(Date date){
    	
    	Calendar calendar = Calendar.getInstance();
    	calendar.setTime(date);
    	
    	return calendar;
    }
    
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

