package com.zero211.moviemaestro;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateFormatUtils
{
    public static final DateFormat TMDB_API_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    private static final DateFormat SHORT_DATE_FORMAT = new SimpleDateFormat("MMM d yyyy");
    private static final DateFormat SHORT_THIS_YEAR_DATE_FORMAT = new SimpleDateFormat("MMM d");

    private static String getFormattedDateFromTMDBDateStr(DateFormat formater, String tmdb_date_str)
    {
        try
        {
            Date date = TMDB_API_DATE_FORMAT.parse(tmdb_date_str);
            String formattedStr = formater.format(date);
            return formattedStr;
        }
        catch (Throwable e)
        {
            e.printStackTrace();
            return tmdb_date_str;
        }

    }

    public static String getShortDateFromTMDBDateStr(String tmdb_date_str)
    {
        return getFormattedDateFromTMDBDateStr(SHORT_DATE_FORMAT, tmdb_date_str);
    }

    public static String getShortThisYearDateFromTMDBDateStr(String tmdb_date_str)
    {
        return getFormattedDateFromTMDBDateStr(SHORT_THIS_YEAR_DATE_FORMAT, tmdb_date_str);
    }
}
