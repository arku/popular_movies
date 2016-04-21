package com.example.arun.popularmovies;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by lazyass on 4/15/16.
 */
public class MovieAdapter extends ArrayAdapter<Movie> {

    private Context mContext;
    private ArrayList<Movie> mMovies;

    public MovieAdapter(Context context, ArrayList<Movie> movies) {
        super(context, 0, movies);
        mContext = context;
        mMovies = movies;

    }

    public void setData(ArrayList<Movie> movies){
        mMovies = movies;
    }

    @Override
    public int getCount() {
        return mMovies == null? 0:mMovies.size();
    }

    @Override
    public Movie getItem(int i) {
        return mMovies.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {

        //Uses ViewHolder pattern
        ViewHolder viewHolder;

        //Inflate the layout for the grid item
        LayoutInflater inflater = LayoutInflater.from(mContext);

        if(view == null){
            //view is not yet recycled
            view = inflater.inflate(R.layout.movie_item, viewGroup, false);

            viewHolder = new ViewHolder();
            viewHolder.posterImageView = (ImageView)view.findViewById(R.id.movie_poster_image_view);
            view.setTag(viewHolder);
        }
        else{
            //Recycled view, no need to use findViewById
            viewHolder = (ViewHolder)view.getTag();
        }

        Picasso.with(mContext)
        .load(mMovies.get(i).getPosterPath()).into(viewHolder.posterImageView);
        return view;
    }

    static class ViewHolder{
        private ImageView posterImageView;
    }
}
