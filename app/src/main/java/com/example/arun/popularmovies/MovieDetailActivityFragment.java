package com.example.arun.popularmovies;

import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * A placeholder fragment containing a simple view.
 */
public class MovieDetailActivityFragment extends Fragment {

    private final String LOG_TAG = MovieDetailActivityFragment.class.getSimpleName();
    private final String votes =" VOTES";
    public MovieDetailActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_movie_detail, container, false);

        /** Find the views **/
        TextView movieNameTextView = (TextView)rootView.findViewById(R.id.movie_name_text_view);
        ImageView moviePosterImageView = (ImageView)rootView.findViewById(R.id.poster_image_view);

        TextView movieReleaseDateTextView = (TextView)rootView.findViewById(R.id.movie_release_date_text_view);
        TextView movieVotesTextView = (TextView)rootView.findViewById(R.id.movie_votes_text_view);

        TextView moviePlotTextView = (TextView)rootView.findViewById(R.id.movie_plot_text_view);
        Button knowMoreButton = (Button)rootView.findViewById(R.id.know_more_button);


        /** Get the parcel from the intent and use it to populate views **/
        Intent intent = getActivity().getIntent();
        Movie movie = intent.getExtras().getParcelable("movie");

        final String movieName = movie.getTitle();
        movieNameTextView.setText(movieName);

        Picasso.with(getActivity())
                .load(movie.getBackdropPath())
                .into(moviePosterImageView);

        movieReleaseDateTextView.setText(formatDate(movie.getReleaseDate()));

        movieVotesTextView.setText(Integer.toString(movie.getVotesCount()) + votes);
        moviePlotTextView.setText(movie.getPlot());

        knowMoreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Toast.makeText(getActivity(), "Opening new tab", Toast.LENGTH_SHORT).show();

                //Implicit intent to open a browser with search text as the movieName

                 String googleUrl = "http://www.google.com/#q=";
                Intent implicitIntent = new Intent()
                        .setAction(Intent.ACTION_WEB_SEARCH)
                        .putExtra(SearchManager.QUERY, movieName + getString(R.string.movie));
                startActivity(implicitIntent);
            }
        });


        return rootView;
    }

    public String formatDate(String dateString){
        SimpleDateFormat existingFormat = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy");
        try{
            Date date = existingFormat.parse(dateString);
            return dateFormat.format(date).toString();
        }
        catch(Exception e){
            Log.e(LOG_TAG, e.toString());
        }
        return "";
    }
}
