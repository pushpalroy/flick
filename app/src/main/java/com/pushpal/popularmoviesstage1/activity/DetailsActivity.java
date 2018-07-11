package com.pushpal.popularmoviesstage1.activity;

import android.content.Context;
import android.database.sqlite.SQLiteConstraintException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.pushpal.popularmoviesstage1.R;
import com.pushpal.popularmoviesstage1.adapter.CastAdapter;
import com.pushpal.popularmoviesstage1.adapter.TrailerAdapter;
import com.pushpal.popularmoviesstage1.database.AppExecutors;
import com.pushpal.popularmoviesstage1.database.MovieDatabase;
import com.pushpal.popularmoviesstage1.model.Movie;
import com.pushpal.popularmoviesstage1.model.MovieCast;
import com.pushpal.popularmoviesstage1.model.MovieCreditResponse;
import com.pushpal.popularmoviesstage1.model.MovieTrailer;
import com.pushpal.popularmoviesstage1.model.MovieTrailerResponse;
import com.pushpal.popularmoviesstage1.networking.RESTClient;
import com.pushpal.popularmoviesstage1.networking.RESTClientInterface;
import com.pushpal.popularmoviesstage1.utilities.Constants;
import com.pushpal.popularmoviesstage1.utilities.DateUtil;
import com.sackcentury.shinebuttonlib.ShineButton;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Response;

public class DetailsActivity extends AppCompatActivity {

    private static final String TAG = DetailsActivity.class.getSimpleName();

