package com.zero211.moviemaestro;

public class StringUtils
{
    public static boolean isNullOrEmpty(String str)
    {
        return ((str == null) || (str.trim().length() == 0));
    }
}
