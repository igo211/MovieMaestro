package com.zero211.utils.http;

import android.util.Log;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

public class HttpStringResponse
{
    private static final String LOGTAG = HttpStringResponse.class.getSimpleName();

    private Map<String, List<String>> responseHeaders;
    private int responseCode;
    private String responseString;
    private DocumentContext documentContext;

    public HttpStringResponse()
    {
        this(0);
    }

    public HttpStringResponse(int responseCode)
    {
        this(responseCode, null);
    }

    public HttpStringResponse(int responseCode, String responseString)
    {
        this.setResponseCode(responseCode);
        this.setResponseString(responseString);
    }



    public void setResponseCode(int responseCode)
    {
        this.responseCode = responseCode;
    }

    public int getResponseCode()
    {
        return responseCode;
    }

    public void setResponseString(String responseString)
    {
        this.responseString = responseString;
    }

    public String getResponseString()
    {
        return responseString;
    }

    public Map<String, List<String>> getResponseHeaders()
    {
        return responseHeaders;
    }

    public void setResponseHeaders(Map<String, List<String>> responseHeaders)
    {
        this.responseHeaders = responseHeaders;
    }

    public DocumentContext getDocumentContext()
    {
        return documentContext;
    }

    public void setDocumentContext(DocumentContext documentContext)
    {
        this.documentContext = documentContext;
    }
}
