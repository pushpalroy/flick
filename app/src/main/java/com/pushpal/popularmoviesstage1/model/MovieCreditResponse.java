package com.pushpal.popularmoviesstage1.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class MovieCreditResponse {
    @SerializedName("id")
    private int id;

    @SerializedName("cast")
    private List<MovieCast> cast;

    @SerializedName("crew")
    private List<MovieCrew> crew;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public List<MovieCast> getCast() {
        return cast;
    }

    public void setCast(List<MovieCast> cast) {
        this.cast = cast;
    }

    public List<MovieCrew> getCrew() {
        return crew;
    }

    public void setCrew(List<MovieCrew> crew) {
        this.crew = crew;
    }
}