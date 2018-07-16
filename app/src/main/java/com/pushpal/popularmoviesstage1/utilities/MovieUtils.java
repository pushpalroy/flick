package com.pushpal.popularmoviesstage1.utilities;

import android.support.annotation.NonNull;
import android.util.Log;

import com.pushpal.popularmoviesstage1.activity.MainActivity;
import com.pushpal.popularmoviesstage1.model.Movie;
import com.pushpal.popularmoviesstage1.model.MovieLang;
import com.pushpal.popularmoviesstage1.networking.RESTClient;
import com.pushpal.popularmoviesstage1.networking.RESTClientInterface;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MovieUtils {
    private static final String TAG = MovieUtils.class.getSimpleName();

    public static void fetchLanguages() {
        RESTClientInterface restClientInterface = RESTClient.getClient().create(RESTClientInterface.class);
        Call<List<MovieLang>> call;
        final List<MovieLang> finalMovieLanguages = new ArrayList<>();

        call = restClientInterface.getLanguages(Constants.API_KEY);

        if (call != null) {
            call.enqueue(new Callback<List<MovieLang>>() {
                @Override
                public void onResponse(@NonNull Call<List<MovieLang>> call, @NonNull Response<List<MovieLang>> response) {
                    int statusCode = response.code();

                    if (statusCode == 200) {
                        if (response.body() != null) {
                            finalMovieLanguages.addAll(response.body());
                            MainActivity.sLanguageMap = new HashMap<>();
                            for (MovieLang movieLang : finalMovieLanguages)
                                MainActivity.sLanguageMap.put(movieLang.getAbbreviation(), movieLang.getEnglishName());
                        }
                    }
                }

                @Override
                public void onFailure(@NonNull Call<List<MovieLang>> call, @NonNull Throwable throwable) {
                    // Log error here since request failed
                    Log.e(TAG, throwable.toString());
                }
            });
        }
    }

    public static boolean isFavourite(Movie movie) {
        boolean isFav = false;
        if (MainActivity.sFavouriteMovies != null) {
            for (Movie favMovie : MainActivity.sFavouriteMovies) {
                if (movie.getId().equals(favMovie.getId())) {
                    isFav = true;
                    break;
                }
            }
        }
        return isFav;
    }
}