    @BindView(R.id.iv_movie_poster)
    ImageView moviePoster;
    @BindView(R.id.tv_movie_title)
    TextView movieTitle;
    @BindView(R.id.tv_movie_release_date)
    TextView movieReleaseDate;
    @BindView(R.id.tv_movie_language)
    TextView movieLanguage;
    @BindView(R.id.tv_vote_average)
    TextView movieVoteAverage;
    @BindView(R.id.tv_vote_count)
    TextView movieVoteCount;
    @BindView(R.id.tv_overview)
    TextView movieOverview;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.btn_favourite)
    ShineButton likeButton;
    @BindView(R.id.collapsing_toolbar)
    CollapsingToolbarLayout collapsingToolbarLayout;
    @BindView(R.id.rv_cast)
    RecyclerView castRecyclerView;
    @BindView(R.id.rv_trailer)
    RecyclerView trailerRecyclerView;
    ActionBar actionBar;
    MovieDatabase mDb;
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        ButterKnife.bind(this);
        context = this;

        supportPostponeEnterTransition();
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }

        Bundle extras = getIntent().getExtras();
        Movie movie = null;
        if (extras != null)
            movie = extras.getParcelable(MainActivity.EXTRA_MOVIE_ITEM);

        mDb = MovieDatabase.getInstance(getApplicationContext());

        if (movie != null) {
            fetchCredits(movie.getId());
            fetchTrailers(movie.getId());
            movieTitle.setText(movie.getTitle());
            movieReleaseDate.setText(DateUtil.getFormattedDate(movie.getReleaseDate()));
            movieLanguage.setText(getLanguage(movie.getOriginalLanguage()));
            movieVoteAverage.setText(String.valueOf(movie.getVoteAverage()));
            String voteCount = String.valueOf(movie.getVoteCount()) + " " + getString(R.string.votes);
            movieVoteCount.setText(voteCount);
            movieOverview.setText(String.valueOf(movie.getOverview()));
            collapsingToolbarLayout.setTitle(movie.getTitle());

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                String imageTransitionName = extras.getString(MainActivity.EXTRA_MOVIE_IMAGE_TRANSITION_NAME);
                moviePoster.setTransitionName(imageTransitionName);
            }

            String imageURL = Constants.IMAGE_BASE_URL
                    + Constants.IMAGE_SIZE_342
                    + movie.getPosterPath();
            Picasso.with(this)
                    .load(imageURL)
                    .into(moviePoster, new Callback() {
                        @Override
                        public void onSuccess() {
                            supportStartPostponedEnterTransition();
                        }

                        @Override
                        public void onError() {
                            supportStartPostponedEnterTransition();
                        }
                    });

            try {
                URL url = new URL(imageURL);
                Bitmap bitmap = BitmapFactory.decodeStream(url.openConnection().getInputStream());
                Palette.from(bitmap).generate(new Palette.PaletteAsyncListener() {
                    @Override
                    public void onGenerated(@NonNull Palette palette) {
                        int mutedColor = palette.getMutedColor(R.attr.colorPrimary);
                        collapsingToolbarLayout.setContentScrimColor(mutedColor);
                    }
                });
            } catch (IOException e) {
                Log.e(TAG, e.getMessage());
            }

            if (isFavourite(movie)) {
                likeButton.setChecked(true);
            }

            final Movie finalMovie = movie;
            likeButton.setOnCheckStateChangeListener(new ShineButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(View view, final boolean checked) {
                    AppExecutors.getInstance().diskIO().execute(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                final String updateMessage;
                                if (checked) {
                                    MovieDatabase.getInstance(context)
                                            .movieDao()
                                            .insertMovie(finalMovie);
                                    updateMessage = "Added to favourites";
                                } else {
                                    MovieDatabase.getInstance(context)
                                            .movieDao()
                                            .deleteMovie(finalMovie);
                                    updateMessage = "Removed from favourites";
                                }

                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(context, updateMessage, Toast.LENGTH_SHORT).show();
                                    }
                                });
                            } catch (SQLiteConstraintException e) {
                                Log.e(TAG, e.getMessage());
                            }
                        }
                    });
                }
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private String getLanguage(String languageAbbr) {
        return MainActivity.languageMap.get(languageAbbr);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private boolean isFavourite(Movie movie) {
        boolean isFav = false;
        if (MainActivity.favouriteMovies != null) {
            for (Movie favMovie : MainActivity.favouriteMovies) {
                if (movie.getId().equals(favMovie.getId())) {
                    isFav = true;
                    break;
                }
            }
        }
        return isFav;
    }

    public void fetchCredits(int movieId) {
        RESTClientInterface restClientInterface = RESTClient.getClient().create(RESTClientInterface.class);
        Call<MovieCreditResponse> call = restClientInterface.getCredits(movieId, Constants.API_KEY);

        if (call != null) {
            call.enqueue(new retrofit2.Callback<MovieCreditResponse>() {
                @Override
                public void onResponse(@NonNull Call<MovieCreditResponse> call,
                                       @NonNull Response<MovieCreditResponse> response) {
                    int statusCode = response.code();

                    if (statusCode == 200) {
                        if (response.body() != null) {
                            MovieCreditResponse movieCreditResponse = response.body();
                            List<MovieCast> casts = movieCreditResponse != null ? movieCreditResponse.getCast() : null;

                            RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(DetailsActivity.this,
                                    LinearLayoutManager.HORIZONTAL,
                                    false);

                            castRecyclerView.setLayoutManager(layoutManager);
                            castRecyclerView.setHasFixedSize(true);
                            castRecyclerView.setAdapter(new CastAdapter(casts));
                        }
                    }
                }

                @Override
                public void onFailure(@NonNull Call<MovieCreditResponse> call, @NonNull Throwable throwable) {
                    // Log error here since request failed
                    Log.e(TAG, throwable.toString());
                }
            });
        }
    }

    public void fetchTrailers(int movieId) {
        RESTClientInterface restClientInterface = RESTClient.getClient().create(RESTClientInterface.class);
        Call<MovieTrailerResponse> call = restClientInterface.getTrailers(movieId, Constants.API_KEY);

        if (call != null) {
            call.enqueue(new retrofit2.Callback<MovieTrailerResponse>() {
                @Override
                public void onResponse(@NonNull Call<MovieTrailerResponse> call,
                                       @NonNull Response<MovieTrailerResponse> response) {
                    int statusCode = response.code();

                    if (statusCode == 200) {
                        if (response.body() != null) {
                            MovieTrailerResponse movieTrailerResponse = response.body();
                            List<MovieTrailer> trailers = movieTrailerResponse != null ? movieTrailerResponse.getTrailers() : null;

                            RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(DetailsActivity.this,
                                    LinearLayoutManager.HORIZONTAL,
                                    false);

                            trailerRecyclerView.setLayoutManager(layoutManager);
                            trailerRecyclerView.setHasFixedSize(true);
                            trailerRecyclerView.setAdapter(new TrailerAdapter(trailers));
                        }
                    }
                }

                @Override
                public void onFailure(@NonNull Call<MovieTrailerResponse> call, @NonNull Throwable throwable) {
                    // Log error here since request failed
                    Log.e(TAG, throwable.toString());
                }
            });
        }
    }
}