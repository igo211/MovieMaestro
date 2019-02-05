package com.zero211.utils.http;

import android.os.AsyncTask;

import com.jayway.jsonpath.DocumentContext;

public abstract class AbstractJSONResultFromURLAsyncTask extends AsyncTask<String, Void, DocumentContext>
{
    private static final String LOGTAG = AbstractJSONResultFromURLAsyncTask.class.getSimpleName();

    @Override
    protected DocumentContext doInBackground(String... params)
    {
        String urlStr = null;

        if ((params != null) && (params.length > 0))
        {
            urlStr = params[0];
        }

        DocumentContext result = HttpUtils.getJSONDocumentContextFromURL(urlStr);

        return result;
    }

}
