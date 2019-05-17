package com.zero211.moviemaestro;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.jayway.jsonpath.DocumentContext;

import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;

import static com.zero211.moviemaestro.AbstractTMDBJSONResultFromURLTask.FACEBOOK_ID_PATH;
import static com.zero211.moviemaestro.AbstractTMDBJSONResultFromURLTask.HOMEPAGE_PATH;
import static com.zero211.moviemaestro.AbstractTMDBJSONResultFromURLTask.IMDB_ID_PATH;
import static com.zero211.moviemaestro.AbstractTMDBJSONResultFromURLTask.INSTA_ID_PATH;
import static com.zero211.moviemaestro.AbstractTMDBJSONResultFromURLTask.TWITTER_ID_PATH;
import static com.zero211.moviemaestro.StringUtils.getFBURIFromID;
import static com.zero211.moviemaestro.StringUtils.getIMDBURIFromID;
import static com.zero211.moviemaestro.StringUtils.getInstaURIFromID;
import static com.zero211.moviemaestro.StringUtils.getTwitterURIFromID;
import static com.zero211.moviemaestro.StringUtils.isNullOrEmpty;

public class UIUtils
{
    public static String getCDLStringFromListWithNames(List<Map<String,Object>> items)
    {
        return getDLStringFromListWithNames(items, ", ");
    }

    public static String getNDLStringFromListWithNames(List<Map<String,Object>> items)
    {
        return getDLStringFromListWithNames(items, "\n");
    }

    public static String getDLStringFromListWithNames(List<Map<String,Object>> items, String delimiter)
    {
        StringBuffer sb = new StringBuffer();

        if ((items == null) || (items.size() == 0))
        {
            return "Unknown";
        }

        for(Map<String,Object> item : items)
        {
            String name = (String)(item.get("name"));
            if (sb.length() > 0)
            {
                sb.append(delimiter);
            }
            sb.append(name);
        }

        String result = sb.toString();
        return result;
    }


    public static void setTextIfNotNullAndNotEmpty(@NonNull TextView textView, String str)
    {
        setTextIfNotNullAndNotEmpty(textView, true, str);
    }

    public static void setTextIfNotNullAndNotEmpty(@NonNull TextView textView, boolean collapseIfNullorEmpty, String str)
    {
        setTextIfNotNullAndNotEmpty(null, collapseIfNullorEmpty, textView, str);
    }

    public static void setTextIfNotNullAndNotEmpty(TextView labelView, @NonNull TextView textView, String str)
    {
        setTextIfNotNullAndNotEmpty(labelView, true, textView, str);
    }

    public static void setTextIfNotNullAndNotEmpty(@NonNull TextView labelView, boolean collapseIfNullorEmpty, @NonNull TextView textView, String str)
    {
        int collapseIfNullorEmptyVal;

        if (collapseIfNullorEmpty)
        {
            collapseIfNullorEmptyVal = View.GONE;
        }
        else
        {
            collapseIfNullorEmptyVal = View.INVISIBLE;
        }


        if (isNullOrEmpty(str))
        {
            if (labelView != null)
            {
                labelView.setVisibility(collapseIfNullorEmptyVal);
            }
            textView.setVisibility(collapseIfNullorEmptyVal);
        }
        else
        {
            if (labelView != null)
            {
                labelView.setVisibility(View.VISIBLE);
            }
            textView.setVisibility(View.VISIBLE);
            textView.setText(str);
        }
    }

    public static void setButtonURIIfNotNullAndNotEmpty(@NonNull Button btn, String uriStr)
    {
        setButtonURIIfNotNullAndNotEmpty(btn, true, uriStr);
    }

    public static void setButtonURIIfNotNullAndNotEmpty(@NonNull Button btn, boolean collapseIfNullorEmpty, String uriStr)
    {
        int collapseIfNullorEmptyVal;

        if (collapseIfNullorEmpty)
        {
            collapseIfNullorEmptyVal = View.GONE;
        }
        else
        {
            collapseIfNullorEmptyVal = View.INVISIBLE;
        }

        if (isNullOrEmpty(uriStr))
        {
            btn.setVisibility(collapseIfNullorEmptyVal);
        }
        else
        {
            btn.setTag(uriStr);
            btn.setVisibility(View.VISIBLE);
        }
    }

    public static void setLabelVisibilityBasedOnStringValues(TextView labelView, String... strs)
    {
        setLabelVisibilityBasedOnStringValues(labelView, true, strs);
    }


    public static void setLabelVisibilityBasedOnStringValues(TextView labelView, boolean collapseIfNullorEmpty, String... strs)
    {
        int collapseIfNullorEmptyVal;

        if (collapseIfNullorEmpty)
        {
            collapseIfNullorEmptyVal = View.GONE;
        }
        else
        {
            collapseIfNullorEmptyVal = View.INVISIBLE;
        }

        if (labelView != null)
        {
            boolean allAreNullOrEmpty = true;
            for (String str : strs)
            {
                if (!isNullOrEmpty(str))
                {
                    allAreNullOrEmpty = false;
                }
            }

            if (allAreNullOrEmpty)
            {
                labelView.setVisibility(collapseIfNullorEmptyVal);
            }
            else
            {
                labelView.setVisibility(View.VISIBLE);
            }

        }

    }

    public static void setSocialButtons(DocumentContext mergedDoc, View parentView)
    {
        String homepage_uri = mergedDoc.read(HOMEPAGE_PATH);
        String fb_id = mergedDoc.read(FACEBOOK_ID_PATH);
        String fb_uri = getFBURIFromID(fb_id);
        String insta_id = mergedDoc.read(INSTA_ID_PATH);
        String insta_uri = getInstaURIFromID(insta_id);
        String twitter_id = mergedDoc.read(TWITTER_ID_PATH);
        String twitter_uri = getTwitterURIFromID(twitter_id);
        String imdb_id = mergedDoc.read(IMDB_ID_PATH);
        String imdb_uri = getIMDBURIFromID(imdb_id);

        Button btnHomePage = parentView.findViewById(R.id.btnHomePage);
        Button btnFB = parentView.findViewById(R.id.btnFB);
        Button btnInsta = parentView.findViewById(R.id.btnInsta);
        Button btnTwitter = parentView.findViewById(R.id.btnTwitter);
        Button btnIMDB = parentView.findViewById(R.id.btnIMDB);

        setButtonURIIfNotNullAndNotEmpty(btnHomePage, homepage_uri);
        setButtonURIIfNotNullAndNotEmpty(btnFB, fb_uri);
        setButtonURIIfNotNullAndNotEmpty(btnInsta, insta_uri);
        setButtonURIIfNotNullAndNotEmpty(btnTwitter, twitter_uri);
        setButtonURIIfNotNullAndNotEmpty(btnIMDB, imdb_uri);
    }
}
