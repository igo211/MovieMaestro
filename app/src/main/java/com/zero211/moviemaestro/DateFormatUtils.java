package com.zero211.moviemaestro;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import androidx.annotation.NonNull;

public class DateFormatUtils
{
    public static final DateFormat BUILD_DATE_FORMAT = new SimpleDateFormat("yyyyMMddHHmmss");
    public static final DateFormat TMDB_API_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    private static final DateFormat SHORT_DATE_FORMAT = new SimpleDateFormat("MMM d, yyyy");
    private static final DateFormat SHORT_THIS_YEAR_DATE_FORMAT = new SimpleDateFormat("MMM d");
    private static final DateFormat LONG_DATE_FORMAT = new SimpleDateFormat("MMMM d, yyyy");
    private static final DateFormat JUST_YEAR_FORMAT = new SimpleDateFormat("yyyy");
    private static final DateFormat EXIF_DATETIME_ORIGINAL_FORMAT = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss");

    private static String getFormattedDateStrFromTMDBDateStr(DateFormat formater, String tmdb_date_str)
    {
        try
        {
            Date date = getDateFromTMDBDateStr(tmdb_date_str);
            if (date == null)
            {
                return tmdb_date_str;
            }

            String formattedStr = formater.format(date);
            return formattedStr;
        }
        catch (Throwable e)
        {
            e.printStackTrace();
            return tmdb_date_str;
        }

    }

    private static String getFormattedDateStrFromDate(DateFormat formater, Date date)
    {
        try
        {
            if (date == null)
            {
                return null;
            }

            String formattedStr = formater.format(date);
            return formattedStr;
        }
        catch (Throwable e)
        {
            e.printStackTrace();
            return null;
        }

    }

    public static Date getDateFromTMDBDateStr(String tmdb_date_str)
    {
        try
        {
            Date date = TMDB_API_DATE_FORMAT.parse(tmdb_date_str);
            return date;
        }
        catch (Throwable e)
        {
            e.printStackTrace();
            return null;
        }
    }

    public static String getShortDateStrFromDate(Date date)
    {
        return getFormattedDateStrFromDate(SHORT_DATE_FORMAT, date);
    }

    public static String getShortDateStrFromTMDBDateStr(String tmdb_date_str)
    {
        return getFormattedDateStrFromTMDBDateStr(SHORT_DATE_FORMAT, tmdb_date_str);
    }

    public static String getShortThisYearDateStrFromDate(Date date)
    {
        return getFormattedDateStrFromDate(SHORT_THIS_YEAR_DATE_FORMAT, date);
    }

    public static String getShortThisYearDateStrFromTMDBDateStr(String tmdb_date_str)
    {
        return getFormattedDateStrFromTMDBDateStr(SHORT_THIS_YEAR_DATE_FORMAT, tmdb_date_str);
    }

    public static String getLongDateStrFromDate(Date date)
    {
        return getFormattedDateStrFromDate(LONG_DATE_FORMAT, date);
    }

    public static String getLongDateStrFromTMDBDateStr(String tmdb_date_str)
    {
        return getFormattedDateStrFromTMDBDateStr(LONG_DATE_FORMAT, tmdb_date_str);
    }

    public static String getJustYearDateStrFromTMDBDate(Date date)
    {
        return getFormattedDateStrFromDate(JUST_YEAR_FORMAT, date);
    }

    public static String getJustYearDateStrFromTMDBDateStr(String tmdb_date_str)
    {
        return getFormattedDateStrFromTMDBDateStr(JUST_YEAR_FORMAT, tmdb_date_str);
    }

    public static Date getDateFromExifDateTimeOriginal(String exifDateTimeOrigStr)
    {
        try
        {
            Date date = EXIF_DATETIME_ORIGINAL_FORMAT.parse(exifDateTimeOrigStr);
            return date;
        }
        catch (Throwable e)
        {
            e.printStackTrace();
            return null;
        }
    }


    // Returns age given the date of birth
    public static int getAge(@NonNull Date dobDate)
    {
        return getAge(dobDate, null);
    }

    // Returns age given the date of birth
    public static int getAge(@NonNull Date dobDate, Date doeDate)
    {
        Calendar dob = Calendar.getInstance();
        dob.setTime(dobDate);

        Calendar doe = Calendar.getInstance();

        if (doeDate != null)
        {
            doe.setTime(doeDate);
        }

        int doeYear = doe.get(Calendar.YEAR);
        int dobYear = dob.get(Calendar.YEAR);

        int age = doeYear - dobYear;

        // if dob is month or day is behind today's month or day
        // reduce age by 1
        int doeMonth = doe.get(Calendar.MONTH);
        int dobMonth = dob.get(Calendar.MONTH);

        if (dobMonth > doeMonth)
        { // this year can't be counted!
            age--;
        }
        else if (dobMonth == doeMonth)
        { // same month? check for day
            int curDay = doe.get(Calendar.DAY_OF_MONTH);
            int dobDay = dob.get(Calendar.DAY_OF_MONTH);
            if (dobDay > curDay)
            { // this year can't be counted!
                age--;
            }
        }

        return age;
    }
}
