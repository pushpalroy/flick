package com.pushpal.popularmoviesstage1.model;

import com.google.gson.annotations.SerializedName;

public class MovieReview {
    @SerializedName("id")
    private String reviewId;

    @SerializedName("author")
    private String reviewer;

    @SerializedName("content")
    private String reviewContent;

    @SerializedName("url")
    private String url;

    public String getReviewId() {
        return reviewId;
    }

    public void setReviewId(String reviewId) {
        this.reviewId = reviewId;
    }

    public String getReviewer() {
        return reviewer;
    }

    public void setReviewer(String reviewer) {
        this.reviewer = reviewer;
    }

    public String getReviewContent() {
        return reviewContent;
    }

    public void setReviewContent(String reviewContent) {
        this.reviewContent = reviewContent;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
