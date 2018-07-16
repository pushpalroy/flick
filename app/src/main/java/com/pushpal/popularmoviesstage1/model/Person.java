package com.pushpal.popularmoviesstage1.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Person {
    @SerializedName("birthday")
    public String birthDay;

    @SerializedName("deathday")
    public String deathDay;

    @SerializedName("id")
    public String id;

    @SerializedName("name")
    public String name;

    @SerializedName("also_known_as")
    public List<String> alsoKnownAs;

    @SerializedName("gender")
    public int gender;

    @SerializedName("biography")
    public String biography;

    @SerializedName("popularity")
    public double popularity;

    @SerializedName("place_of_birth")
    public String placeOfBirth;

    @SerializedName("profile_path")
    public String profileImagePath;

    @SerializedName("adult")
    public boolean adult;

    @SerializedName("imdb_id")
    public String imdbId;

    @SerializedName("homePage")
    public String homePage;

    public String getBirthDay() {
        return birthDay;
    }

    public void setBirthDay(String birthDay) {
        this.birthDay = birthDay;
    }

    public String getDeathDay() {
        return deathDay;
    }

    public void setDeathDay(String deathDay) {
        this.deathDay = deathDay;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getAlsoKnownAs() {
        return alsoKnownAs;
    }

    public void setAlsoKnownAs(List<String> alsoKnownAs) {
        this.alsoKnownAs = alsoKnownAs;
    }

    public int getGender() {
        return gender;
    }

    public void setGender(int gender) {
        this.gender = gender;
    }

    public String getBiography() {
        return biography;
    }

    public void setBiography(String biography) {
        this.biography = biography;
    }

    public double getPopularity() {
        return popularity;
    }

    public void setPopularity(double popularity) {
        this.popularity = popularity;
    }

    public String getPlaceOfBirth() {
        return placeOfBirth;
    }

    public void setPlaceOfBirth(String placeOfBirth) {
        this.placeOfBirth = placeOfBirth;
    }

    public String getProfileImagePath() {
        return profileImagePath;
    }

    public void setProfileImagePath(String profileImagePath) {
        this.profileImagePath = profileImagePath;
    }

    public boolean isAdult() {
        return adult;
    }

    public void setAdult(boolean adult) {
        this.adult = adult;
    }

    public String getImdbId() {
        return imdbId;
    }

    public void setImdbId(String imdbId) {
        this.imdbId = imdbId;
    }

    public String getHomePage() {
        return homePage;
    }

    public void setHomePage(String homePage) {
        this.homePage = homePage;
    }
}
