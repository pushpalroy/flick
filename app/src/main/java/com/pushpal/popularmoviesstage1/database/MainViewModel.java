package com.pushpal.popularmoviesstage1.database;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;
import android.util.Log;

import com.pushpal.popularmoviesstage1.model.Movie;

import java.util.List;

public class MainViewModel extends AndroidViewModel {

    private static final String TAG = MainViewModel.class.getSimpleName();
    private LiveData<List<Movie>> movies;

    public MainViewModel(@NonNull Application application) {
        super(application);

        MovieDatabase movieDatabase = MovieDatabase.getInstance(this.getApplication());
        Log.d(TAG, "Actively retrieving the movies from the database");
        movies = movieDatabase.movieDao().getAllMovies();
    }

    public LiveData<List<Movie>> getMovies() {
        return movies;
    }
}
