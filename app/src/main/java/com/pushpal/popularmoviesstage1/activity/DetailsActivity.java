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
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.pushpal.popularmoviesstage1.R;
import com.pushpal.popularmoviesstage1.adapter.CastAdapter;
import com.pushpal.popularmoviesstage1.adapter.ReviewAdapter;
import com.pushpal.popularmoviesstage1.adapter.TrailerAdapter;
import com.pushpal.popularmoviesstage1.database.AppExecutors;
import com.pushpal.popularmoviesstage1.database.MovieDatabase;
import com.pushpal.popularmoviesstage1.model.Movie;
import com.pushpal.popularmoviesstage1.model.MovieCast;
import com.pushpal.popularmoviesstage1.model.MovieCreditResponse;
import com.pushpal.popularmoviesstage1.model.MovieReview;
import com.pushpal.popularmoviesstage1.model.MovieReviewResponse;
import com.pushpal.popularmoviesstage1.model.MovieTrailer;
import com.pushpal.popularmoviesstage1.model.MovieTrailerResponse;
import com.pushpal.popularmoviesstage1.networking.RESTClient;
import com.pushpal.popularmoviesstage1.networking.RESTClientInterface;
import com.pushpal.popularmoviesstage1.utilities.Constants;
import com.pushpal.popularmoviesstage1.utilities.DateUtil;
import com.pushpal.popularmoviesstage1.utilities.MovieUtils;
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
    @BindView(R.id.ll_trailers)
    LinearLayout trailerLayout;
    @BindView(R.id.rv_review)
    RecyclerView reviewRecyclerView;
    @BindView(R.id.ll_reviews)
    LinearLayout reviewLayout;
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        ButterKnife.bind(this);
        setUpActionBar();
        context = this;

        Bundle extras = getIntent().getExtras();
        Movie movie = null;
        if (extras != null)
            movie = extras.getParcelable(Constants.EXTRA_MOVIE_ITEM);

        if (movie != null) {
            fetchCredits(movie.getId());
            fetchTrailers(movie.getId());
            fetchReviews(movie.getId());

            movieTitle.setText(movie.getTitle());
            movieReleaseDate.setText(DateUtil.getFormattedDate(movie.getReleaseDate()));
            movieLanguage.setText(getLanguage(movie.getOriginalLanguage()));
            movieVoteAverage.setText(String.valueOf(movie.getVoteAverage()));
            String voteCount = String.valueOf(movie.getVoteCount()) + " " + getString(R.string.votes);
            movieVoteCount.setText(voteCount);
            movieOverview.setText(String.valueOf(movie.getOverview()));
            collapsingToolbarLayout.setTitle(movie.getTitle());

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                String imageTransitionName = extras.getString(Constants.EXTRA_MOVIE_IMAGE_TRANSITION_NAME);
                moviePoster.setTransitionName(imageTransitionName);
            }

            final String imageURL = Constants.IMAGE_BASE_URL
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

            if (MovieUtils.isFavourite(movie))
                likeButton.setChecked(true);

            generateImageColor(imageURL);
            setUpLikeButton(movie);
        }
    }

    private void setUpActionBar() {
        supportPostponeEnterTransition();
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }
    }

    private void generateImageColor(final String imageURL) {
        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
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
            }
        });
    }

    private String getLanguage(String languageAbbr) {
        return MainActivity.sLanguageMap.get(languageAbbr);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void setUpLikeButton(final Movie finalMovie) {
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

    private void fetchCredits(int movieId) {
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

    private void fetchTrailers(int movieId) {
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

                            if (trailers != null && trailers.size() > 0) {
                                RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(DetailsActivity.this,
                                        LinearLayoutManager.HORIZONTAL,
                                        false);

                                trailerRecyclerView.setLayoutManager(layoutManager);
                                trailerRecyclerView.setHasFixedSize(true);
                                trailerRecyclerView.setAdapter(new TrailerAdapter(trailers));
                            } else {
                                trailerLayout.setVisibility(View.GONE);
                            }
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

    private void fetchReviews(int movieId) {
        RESTClientInterface restClientInterface = RESTClient.getClient().create(RESTClientInterface.class);
        Call<MovieReviewResponse> call = restClientInterface.getReviews(movieId, Constants.API_KEY);

        if (call != null) {
            call.enqueue(new retrofit2.Callback<MovieReviewResponse>() {
                @Override
                public void onResponse(@NonNull Call<MovieReviewResponse> call,
                                       @NonNull Response<MovieReviewResponse> response) {
                    int statusCode = response.code();

                    if (statusCode == 200) {
                        if (response.body() != null) {
                            MovieReviewResponse movieReviewResponse = response.body();
                            List<MovieReview> reviews = movieReviewResponse != null ? movieReviewResponse.getReviews() : null;

                            if (reviews != null && reviews.size() > 0) {
                                RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(DetailsActivity.this,
                                        LinearLayoutManager.VERTICAL,
                                        false);

                                reviewRecyclerView.setLayoutManager(layoutManager);
                                reviewRecyclerView.setHasFixedSize(true);
                                reviewRecyclerView.setAdapter(new ReviewAdapter(reviews));
                            } else {
                                reviewLayout.setVisibility(View.GONE);
                            }
                        }
                    }
                }

                @Override
                public void onFailure(@NonNull Call<MovieReviewResponse> call, @NonNull Throwable throwable) {
                    // Log error here since request failed
                    Log.e(TAG, throwable.toString());
                }
            });
        }
    }
}