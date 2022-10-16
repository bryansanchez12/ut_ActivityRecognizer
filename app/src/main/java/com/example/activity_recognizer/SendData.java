package com.example.activity_recognizer;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class SendData extends AsyncTask< String, Void, Void> {

    private String URL_POST_DATA = "https://thecollector12.000webhostapp.com/SubmitActivityToDB.php";

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected void onPostExecute(Void result) {
        super.onPostExecute(result);
    }

    @SuppressLint("LongLogTag")
    @Override
    protected Void doInBackground(String... arg) {

        List<NameValuePair> params = new ArrayList<NameValuePair>();

        // MAC identifier from mobile phone
        params.add(new BasicNameValuePair("userID", arg[0]));

        // Activity recognized by the model
        params.add(new BasicNameValuePair("activity", arg[1]));

        // Coordinates from GPS
        params.add(new BasicNameValuePair("latitude", arg[2]));
        params.add(new BasicNameValuePair("longitude", arg[3]));

        ServiceHandler serviceClient = new ServiceHandler();
        String json = serviceClient.makeServiceCall(URL_POST_DATA, ServiceHandler.POST, params);


        Log.d("## Submission Request: ", "> " + json);

        // Checking the JSON data
        if (json != null) {
            try {
                JSONObject jsonObj = new JSONObject(json);
                boolean error = jsonObj.getBoolean("error");
                // Search for error node in json
                if (!error) {
                    // No error, hence data submitted successfully
                    Log.e("$$ Data submitted successfully!",
                            "> " + jsonObj.getString("message"));
                } else {
                    Log.e("$$ Error in submission: ",
                            "> " + jsonObj.getString("message"));
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

        } else {
            Log.e("$$ JSON Data", "Error!, JSON data corrupted");
        }

        return null;
    }


}
