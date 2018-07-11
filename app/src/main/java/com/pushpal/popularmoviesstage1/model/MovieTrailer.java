package com.pushpal.popularmoviesstage1.model;

import com.google.gson.annotations.SerializedName;

public class MovieTrailer {

    @SerializedName("id")
    private String videoId;

    @SerializedName("iso_639_1")
    private String iso_639_1;

    @SerializedName("iso_3166_1")
    private String iso_3166_1;

    @SerializedName("key")
    private String videoKey;

    @SerializedName("name")
    private String videoName;

    @SerializedName("site")
    private String site;

    @SerializedName("size")
    private int size;

    @SerializedName("type")
    private String videoType;

    public String getVideoId() {
        return videoId;
    }

    public void setVideoId(String videoId) {
        this.videoId = videoId;
    }

    public String getIso_639_1() {
        return iso_639_1;
    }

    public void setIso_639_1(String iso_639_1) {
        this.iso_639_1 = iso_639_1;
    }

    public String getIso_3166_1() {
        return iso_3166_1;
    }

    public void setIso_3166_1(String iso_3166_1) {
        this.iso_3166_1 = iso_3166_1;
    }

    public String getVideoKey() {
        return videoKey;
    }

    public void setVideoKey(String videoKey) {
        this.videoKey = videoKey;
    }

    public String getVideoName() {
        return videoName;
    }

    public void setVideoName(String videoName) {
        this.videoName = videoName;
    }

    public String getSite() {
        return site;
    }

    public void setSite(String site) {
        this.site = site;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public String getVideoType() {
        return videoType;
    }

    public void setVideoType(String videoType) {
        this.videoType = videoType;
    }
}
