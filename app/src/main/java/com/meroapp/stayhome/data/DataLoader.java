package com.meroapp.stayhome.data;

import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;

public class DataLoader extends AsyncTask<Void, Void, String> {
    private static final String TAG = "DATA LOADER";
    private AsyncResultListener asyncResultListener;

    public interface AsyncResultListener{
        void getResult(CovidData covidData);
    }

    public DataLoader(AsyncResultListener asyncResultListener) {
        this.asyncResultListener = asyncResultListener;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected String doInBackground(Void... voids) {
        URL url = createUrl("https://nepalcorona.info/api/v1/data/nepal");
        String jsonResponse = null;
        try {
            jsonResponse = makeHttpRequest(url);
        } catch (IOException e) {
            Log.e(TAG, "Problem making the HTTP request.", e);
        }
        return jsonResponse;
    }

    @Override
    protected void onPostExecute(String s) {
        if (TextUtils.isEmpty(s)) {
            Log.d(TAG, "onPreExecute: On......NULL JSON RESPONSE");
            return ;
        }
        CovidData covidData = new CovidData();
        try {
            JSONObject baseJsonResponse = new JSONObject(s);

            covidData.setTotalCase(baseJsonResponse.getString("tested_total"));
            covidData.setIsolation(baseJsonResponse.getString("in_isolation"));
            covidData.setQuarantined(baseJsonResponse.getString("quarantined"));
            covidData.setTotalDeaths(baseJsonResponse.getString("deaths"));
            covidData.setTotalRecovered(baseJsonResponse.getString("recovered"));
            covidData.setPositive(baseJsonResponse.getString("tested_positive"));

        } catch (JSONException e){
            Log.e("QueryUtils", "Problem parsing the earthquake JSON results", e);
        }
        asyncResultListener.getResult(covidData);

    }

    private static String makeHttpRequest(URL url) throws IOException {
        String jsonResponse = "";

        if (url == null) {
            return jsonResponse;
        }

        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setReadTimeout(10000 /* milliseconds */);
            urlConnection.setConnectTimeout(15000 /* milliseconds */);
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // If the request was successful (response code 200),
            // then read the input stream and parse the response.
            if (urlConnection.getResponseCode() == 200) {
                inputStream = urlConnection.getInputStream();
                jsonResponse = readFromStream(inputStream);
            } else {
                Log.e(TAG, "Error response code: " + urlConnection.getResponseCode());
            }
        } catch (IOException e) {
            Log.e(TAG, "Problem retrieving the earthquake JSON results.", e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (inputStream != null) {
                // Closing the input stream could throw an IOException, which is why
                // the makeHttpRequest(URL url) method signature specifies than an IOException
                // could be thrown.
                inputStream.close();
            }
        }
        return jsonResponse;
    }
    private static String readFromStream(InputStream inputStream) throws IOException {
        StringBuilder output = new StringBuilder();
        if (inputStream != null) {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
            BufferedReader reader = new BufferedReader(inputStreamReader);
            String line = reader.readLine();
            while (line != null) {
                output.append(line);
                line = reader.readLine();
            }
        }
        return output.toString();
    }

    private static URL createUrl(String stringUrl) {
        URL url = null;
        try {
            url = new URL(stringUrl);
        } catch (MalformedURLException e) {
            Log.e(TAG, "Problem building the URL ", e);
        }
        return url;
    }
}
