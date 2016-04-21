package com.example.arun.popularmovies;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.Spinner;
import android.widget.Toast;

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
import java.util.Arrays;
import java.util.List;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {


    public MovieAdapter imageAdapter;
    public ArrayList<Movie> moviesArrayList;

    public MainActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        String userPref = "popular";

        //Find the spinner and set adapter
        Spinner spinner = (Spinner)rootView.findViewById(R.id.spinner);

        List<String> userChoices = new ArrayList<>(Arrays.asList(new String[]{"Popular", "Top Rated"}));
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(getActivity(), R.layout.support_simple_spinner_dropdown_item,userChoices);
        spinner.setAdapter(arrayAdapter);


        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String userPref = getSnakeCase((String)adapterView.getItemAtPosition(i));
                Log.d("Spinner", userPref);
                new FetchMovieDataTask().execute(userPref);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });


        GridView gridView = (GridView)rootView.findViewById(R.id.movies_grid);
        ArrayList<Movie> movies = new ArrayList<>();
        imageAdapter = new MovieAdapter(getActivity(), movies);
        gridView.setAdapter(imageAdapter);


        new FetchMovieDataTask().execute(userPref);


        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                Movie movie = imageAdapter.getItem(i);
                Intent intent = new Intent(getActivity(), MovieDetailActivity.class);

                intent.putExtra("movie", movie);
                startActivity(intent);

            }
        });
        return rootView;
    }

    public String getSnakeCase(String string){

        String[] parts = string.toLowerCase().split(" ");
        String snakeCaseString = "";
        for(String part:parts){
            snakeCaseString += part +"_";
        }
        return snakeCaseString.substring(0, snakeCaseString.length()-1);
    }

    //Inner class to fetch data from tmdb api

    public class FetchMovieDataTask extends AsyncTask<String, Void, ArrayList<Movie>> {

        public final String LOG_TAG = FetchMovieDataTask.class.getSimpleName();


        /**
         *
         * @param relativeUrl obtained from TMDB API
         * @return the the Absolute URL for the movie poster
         */
        public String getAbsoluteUrl(String relativeUrl){
            String baseUrl = "http://image.tmdb.org/t/p/w342";
            return baseUrl + relativeUrl;
        }

        //Helper method to parse response from TMDB API

        /**
         *
         * @param response
         * @return the array of image URLs
         */
        public ArrayList<Movie> parseData(String response) {
            moviesArrayList = new ArrayList<Movie>();
            Movie newMovie;
            String movieTitle, movieReleaseDate, relativeUrl, moviePlot, moviePosterUrl, backdropPath, movieBackdropurl;
            int movieId, movieVotes;
            if (response != null) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    JSONArray movies = jsonObject.getJSONArray("results");
                    JSONObject movie;

                    //Loop through the movies array and add the image url to the arrayList
                    for (int index = 0; index < movies.length(); index++) {

                        //Get access to a particular movie
                        movie = movies.getJSONObject(index);

                        //Get the various entities of a movie
                        movieId = movie.getInt("id");
                        movieTitle = movie.getString("title");
                        movieReleaseDate = movie.getString("release_date");
                        relativeUrl = movie.getString("poster_path");
                        movieVotes = movie.getInt("vote_count");
                        moviePlot = movie.getString("overview");
                        backdropPath = movie.getString("backdrop_path");

                        //Get the absolute url of the movie poster
                        moviePosterUrl = getAbsoluteUrl(relativeUrl);
                        movieBackdropurl =getAbsoluteUrl(backdropPath);

                        //Construct a new movie
                        newMovie = new Movie(movieId, movieTitle, moviePosterUrl, moviePlot, movieReleaseDate, movieVotes, movieBackdropurl);

                        //Add the movie to the moviesArrayList
                        moviesArrayList.add(newMovie);
                    }
                } catch (JSONException e) {
                    Log.e(LOG_TAG, e.toString());
                }
            }
            return moviesArrayList;
        }
        @Override
        protected void onPreExecute() {
            Log.d(LOG_TAG, "OnPreExecuted");
            super.onPreExecute();
            Toast.makeText(getActivity(), getString(R.string.loading_toast),Toast.LENGTH_SHORT).show();
        }


        @Override
        protected ArrayList<Movie> doInBackground(String... params) {


            String baseUrl = "http://api.themoviedb.org/3/movie/";
            String API_KEY = "api_key";
            String TMDB_API_KEY = "e9a4689313dd5ce870efaeef49d348b8";
            String TYPE = params[0];

            baseUrl = baseUrl + TYPE;


            //Build the URI to query upon
            Uri uri = Uri.parse(baseUrl)
                    .buildUpon()
                    .appendQueryParameter(API_KEY, TMDB_API_KEY)
                    .build();

            Log.d(LOG_TAG, uri.toString());
            URL queryUrl = null;
            String response = "";
            HttpURLConnection httpUrlConnection = null;

            try{
                queryUrl = new URL(uri.toString());
                httpUrlConnection = (HttpURLConnection)queryUrl.openConnection();

                //Set the request method and connect
                httpUrlConnection.setRequestMethod("GET");
                httpUrlConnection.connect();

                int responseCode = httpUrlConnection.getResponseCode();
                Log.d(LOG_TAG, Integer.toString(responseCode));

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


            return parseData(response);
        }

        @Override
        protected void onPostExecute(ArrayList<Movie> movies) {

            //Update UI
            imageAdapter.setData(movies);
            imageAdapter.notifyDataSetChanged();
        }
    }
}
