package com.pushpal.popularmoviesstage1.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class MovieTrailerResponse {
    @SerializedName("id")
    private int id;

    @SerializedName("results")
    private List<MovieTrailer> trailers;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public List<MovieTrailer> getTrailers() {
        return trailers;
    }

    public void setTrailers(List<MovieTrailer> trailers) {
        this.trailers = trailers;
    }
}