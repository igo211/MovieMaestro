package com.zero211.moviemaestro;

import android.content.Context;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class StringUtils
{
    private static final String ID_PLACEHOLDER = "<ID>";

    public static boolean isNullOrEmpty(@Nullable String str)
    {
        return ((str == null) || (str.trim().length() == 0));
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

    private static String getURLFromID(@NonNull String urlPatternStr,@NonNull String id)
    {
        if (id == null)
        {
            return null;
        }

        String url = urlPatternStr.replace(ID_PLACEHOLDER, id);
        return url;
    }

    public static String getIMDBURLFromID(@NonNull String id)
    {
        return getURLFromID("https://www.imdb.com/name/<ID>/", id);
    }

    public static String getFBURLFromID(@NonNull String id)
    {
        return getURLFromID("https://www.facebook.com/<ID>", id);
    }

    public static String getInstaURLFromID(@NonNull String id)
    {
        return getURLFromID("https://www.instagram.com/<ID>/", id);
    }

    public static String getTwitterURLFromID(@NonNull String id)
    {
        return getURLFromID("https://twitter.com/<ID>", id);
    }
}
