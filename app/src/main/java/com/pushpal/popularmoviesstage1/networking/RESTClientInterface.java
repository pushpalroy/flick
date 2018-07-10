package com.pushpal.popularmoviesstage1.networking;

import com.pushpal.popularmoviesstage1.model.MovieLang;
import com.pushpal.popularmoviesstage1.model.MovieResponse;
import com.pushpal.popularmoviesstage1.utilities.Constants;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface RESTClientInterface {
    @GET(Constants.POPULAR_MOVIES_URL)
    Call<MovieResponse> getPopularMovies(@Query("api_key") String apiKey, @Query("page") int page);

    @GET(Constants.TOP_RATED_MOVIES_URL)
    Call<MovieResponse> getTopRatedMovies(@Query("api_key") String apiKey, @Query("page") int page);

    @GET(Constants.UPCOMING_MOVIES_URL)
    Call<MovieResponse> getUpcomingMovies(@Query("api_key") String apiKey);

    @GET(Constants.NOW_PLAYING_MOVIES_URL)
    Call<MovieResponse> getNowPlayingMovies(@Query("api_key") String apiKey, @Query("page") int page);

    @GET(Constants.LATEST_MOVIES_URL)
    Call<MovieResponse> getLatestMovies(@Query("api_key") String apiKey);

    @GET(Constants.MOVIE_DETAILS_URL)
    Call<MovieResponse> getMovieDetails(@Path("id") int id, @Query("api_key") String apiKey);

    @GET(Constants.LANGUAGES)
    Call<List<MovieLang>> getLanguages(@Query("api_key") String apiKey);
}