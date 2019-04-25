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
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

public class HttpUtils
{
    private static final String LOGTAG = HttpUtils.class.getSimpleName();

    public static final String INTERNAL_ERROR_LABEL = "internal_error";
    public static final String INTERNAL_ERROR_PATH = "$." + INTERNAL_ERROR_LABEL;

    public static DocumentContext getJSONDocumentContext(String jsonResponseString)
    {
        if (!(jsonResponseString.trim().startsWith("{")))
        {
            jsonResponseString = "{\"" + INTERNAL_ERROR_LABEL + "\" : \"" + jsonResponseString + "\"";
        }

        if (!(jsonResponseString.trim().endsWith("}")))
        {
            jsonResponseString = jsonResponseString + "}";
        }

        Configuration conf = Configuration.defaultConfiguration();
        conf = conf.addOptions(Option.DEFAULT_PATH_LEAF_TO_NULL);

        DocumentContext result = JsonPath.using(conf).parse(jsonResponseString);

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

            Map<String, List<String>> response_headers = con.getHeaderFields();
            result.setResponseHeaders(response_headers);

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
