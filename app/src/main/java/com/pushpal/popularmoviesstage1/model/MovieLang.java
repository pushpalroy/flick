package com.pushpal.popularmoviesstage1.model;

import com.google.gson.annotations.SerializedName;

public class MovieLang {

    @SerializedName("name")
    public String name;
    @SerializedName("iso_639_1")
    private String abbreviation;
    @SerializedName("english_name")
    private String englishName;

    public MovieLang(String abbreviation, String englishName, String name) {
        this.abbreviation = abbreviation;
        this.englishName = englishName;
        this.name = name;
    }

    public String getAbbreviation() {
        return abbreviation;
    }

    public void setAbbreviation(String abbreviation) {
        this.abbreviation = abbreviation;
    }

    public String getEnglishName() {
        return englishName;
    }

    public void setEnglishName(String englishName) {
        this.englishName = englishName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
