package com.example.arun.popularmovies;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by lazyass on 4/20/16.
 */
public class Movie implements Parcelable{
    private int id;
    private String title;
    private String posterPath;
    private String backdropPath;
    private String plot;
    private String releaseDate;
    private int votesCount;

    public Movie(int id, String title, String posterPath, String plot, String releaseDate, int votesCount, String backdropPath){
        this.id = id;
        this.title = title;
        this.posterPath = posterPath;
        this.plot = plot;
        this.releaseDate = releaseDate;
        this.votesCount = votesCount;
        this.backdropPath = backdropPath;
    }

    /**
     * Unwraps the contents in the order it was written from the parcel and constructs a new object
     * @param in
     */
    protected Movie(Parcel in) {
        id = in.readInt();
        title = in.readString();
        posterPath = in.readString();
        plot = in.readString();
        releaseDate = in.readString();
        votesCount = in.readInt();
        backdropPath = in.readString();
    }


    public int getVotesCount() {
        return votesCount;
    }

    // Getters for each of the instance variables
    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getPosterPath() {
        return posterPath;
    }

    public String getPlot() {
        return plot;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public String getBackdropPath() {return backdropPath; }

    public String toString(){
        return getTitle() + " " + getId() + " " + getReleaseDate() + "\n" + getPosterPath() + "\n"  + getBackdropPath() +  "\n" + getPlot() + "\n" + getVotesCount() + " votes";
    }

    /** Required methods for Parcelable interface **/
    @Override
    public int describeContents() {
        return 0;
    }

    /**
     *  Writes the object's contents into a parcel
     * @param parcel
     * @param i
     */
    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(getId());
        parcel.writeString(getTitle());
        parcel.writeString(getPosterPath());
        parcel.writeString(getPlot());
        parcel.writeString(getReleaseDate());
        parcel.writeInt(getVotesCount());
        parcel.writeString(getBackdropPath());
    }

    public static final Creator<Movie> CREATOR = new Creator<Movie>() {
        @Override
        public Movie createFromParcel(Parcel in) {
            return new Movie(in);
        }

        @Override
        public Movie[] newArray(int size) {
            return new Movie[size];
        }
    };


}
