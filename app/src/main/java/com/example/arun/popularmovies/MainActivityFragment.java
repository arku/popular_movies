package com.example.arun.popularmovies;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {

    public MainActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        String userPref = "popularity.desc";

        // TODO: 4/15/16 Create an adapter for GridView 
        new FetchMovieDataTask().execute(userPref);
        return rootView;
    }

    //Inner class to fetch data from tmdb api

    public class FetchMovieDataTask extends AsyncTask<String, Void, List<String>> {

        public final String LOG_TAG = FetchMovieDataTask.class.getSimpleName();


        /**
         *
         * @param relativeUrl obtained from TMDB API
         * @return the the Absolute URL for the movie poster
         */
        public String getAbsoluteUrl(String relativeUrl){
            String baseUrl = "http://image.tmdb.org/t/p/";
            baseUrl += "w185";
            return baseUrl + relativeUrl;
        }

        //Helper method to parse response from TMDB API

        /**
         *
         * @param response
         * @return the array of image URLs
         */
        public List<String> parseData(String response){
            List<String> imageUrlStrings = new ArrayList<>();

            try{
                JSONObject jsonObject = new JSONObject(response);
                JSONArray movies = jsonObject.getJSONArray("results");

                //Loop through the movies array and add the image url to the arrayList
                for(int index=0;index < movies.length();index++){
                    JSONObject movie = movies.getJSONObject(index);
                    String relativeUrl = movie.getString("poster_path");

                    String absoluteUrl = getAbsoluteUrl(relativeUrl);
                    imageUrlStrings.add(absoluteUrl);
                }
            }
            catch (JSONException e){
                Log.e(LOG_TAG, e.toString());
            }
            return imageUrlStrings;
        }


        @Override
        protected void onPreExecute() {
            Log.d(LOG_TAG, "OnPreExecuted");
            super.onPreExecute();
        }


        @Override
        protected List<String> doInBackground(String... params) {

            // TODO: 4/15/16 Build the URI to fetch data

            String baseUrl = "http://api.themoviedb.org/3/discover/movie";
            String API_KEY = "api_key";
            String TMDB_API_KEY = "e9a4689313dd5ce870efaeef49d348b8";
            String SORT_BY = "sort_by";
            String SORT_POPULARITY = params[0];


            //Build the URI to query upon
            Uri uri = Uri.parse(baseUrl)
                    .buildUpon()
                    .appendQueryParameter(API_KEY, TMDB_API_KEY)
                    .appendQueryParameter(SORT_BY, SORT_POPULARITY)
                    .build();

            URL queryUrl = null;
            String response = "";
            HttpURLConnection httpUrlConnection = null;

            // TODO: 4/15/16 Setup HTTPUrl Connection to read data in a buffer
            try{
                queryUrl = new URL(uri.toString());
                httpUrlConnection = (HttpURLConnection)queryUrl.openConnection();

                //Set the request method and connect
                httpUrlConnection.setRequestMethod("GET");
                httpUrlConnection.connect();

                int responseCode = httpUrlConnection.getResponseCode();
                //Log.d(LOG_TAG, Integer.toString(responseCode));

                if(responseCode == HttpURLConnection.HTTP_OK){
                    InputStream inputStream = httpUrlConnection.getInputStream();
                    StringBuffer stringBuffer = new StringBuffer();

                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

                    //Add the response to the StringBuffer
                    String line="";
                    while((line = bufferedReader.readLine()) != null)
                    {
                        stringBuffer.append(line);
                        stringBuffer.append("\n");
                    }
                    response = stringBuffer.toString();
                    Log.i(LOG_TAG, response);
                }


                else{
                    Log.d(LOG_TAG, "Response Code: " + responseCode);
                }

            }

            catch (IOException e){
                Log.e(LOG_TAG, e.toString());
            }
            finally {
                if(httpUrlConnection != null) {
                    try {
                        httpUrlConnection.disconnect();
                    } catch (Exception e) {
                        Log.e(LOG_TAG, e.toString());
                    }
                }
            }

            // TODO: 4/15/16 Parse data fetched

            List<String> imageUrlStrings = parseData(response);
            // TODO: 4/15/16 Update UI
            return imageUrlStrings;
        }

        @Override
        protected void onPostExecute(List<String> imageUrlStrings) {
            Log.d(LOG_TAG, imageUrlStrings.toString());
        }
    }
}
