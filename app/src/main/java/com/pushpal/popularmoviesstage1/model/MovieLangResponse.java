package com.pushpal.popularmoviesstage1.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

class MovieLangResponse {

    @SerializedName("JSON")
    private List<MovieLang> languages;

    public List<MovieLang> getLanguages() {
        return languages;
    }

    public void setLanguages(List<MovieLang> languages) {
        this.languages = languages;
    }
}
