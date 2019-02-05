package com.zero211.utils.http;

import android.util.Log;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.ReadContext;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class HttpUtils
{
    private static final String LOGTAG = HttpUtils.class.getSimpleName();

    public static final String INTERNAL_ERROR_LABEL = "internal_error";
    public static final String INTERNAL_ERROR_PATH = "$." + INTERNAL_ERROR_LABEL;

    public static DocumentContext getJSONDocumentContextFromURL(String urlStr)
    {
        DocumentContext result = null;

        HttpStringResponse response = getHttpResponseFromURL(urlStr);

        String responseStr = response.getResponseString();

        if (!(responseStr.trim().startsWith("{")))
        {
            responseStr = "{\"" + INTERNAL_ERROR_LABEL + "\" : \"" + responseStr + "\"";
        }

        if (!(responseStr.trim().endsWith("}")))
        {
            responseStr = responseStr + "}";
        }

        Configuration conf = Configuration.defaultConfiguration();
        conf = conf.addOptions(Option.DEFAULT_PATH_LEAF_TO_NULL);

        result = JsonPath.using(conf).parse(responseStr);

        return result;
    }

    public static HttpStringResponse getHttpResponseFromURL(String urlStr)
    {
        Log.i(LOGTAG, "downloading " + urlStr);

        HttpStringResponse result = new HttpStringResponse();

        try
        {
            URL url = new URL(urlStr);
            HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
            int responseCode = con.getResponseCode();
            result.setResponseCode(responseCode);

            try (AutoCloseable autoCloseableCon = () -> con.disconnect();
                 InputStream is = responseCode >=400 ? con.getErrorStream() : con.getInputStream();
                 BufferedReader in = new BufferedReader(new InputStreamReader(is))
            )
            {
                char[] buf = new char[1024];
                StringBuilder sb = new StringBuilder();
                int charsRead;

                while((charsRead = in.read(buf,0, buf.length)) != -1)
                {
                    sb.append(buf,0, charsRead);
                }

                String responseString = sb.toString();
                Log.i(LOGTAG, "download result is: " + responseString);

                result.setResponseString(responseString);

                return result;

            }
            catch (Exception e)
            {
                return modifyResponseForException(urlStr, result, e);
            }

        }
        catch (Exception e)
        {
            return modifyResponseForException(urlStr, result, e);
        }

    }

    private static HttpStringResponse modifyResponseForException(String urlStr, HttpStringResponse response, Exception e)
    {
        e.printStackTrace();
        String preExistingMsg = response.getResponseString();
        String msg = "Got an Exception for url: '" + urlStr + "' :'" + e.getMessage() + "'";
        if ((preExistingMsg != null) && (preExistingMsg.trim().length() >0))
        {
            msg = msg + " with a response body of: '" + preExistingMsg + "'";
        }

        Log.i(LOGTAG, msg);

        response.setResponseString(msg);
        return response;
    }
}
