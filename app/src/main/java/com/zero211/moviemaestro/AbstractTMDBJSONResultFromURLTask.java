package com.zero211.moviemaestro;

import android.content.Context;
import android.content.res.Resources;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.os.ConfigurationCompat;

import com.jayway.jsonpath.DocumentContext;
import com.zero211.utils.http.AbstractJSONResultFromURLAsyncTask;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.zero211.moviemaestro.StringUtils.isNullOrEmpty;
import static com.zero211.utils.http.HttpUtils.INTERNAL_ERROR_PATH;

public abstract class AbstractTMDBJSONResultFromURLTask extends AbstractJSONResultFromURLAsyncTask
{
    private static final String LOGTAG = AbstractTMDBJSONResultFromURLTask.class.getSimpleName();

    protected static final Locale DEFAULT_LOCALE = ConfigurationCompat.getLocales(Resources.getSystem().getConfiguration()).get(0);
    protected static final String LOCALE_STR = DEFAULT_LOCALE.toString();
    protected static final String REGION_STR = DEFAULT_LOCALE.getCountry();

    protected static final String API_PREFIX = "https://api.themoviedb.org/3/";
    protected static final String API_KEY_PLACEHOLDER = "<API_KEY>";
    protected static final String START_DATE_PLACEHOLDER = "<START_DATE>";
    protected static final String END_DATE_PLACEHOLDER = "<END_DATE>";
    protected static final String PAGE_PLACEHOLDER = "<PAGE>";
    protected static final String MOVIE_ID_PLACEHOLDER = "<MOVIE_ID>";
    protected static final String PERSON_ID_PLACEHOLDER = "<PERSON_ID>";

    protected static final String ERRORS_PATH = "$.errors";
    protected static final String STATUS_CODE_PATH = "$.status_code";
    protected static final String STATUS_MESSAGE_PATH = "$.status_message";
    protected static final String PAGE_PATH = "$.page";
    protected static final String TOTAL_RESULTS_PATH = "$.total_results";
    protected static final String TOTAL_PAGES_PATH = "$.total_pages";
    protected static final String RESULTS_PATH = "$.results";

    private String urlWithAPIKeyTemplateStr;
    private int startPage = 1;
    private int endPage = Integer.MAX_VALUE;

    public AbstractTMDBJSONResultFromURLTask(@NonNull Context context, @NonNull String urlTemplateStr)
    {
        this(context, 1, 1, urlTemplateStr);
    }

    public AbstractTMDBJSONResultFromURLTask(@NonNull Context context, int startPage, int endPage, @NonNull String urlTemplateStr)
    {
        String api_key = StringUtils.rawTextFileToString(context, R.raw.tmdb_api_key);
        String trimmed_api_key = api_key.trim();
        this.urlWithAPIKeyTemplateStr = API_PREFIX + urlTemplateStr.replace(API_KEY_PLACEHOLDER, trimmed_api_key);
        this.startPage = startPage;
        this.endPage = endPage;
    }

    @Override
    protected DocumentContext doInBackground(String... params)
    {
        int currentPage = startPage - 1;

        Integer TMDB_total_results = 0;
        Integer TMDB_total_pages = 0;

        String internal_err_msg = null;
        List<String> TMDB_err_msgs = null;
        Integer TMDB_status_code = 0;
        String TMDB_status_msg = null;

        DocumentContext mergedDoc = null;

        do
        {
            currentPage++;

            String pageURLStr = null;

            pageURLStr = urlWithAPIKeyTemplateStr.replace(PAGE_PLACEHOLDER, String.valueOf(currentPage));

            DocumentContext pageDoc = super.doInBackground(pageURLStr);

            internal_err_msg = pageDoc.read(INTERNAL_ERROR_PATH);
            TMDB_err_msgs = pageDoc.read(ERRORS_PATH);
            TMDB_status_code = pageDoc.read(STATUS_CODE_PATH);
            TMDB_status_msg = pageDoc.read(STATUS_MESSAGE_PATH);
            TMDB_total_results = pageDoc.read(TOTAL_RESULTS_PATH);
            TMDB_total_pages = pageDoc.read(TOTAL_PAGES_PATH);
            List<Map<String,Object>> pageResults = pageDoc.read(RESULTS_PATH);

            if ((internal_err_msg != null) || (TMDB_err_msgs != null) || (TMDB_status_msg != null))
            {
                // TODO: return partial results merged with the err/errmsgs/statuscode/statusmsg instead?
                return pageDoc;
            }
            else
            {
                if (currentPage == 1)
                {
                    mergedDoc = pageDoc;
                    mergedDoc.delete(PAGE_PATH);
                }
                else
                {
                    for (Map<String, Object> pageResultsItem : pageResults)
                    {
                        mergedDoc.add(RESULTS_PATH, pageResultsItem);
                    }
                    mergedDoc.set(TOTAL_PAGES_PATH, TMDB_total_pages);
                    mergedDoc.set(TOTAL_RESULTS_PATH, TMDB_total_results);
                }
            }

            // We need to potentially re-adjust the endPage after every API query response,
            // since query responses are not cursors/cached on the server and therefore the total_pages value could change.
            // Integer.MAX_VALUE represents a special value that means "whatever the total pages reported for the query is".
            if ((endPage  == Integer.MAX_VALUE) || ((TMDB_total_pages != null) && (endPage > TMDB_total_pages)))
            {
                endPage = TMDB_total_pages;
            }

        } while (currentPage < endPage);

        return mergedDoc;
    }

    protected String getStartPageFromParams(int expectedPos, String... params)
    {
        String startPageParam = String.valueOf(1);

        if ((params != null) && (params.length > expectedPos))
        {
            startPageParam = params[expectedPos];
        }

        return startPageParam;
    }

    protected String getEndPageFromParams(int expectedPos, String... params)
    {
        String endPageParam = String.valueOf(Integer.MAX_VALUE);

        if ((params != null) && (params.length > expectedPos))
        {
            endPageParam = params[expectedPos];
        }

        return endPageParam;
    }


    public String getCDLStringFromListWithNames(List<Map<String,Object>> items)
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
                sb.append(", ");
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

    public static void setTextIfNotNullAndNotEmpty(TextView labelView, boolean collapseIfNullorEmpty, @NonNull TextView textView, String str)
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

    public static void setLabelVisibilityBasedOnStringValues(TextView labelView, String... strs)
    {
        setLabelVisibilityBasedOnStringValues(labelView, true, strs);
    }

    public static void setLabelVisibilityBasedOnStringValues(TextView labelView, boolean collapseIfNullorEmpty, String... strs)
    {
        int collapseIfNullorEmptyVal = View.GONE;

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

            int visibilty = View.VISIBLE;
            for (String str : strs)
            {
                if (isNullOrEmpty(str))
                {
                    visibilty = collapseIfNullorEmptyVal;

                }
            }

            labelView.setVisibility(visibilty);
        }

    }
}
