package com.zero211.moviemaestro;

import android.content.Context;
import android.content.res.Resources;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.os.ConfigurationCompat;

import com.jayway.jsonpath.DocumentContext;
import com.zero211.utils.http.AbstractJSONResultFromURLAsyncTask;
import com.zero211.utils.http.HttpStringResponse;
import com.zero211.utils.http.HttpUtils;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.zero211.moviemaestro.StringUtils.isNullOrEmpty;
import static com.zero211.utils.http.HttpUtils.INTERNAL_ERROR_PATH;

public abstract class AbstractTMDBJSONResultFromURLTask extends AbstractJSONResultFromURLAsyncTask
{
    private static final String LOGTAG = AbstractTMDBJSONResultFromURLTask.class.getSimpleName();

    private enum PROGRESS_TYPE
    {
        PAGE_PROCESSED,
        RATE_LIMITED,
        MESSAGE
    }

    protected static final Locale DEFAULT_LOCALE = ConfigurationCompat.getLocales(Resources.getSystem().getConfiguration()).get(0);
    protected static final String LOCALE_STR = DEFAULT_LOCALE.toString().replace("_","-");
    protected static final String REGION_STR = DEFAULT_LOCALE.getCountry();

    protected static final String API_PREFIX = "https://api.themoviedb.org/3/";
    protected static final String API_KEY_PLACEHOLDER = "<API_KEY>";
    protected static final String START_DATE_PLACEHOLDER = "<START_DATE>";
    protected static final String END_DATE_PLACEHOLDER = "<END_DATE>";
    protected static final String PAGE_PLACEHOLDER = "<PAGE>";
    protected static final String QUERY_PLACEHOLDER = "<QUERY>";
    protected static final String MOVIE_ID_PLACEHOLDER = "<MOVIE_ID>";
    protected static final String PERSON_ID_PLACEHOLDER = "<PERSON_ID>";

    protected static final String ERRORS_PATH = "$.errors";
    protected static final String STATUS_CODE_PATH = "$.status_code";
    protected static final String STATUS_MESSAGE_PATH = "$.status_message";
    protected static final String PAGE_PATH = "$.page";
    protected static final String TOTAL_RESULTS_PATH = "$.total_results";
    protected static final String TOTAL_PAGES_PATH = "$.total_pages";
    protected static final String RESULTS_PATH = "$.results";


    private static final Long MILLIS_PER_SECOND = 1000L;

    private static final String RETRY_AFTER_HEADER = "Retry-After";

    private static final String RATELIMIT_LIMIT_HEADER = "X-RateLimit-Limit";
    private static int LAST_RATELIMIT_LIMIT = 40;

    private static final String RATELIMIT_REMAINING_HEADER = "X-RateLimit-Remaining";
    private static int LAST_RATELIMIT_REMAINING = 40;

    // reset value is a date in epoch millis
    private static final String RATELIMIT_RESET_EPOCH_SECONDS_HEADER = "X-RateLimit-Reset";
    private static long LAST_RATELIMIT_RESET_EPOCH_SECONDS = Calendar.getInstance().getTimeInMillis() / MILLIS_PER_SECOND;

    private Context context;
    private String urlWithAPIKeyTemplateStr;
    private int startPage = 1;
    private int endPage = Integer.MAX_VALUE;

    public AbstractTMDBJSONResultFromURLTask(@NonNull Context context, @NonNull String urlTemplateStr)
    {
        this(context, 1, 1, urlTemplateStr);
    }

    public AbstractTMDBJSONResultFromURLTask(@NonNull Context context, int startPage, int endPage, @NonNull String urlTemplateStr)
    {
        this.context = context;
        this.startPage = startPage;
        this.endPage = endPage;

        String api_key = StringUtils.rawTextFileToString(context, R.raw.tmdb_api_key);
        String trimmed_api_key = api_key.trim();
        this.urlWithAPIKeyTemplateStr = API_PREFIX + urlTemplateStr.replace(API_KEY_PLACEHOLDER, trimmed_api_key);
    }

