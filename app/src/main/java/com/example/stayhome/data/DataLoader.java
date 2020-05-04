package com.example.stayhome.data;

import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
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
    private String country;
    private AsyncResultListener asyncResultListener;

    public interface AsyncResultListener{
        void getResult(CovidData covidData);
    }

    public DataLoader(String country, AsyncResultListener asyncResultListener) {
        this.country = country;
        this.asyncResultListener = asyncResultListener;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected String doInBackground(Void... voids) {
        URL url = createUrl("https://api.covid19api.com/summary");
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
        if (country.equals("Worldwide")){

            CovidData covidData = new CovidData();
            try {
                JSONObject baseJsonResponse = new JSONObject(s);
                JSONObject data = baseJsonResponse.getJSONObject("Global");

                covidData.setCountry("Worldwide");
                covidData.setTotalConfirmed(data.getString("TotalConfirmed"));
                covidData.setNewConfirmed(data.getString("NewConfirmed"));
                covidData.setNewDeaths(data.getString("NewDeaths"));
                covidData.setTotalDeaths(data.getString("TotalDeaths"));
                covidData.setNewRecovered(data.getString("NewRecovered"));
                covidData.setTotalRecovered(data.getString("TotalRecovered"));

            } catch (JSONException e){
                Log.e("QueryUtils", "Problem parsing the earthquake JSON results", e);
            }
            asyncResultListener.getResult(covidData);

        }else {
            CovidData covidData = new CovidData();
            try {
                JSONObject baseJsonResponse = new JSONObject(s);
                JSONArray data = baseJsonResponse.getJSONArray("Countries");

                for (int i=0; i<data.length(); i++){
                    if ((data.getJSONObject(i).getString("Country")).equals(country)){
                        covidData.setCountry(data.getJSONObject(i).getString("Country"));
                        covidData.setNewConfirmed(String.valueOf(data.getJSONObject(i).getLong("NewConfirmed")));
                        covidData.setTotalConfirmed(String.valueOf(data.getJSONObject(i).getLong("TotalConfirmed")));
                        covidData.setNewDeaths(String.valueOf(data.getJSONObject(i).getLong("NewDeaths")));
                        covidData.setTotalDeaths(String.valueOf(data.getJSONObject(i).getLong("TotalDeaths")));
                        covidData.setNewRecovered(String.valueOf(data.getJSONObject(i).getLong("NewRecovered")));
                        covidData.setTotalRecovered(String.valueOf(data.getJSONObject(i).getLong("TotalRecovered")));

                    }
                }
            } catch (JSONException e){
                Log.e("QueryUtils", "Problem parsing the earthquake JSON results", e);
                return;
            }
            asyncResultListener.getResult(covidData);
        }

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
