package com.example.arun.popularmovies;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
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
    private MovieAdapter imageAdapter;
    private ArrayList<Movie> moviesArrayList;
    private SharedPreferences sharedPreferences;
    private Spinner spinner;
    private int count = 0;

    public MainActivityFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);


        //Find if the user has network connection, show an alert if he has no internet connection
        ConnectivityManager connectivityManager = (ConnectivityManager)getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if(networkInfo == null || !networkInfo.isConnected()){
            //Toast.makeText(getActivity(), "No connection.", Toast.LENGTH_LONG).show();

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage("No internet connection. Try after connecting to the internet");
            builder.show();
            return rootView;
        }

        //Get a handle to sharedPreferences
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

        ArrayList<Movie> movies = new ArrayList<>();

        //Find the spinner and set adapter
        spinner = (Spinner)rootView.findViewById(R.id.spinner);


        //Populate the adapter
        List<String> userChoices = new ArrayList<>(Arrays.asList(new String[]{"Popular", "Top Rated"}));
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(getActivity(), R.layout.support_simple_spinner_dropdown_item,userChoices);
        spinner.setAdapter(arrayAdapter);



        //Attaching the adapter to gridview
        GridView gridView = (GridView)rootView.findViewById(R.id.movies_grid);
        imageAdapter = new MovieAdapter(getActivity(), new ArrayList<Movie>());
        gridView.setAdapter(imageAdapter);

        //Restore the seleced item in the spinner
        int position = sharedPreferences.getInt("item_selected_pos", 0);
        spinner.setSelection(position, true);

        //Restore data using the bundle in case of configuration changes
        if(savedInstanceState != null){
            Log.i("saveInstance", "restoring instance");
            movies = savedInstanceState.getParcelableArrayList(getString(R.string.parcelable_bundle_key));
            imageAdapter.setData(movies);
            imageAdapter.notifyDataSetChanged();
            Toast.makeText(getActivity(),"Adapter is redrawing", Toast.LENGTH_SHORT).show();
            Log.i("saveInstance", "adapter set");
        }

        //Make the api call
        else{
            // String userPref = sharedPreferences.getString("user_pref", getString(R.string.popular));
            String userPref = sharedPreferences.getString("user_pref", getString(R.string.popular));
            Log.i("else", "Making the API call");
            new FetchMovieDataTask().execute(userPref);
        }


        //Find the imageView in the gridview to animate it using sharedElement Transitions
        LayoutInflater layoutInflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View viewToTransition = layoutInflater.inflate(R.layout.movie_item,null);
        final ImageView imageViewToTransition = (ImageView)viewToTransition.findViewById(R.id.movie_poster_image_view);


        //Set an item click listener to the gridview
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                Movie movie = imageAdapter.getItem(i);
                Intent intent = new Intent(getActivity(), MovieDetailActivity.class);

                intent.putExtra("movie", movie);

                // Establish a sharedElement Transition
                ActivityOptionsCompat options = ActivityOptionsCompat.
                        makeSceneTransitionAnimation(getActivity(), (View)imageViewToTransition, getString(R.string.transition_string));
                ActivityCompat.startActivity(getActivity(), intent, options.toBundle());

            }
        });

        //Setup an item selected listener to change the list of movies based on the user's choice
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                //Get the user choice and store it in SharedPreferences
                String userPref = getSnakeCase((String) adapterView.getItemAtPosition(i));

                Log.d("onItemSelected", "Listener triggered, calling API");
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("user_pref", userPref);
                editor.putInt("item_selected_pos", i);
                editor.apply();

                //Get the user's preference
                userPref = sharedPreferences.getString("user_pref", getString(R.string.popular));
                new FetchMovieDataTask().execute(userPref);

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(getString(R.string.parcelable_bundle_key), moviesArrayList);
        Log.i("OnSaveInstancestate", "saved");
    }


    @Override
    public void onResume() {
        super.onResume();
    }

    /**
     *
     * @param string
     * @returns the snakecased version of the string
     */
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

            //Log.d(LOG_TAG, uri.toString());
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
