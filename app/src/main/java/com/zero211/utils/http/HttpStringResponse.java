package com.zero211.utils.http;

import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class HttpStringResponse
{
    private static final String LOGTAG = HttpStringResponse.class.getSimpleName();

    private int responseCode;
    private String responseString;



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

}
