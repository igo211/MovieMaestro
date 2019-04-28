package com.zero211.moviemaestro;

import android.content.Context;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class StringUtils
{
    private static final String ID_PLACEHOLDER = "<ID>";

    public static boolean isNullOrEmpty(@Nullable String str)
    {
        return ((str == null) || (str.trim().length() == 0));
    }

    public static boolean isPureAscii(String v)
    {
        return StandardCharsets.US_ASCII.newEncoder().canEncode(v);
    }

    public static String rawTextFileToString(@NonNull Context ctx, @NonNull int resId)
    {
        InputStream inputStream = ctx.getResources().openRawResource(resId);

        InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        String line;
        StringBuffer sb = new StringBuffer();

        try
        {
            while ((line = bufferedReader.readLine()) != null)
            {
                if (sb.length() > 0)
                {
                    sb.append('\n');
                }
                sb.append(line);
            }
        }
        catch (IOException e)
        {
            return null;
        }
        return sb.toString();
    }

    private static String getURIFromID(@NonNull String uriPatternStr, @NonNull String id)
    {
        if (isNullOrEmpty(id))
        {
            return null;
        }

        String uri = uriPatternStr.replace(ID_PLACEHOLDER, id);
        return uri;
    }

    public static String getIMDBURIFromID(@NonNull String id)
    {
        return getURIFromID("https://www.imdb.com/name/<ID>/", id);
    }

    public static String getFBURIFromID(@NonNull String id)
    {
        return getURIFromID("https://www.facebook.com/<ID>", id);
    }

    public static String getInstaURIFromID(@NonNull String id)
    {
        return getURIFromID("https://www.instagram.com/<ID>/", id);
    }

    public static String getTwitterURIFromID(@NonNull String id)
    {
        return getURIFromID("https://twitter.com/<ID>", id);
    }
}