    @Override
    protected HttpStringResponse doInBackground(String... params)
    {
        int retry_count = 0;
        int currentPage = startPage;

        DocumentContext mergedDoc = null;
        HttpStringResponse mergedResponse = null;

        do
        {
            if (retry_count > 20)
            {
                // TODO: What to do?  We've tried a page query 20 times, without success.
            }


            String pageURLStr = urlWithAPIKeyTemplateStr.replace(PAGE_PLACEHOLDER, String.valueOf(currentPage));

            if ((retry_count == 0) && (LAST_RATELIMIT_REMAINING <= 0))
            {
                try
                {
                    long nowMillis = Calendar.getInstance().getTimeInMillis();
                    long sleepMillis = (LAST_RATELIMIT_RESET_EPOCH_SECONDS * MILLIS_PER_SECOND) - nowMillis;
                    int sleepSeconds = (int)(sleepMillis / MILLIS_PER_SECOND);

                    publishProgress(PROGRESS_TYPE.RATE_LIMITED, currentPage, endPage, sleepSeconds);

                    Thread.sleep(sleepMillis);
                }
                catch (InterruptedException e)
                {
                    e.printStackTrace();
                }
            }

            HttpStringResponse pageResponse = super.doInBackground(pageURLStr);

            Map<String,List<String>> pageResponseHeaders = pageResponse.getResponseHeaders();

            // TODO: instead, maintain an ArrayList of HttpStringResponses with a mergedDoc member or just have a method to calc the rolled-up mergedoc?
            mergedResponse = pageResponse;

            if (isTMDBorInternalError(pageResponse))
            {
                mergedResponse.setDocumentContext(mergedDoc);

                DocumentContext pageDoc = HttpUtils.getJSONDocumentContext(pageResponse.getResponseString());
                int TMDB_status_code = pageDoc.read(STATUS_CODE_PATH);

                switch (TMDB_status_code)
                {
                    case 25: // Your request count (#) is over the allowed limit of (40).
                        int sleepSeconds = this.getHeaderSingleIntVal(pageResponseHeaders, RETRY_AFTER_HEADER);
                        long sleepMillis =  sleepSeconds * MILLIS_PER_SECOND;
                        try
                        {
                            publishProgress(PROGRESS_TYPE.RATE_LIMITED, currentPage, endPage, sleepSeconds);
                            Thread.sleep(sleepMillis);
                            retry_count++;
                        }
                        catch (InterruptedException e)
                        {
                            e.printStackTrace();
                        }
                        break;
                    default:
                        return mergedResponse;
                }
            }
            else
            {
                retry_count = 0;
                DocumentContext pageDoc = HttpUtils.getJSONDocumentContext(pageResponse.getResponseString());

                Integer TMDB_total_results = pageDoc.read(TOTAL_RESULTS_PATH);
                Integer TMDB_total_pages = pageDoc.read(TOTAL_PAGES_PATH);


                List<Map<String,Object>> pageResults = pageDoc.read(RESULTS_PATH);

                // Update the static rate-limit values
                LAST_RATELIMIT_LIMIT = this.getHeaderSingleIntVal(pageResponseHeaders, RATELIMIT_LIMIT_HEADER);
                LAST_RATELIMIT_REMAINING = this.getHeaderSingleIntVal(pageResponseHeaders, RATELIMIT_REMAINING_HEADER);
                LAST_RATELIMIT_RESET_EPOCH_SECONDS = this.getHeaderSingleLongVal(pageResponseHeaders, RATELIMIT_RESET_EPOCH_SECONDS_HEADER);

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

                // We need to potentially re-adjust the endPage after every API query response,
                // since query responses are not cursors/cached on the server and therefore the total_pages value could change.
                // Integer.MAX_VALUE represents a special value that means "whatever the total pages reported for the query is".
                if ((endPage  == Integer.MAX_VALUE) || ((TMDB_total_pages != null) && (endPage > TMDB_total_pages)))
                {
                    endPage = TMDB_total_pages;
                }

                currentPage++;
            }

        } while (currentPage < endPage);

        mergedResponse.setDocumentContext(mergedDoc);
        return mergedResponse;
    }

    protected void onProgressUpdate(Object... progress)
    {
        PROGRESS_TYPE progressType = (PROGRESS_TYPE)(progress[0]);
        Integer currentPage;
        Integer endPage;

        switch (progressType)
        {
            case PAGE_PROCESSED:
                currentPage = (Integer)(progress[1]);
                endPage = (Integer)(progress[2]);
                // TODO: Anything to do?  Maybe a progress bar instead of an indefinite?
                break;
            case RATE_LIMITED:
                currentPage = (Integer)(progress[1]);
                endPage = (Integer)(progress[2]);
                Integer sleepSeconds = (Integer)(progress[3]);
                if (sleepSeconds > 1)
                {
                    Toast.makeText(this.context, "Rate limiting from TMDB... waiting " + sleepSeconds + " seconds to retry query for page: " + currentPage + " of " + endPage, Toast.LENGTH_SHORT).show();
                }
                break;
            case MESSAGE:
                String msg = (String)(progress[1]);
                int toastShowTime = (Integer)(progress[2]);
                Toast.makeText(this.context, msg, toastShowTime).show();
                break;
                default:
        }
    }


    protected static boolean isTMDBorInternalError(@NonNull HttpStringResponse pageResponse)
    {
        DocumentContext pageDoc = HttpUtils.getJSONDocumentContext(pageResponse.getResponseString());

        String internal_err_msg = pageDoc.read(INTERNAL_ERROR_PATH);
        String TMDB_err_msgs = pageDoc.read(ERRORS_PATH);
        String TMDB_status_code = pageDoc.read(STATUS_CODE_PATH);
        String TMDB_status_msg = pageDoc.read(STATUS_MESSAGE_PATH);
        String TMDB_total_results = pageDoc.read(TOTAL_RESULTS_PATH);
        String TMDB_total_pages = pageDoc.read(TOTAL_PAGES_PATH);

        return ((internal_err_msg != null) || (TMDB_err_msgs != null) || (TMDB_status_msg != null));
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

    public Long getHeaderSingleLongVal(Map<String,List<String>> pageResponseHeaders, String headerName)
    {
        List<String> valueStrs = pageResponseHeaders.get(headerName);
        String valueStr = valueStrs.get(0);
        Long result = Long.valueOf(valueStr);
        return result;
    }

    public Integer getHeaderSingleIntVal(Map<String,List<String>> pageResponseHeaders, String headerName)
    {
        List<String> valueStrs = pageResponseHeaders.get(headerName);
        String valueStr = valueStrs.get(0);
        Integer result = Integer.valueOf(valueStr);
        return result;
    }

    public String getHeaderSingleStrVal(Map<String,List<String>> pageResponseHeaders, String headerName)
    {
        List<String> valueStrs = pageResponseHeaders.get(headerName);
        String result = valueStrs.get(0);
        return result;
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
}
