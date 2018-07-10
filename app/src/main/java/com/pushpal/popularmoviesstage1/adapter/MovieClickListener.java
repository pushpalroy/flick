package com.pushpal.popularmoviesstage1.adapter;

import android.widget.ImageView;

import com.pushpal.popularmoviesstage1.model.Movie;

public interface MovieClickListener {
    void onMovieClick(int pos, Movie movie, ImageView shareImageView);
}