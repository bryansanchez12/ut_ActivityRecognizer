package com.example.activity_recognizer;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.DefaultHttpClient;

import android.util.Log;
public class ServiceHandler {

    // HashMaps depending on the course type
    private Map<String, List> accelerometerData;
    private Map<String, List> gpsData;


    static InputStream is = null;
    static String response = null;
    public final static int GET = 1;
    public final static int POST = 2;

    public ServiceHandler() {
        accelerometerData = new HashMap<>();
        gpsData = new HashMap<>();
    }


    /**
     * Create a HTTP request and send it to an URL that will stores data into a DataBase
     * @param url of the php class that takes care of storing the data into the DB
     * @param method of the http request: GET or POST
     * @return the response of the request if any
     */
    public String makeServiceCall(String url, int method) {
        return this.makeServiceCall(url, method, null);
    }


    /**
     * Create a HTTP request and send it to an URL that will stores data into a DataBase
     * @param url of the php class that takes care of storing the data into the DB
     * @param method of the http request: GET or POST
     * @param params list of parameters that will be sent with the request if any
     * @return the response of the request if any
     */
    public String makeServiceCall(String url, int method,
                                  List<NameValuePair> params) {
        try {

            DefaultHttpClient httpClient = new DefaultHttpClient();
            HttpEntity httpEntity = null;
            HttpResponse httpResponse = null;

            // POST: send data to the DB
            if (method == POST) {
                HttpPost httpPost = new HttpPost(url);

                if (params != null) {
                    httpPost.setEntity(new UrlEncodedFormEntity(params));
                }

                httpResponse = httpClient.execute(httpPost);

            // GET: fetch data from the DB
            } else if (method == GET) {

                if (params != null) {
                    String paramString = URLEncodedUtils
                            .format(params, "utf-8");
                    url += "?" + paramString;
                }

                HttpGet httpGet = new HttpGet(url);
                httpResponse = httpClient.execute(httpGet);

            }

            httpEntity = httpResponse.getEntity();
            is = httpEntity.getContent();

        } catch (Exception e) {
            e.printStackTrace();
        }


        // From the response, build a string with its data
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    is, "UTF-8"), 8);
            StringBuilder sb = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
            is.close();
            response = sb.toString();
        } catch (Exception e) {
            Log.e("Buffer Error", "> Error: " + e.toString());
        }

        return response;
    }
}
