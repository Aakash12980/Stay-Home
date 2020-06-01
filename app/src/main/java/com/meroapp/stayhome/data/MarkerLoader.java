package com.meroapp.stayhome.data;

import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class MarkerLoader extends AsyncTask<Void, Void, String> {
    private static final String TAG = "MARKER LOADER";
    private AsyncDataListener asyncDataListener;

    public interface AsyncDataListener{
        void getResult(List<Double[] > covidData);
    }

    public MarkerLoader(AsyncDataListener asyncDataListener) {
        this.asyncDataListener = asyncDataListener;
    }

    @Override
    protected String doInBackground(Void... voids) {
        URL url = createUrl("https://data.nepalcorona.info/api/v1/covid");
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
        try {
            List<Double[] > data = new ArrayList<>();
            JSONArray baseJsonResponse = new JSONArray(s);
            for (int i=0; i<baseJsonResponse.length(); i++){
                if (baseJsonResponse.getJSONObject(i).getString("currentState").equals("active")){
                    Double lat = baseJsonResponse.getJSONObject(i).getJSONObject("point").getJSONArray("coordinates").getDouble(1);
                    Double lng = baseJsonResponse.getJSONObject(i).getJSONObject("point").getJSONArray("coordinates").getDouble(0);
                    data.add(new Double[]{lat, lng});
                }
            }
            asyncDataListener.getResult(data);

        } catch (JSONException e){
            Log.e("QueryUtils", "Problem parsing the earthquake JSON results", e);
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
