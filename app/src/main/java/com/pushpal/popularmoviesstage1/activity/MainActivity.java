package com.pushpal.popularmoviesstage1.activity;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.konifar.fab_transformation.FabTransformation;
import com.pushpal.popularmoviesstage1.R;
import com.pushpal.popularmoviesstage1.adapter.MovieAdapter;
import com.pushpal.popularmoviesstage1.adapter.MovieClickListener;
import com.pushpal.popularmoviesstage1.database.MainViewModel;
import com.pushpal.popularmoviesstage1.model.Movie;
import com.pushpal.popularmoviesstage1.model.MovieLang;
import com.pushpal.popularmoviesstage1.model.MovieResponse;
import com.pushpal.popularmoviesstage1.networking.ConnectivityReceiver;
import com.pushpal.popularmoviesstage1.networking.RESTClient;
import com.pushpal.popularmoviesstage1.networking.RESTClientInterface;
import com.pushpal.popularmoviesstage1.utilities.Constants;
import com.pushpal.popularmoviesstage1.utilities.MovieApplication;
import com.victor.loading.newton.NewtonCradleLoading;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;
import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt;

public class MainActivity extends AppCompatActivity implements
        ConnectivityReceiver.ConnectivityReceiverListener,
        MovieClickListener {

    public static final String EXTRA_MOVIE_ITEM = "movie_image_url";
    public static final String EXTRA_MOVIE_IMAGE_TRANSITION_NAME = "movie_image_transition_name";
    public static final String SORT_TYPE = "sort_type";
    public static final String ADAPTER_POSITION = "adapter_position";
    public static final String CALL_PAGE = "call_page";
    public static final String CALL_PAGE_PENDING = "call_page_pending";

    private static final String TAG = MainActivity.class.getSimpleName();
    public static Map<String, String> languageMap;
    public static List<Movie> favouriteMovies;
    @BindView(R.id.poster_recycler_view)
    RecyclerView mRecyclerView;
    @BindView(R.id.coordinatorLayout)
    CoordinatorLayout mCoordinatorLayout;
    @BindView(R.id.loader)
    NewtonCradleLoading loader;
    @BindView(R.id.loader_container)
    LinearLayout loaderContainer;
    @BindView(R.id.fab)
    FloatingActionButton fab;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.sort_layout)
    CardView sortLayout;
    @BindView(R.id.overlay)
    View overlayView;
    MovieAdapter mAdapter;
    GridLayoutManager mLayoutManager;
    Snackbar mSnackBar;
    int callPage, callPagePending;
    String sortCategory;
    boolean isCozyView = false;
    @BindView(R.id.iv_popular_icon)
    ImageView popularIcon;
    @BindView(R.id.iv_top_rated_icon)
    ImageView topRatedIcon;
    @BindView(R.id.iv_favourite_icon)
    ImageView favouriteIcon;
    private List<Movie> movies;
    private int adapterPosition = 0;
    private String resumeStatus = "normal";

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // ButterKnife Binding
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);

        resetData();
        setupRecyclerView();
        implementPagination();

        startLoader();
        fetchLanguages();
        retrieveFavMovies();

        // Fetching page 1, top rated
        topRatedIcon.setColorFilter(getResources()
                .getColor(R.color.colorFilter));
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString(SORT_TYPE, sortCategory);
        outState.putInt(CALL_PAGE, callPage);
        outState.putInt(CALL_PAGE_PENDING, callPagePending);
        outState.putInt(ADAPTER_POSITION, mLayoutManager.findFirstCompletelyVisibleItemPosition());

        MainViewModel mainViewModel = ViewModelProviders.of(this).get(MainViewModel.class);
        mainViewModel.setMovies(movies);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        callPage = savedInstanceState.getInt(CALL_PAGE);
        callPagePending = savedInstanceState.getInt(CALL_PAGE_PENDING);

        if (savedInstanceState.containsKey(SORT_TYPE) && savedInstanceState.containsKey(ADAPTER_POSITION)) {
            sortCategory = savedInstanceState.getString(SORT_TYPE);
            adapterPosition = savedInstanceState.getInt(ADAPTER_POSITION);
            resetAndSetIconFilters(sortCategory);
            resumeStatus = "rotated";
        } else
            sortCategory = Constants.CATEGORY_TOP_RATED;
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (resumeStatus.equals("normal")) {
            if (!sortCategory.equals(Constants.CATEGORY_FAVOURITE))
                fetchMovies(callPage);
            else {
                setFavourite();
                dismissLoader();
                fab.setVisibility(View.VISIBLE);
            }
        } else if (resumeStatus.equals("rotated")) {
            MainViewModel mainViewModel = ViewModelProviders.of(this)
                    .get(MainViewModel.class);
            if (movies != null) {
                movies.clear();
                movies.addAll(mainViewModel.getMovies());
            }
            mAdapter.notifyItemInserted(movies.size() - 1);
            mAdapter.notifyDataSetChanged();
            mRecyclerView.scheduleLayoutAnimation();
            if (adapterPosition != 0) {
                mRecyclerView.smoothScrollToPosition(adapterPosition);
                adapterPosition = 0;
            }
            dismissLoader();
            fab.setVisibility(View.VISIBLE);
        }

        // Register connection status listener
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);

        ConnectivityReceiver connectivityReceiver = new ConnectivityReceiver();
        registerReceiver(connectivityReceiver, intentFilter);

        /* Register connection status listener */
        MovieApplication.getInstance().setConnectivityListener(this);
    }

    protected void fetchMovies(final int page) {
        RESTClientInterface restClientInterface = RESTClient.getClient().create(RESTClientInterface.class);
        Call<MovieResponse> call = null;

        switch (sortCategory) {
            case Constants.CATEGORY_TOP_RATED:
                setTitle(getString(R.string.action_top_rated));
                call = restClientInterface.getTopRatedMovies(Constants.API_KEY, page);
                break;
            case Constants.CATEGORY_MOST_POPULAR:
                setTitle(getString(R.string.action_most_popular));
                call = restClientInterface.getPopularMovies(Constants.API_KEY, page);
                break;
        }

        if (call != null) {
            call.enqueue(new Callback<MovieResponse>() {
                @Override
                public void onResponse(@NonNull Call<MovieResponse> call, @NonNull Response<MovieResponse> response) {
                    int statusCode = response.code();

                    if (statusCode == 200) {
                        if (response.body().getResults() != null) {
                            movies.addAll(response.body().getResults());
                            mAdapter.notifyItemInserted(movies.size() - 1);
                            mAdapter.notifyDataSetChanged();
                            mRecyclerView.scheduleLayoutAnimation();
                            if (adapterPosition != 0) {
                                mRecyclerView.smoothScrollToPosition(adapterPosition);
                                adapterPosition = 0;
                            }
                        }
                    }
                    dismissLoader();
                    fab.setVisibility(View.VISIBLE);
                    showTapTargetView(fab);
                }

                @Override
                public void onFailure(@NonNull Call<MovieResponse> call, @NonNull Throwable throwable) {
                    // Log error here since request failed
                    Log.e(TAG, throwable.toString());
                    dismissLoader();
                    callPagePending = callPage;
                    callPage--;
                }
            });
        }
    }

    private void resetData() {
        if (movies != null)
            movies.clear();

        // By default sort order is set to top rated
        sortCategory = Constants.CATEGORY_TOP_RATED;
        callPagePending = 0;
        callPage = 1;
    }

    protected void implementPagination() {
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {

                int totalItemCount = mLayoutManager.getItemCount();

                // callPage: Has value 1 at first, increments by 1
                // dy > 0: Checks if scroll direction is downwards
                // ((totalItemCount / 20) == callPage)): Checks if page needs to be incremented,
                // here the API returns 20 items per page

                if ((dy > 0) && ((totalItemCount / 20) == callPage)) {
                    int visibleItemCount = mLayoutManager.getChildCount();
                    int pastVisibleItems = mLayoutManager.findFirstVisibleItemPosition();

                    // Checks whether reached near the end of recycler view, 10 items less than total items
                    if (pastVisibleItems + visibleItemCount >= (totalItemCount - 10)) {
                        callPage++;
                        fetchMovies(callPage);
                        //Toast.makeText(MainActivity.this, String.valueOf(callPage), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
        if (isConnected) {
            if (mSnackBar != null)
                mSnackBar.dismiss();
            if (callPagePending != 0) {
                fetchMovies(callPagePending);
                callPage = callPagePending;
                callPagePending = 0;
            }
        } else {
            showSnack();
        }
    }

    // Showing the status in Snack bar
    private void showSnack() {
        mSnackBar = Snackbar
                .make(mCoordinatorLayout, "No internet connection!", Snackbar.LENGTH_INDEFINITE)
                .setAction("DISMISS", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mSnackBar.dismiss();
                    }
                });

        // Changing message text color
        mSnackBar.setActionTextColor(Color.GRAY);

        // Changing action button text color
        View snackBarView = mSnackBar.getView();
        TextView textView = snackBarView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(Color.LTGRAY);

        if (!mSnackBar.isShownOrQueued()) {
            mSnackBar.show();
        }
    }

    // Start loader
    private void startLoader() {
        loaderContainer.setVisibility(View.VISIBLE);
        loader.start();
    }

    // Dismiss loader
    private void dismissLoader() {
        if (loader.isStart()) {
            loaderContainer.setVisibility(View.INVISIBLE);
            loader.stop();
        }
    }

    private void setupRecyclerView() {
        final Context context = mRecyclerView.getContext();
        mLayoutManager = new GridLayoutManager(context, 3);
        mRecyclerView.setLayoutManager(mLayoutManager);
        movies = new ArrayList<>();
        mAdapter = new MovieAdapter(movies, Constants.ARRANGEMENT_COMPACT, this);
        mRecyclerView.setAdapter(mAdapter);
        loader.setLoadingColor(R.color.colorAccent);
    }

    private void rearrangeRecyclerView(String arrangementType, int spanCount) {
        final Context context = mRecyclerView.getContext();
        mLayoutManager = new GridLayoutManager(context, spanCount);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new MovieAdapter(movies, arrangementType, this);
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();
        mRecyclerView.scheduleLayoutAnimation();
    }

    private void showTapTargetView(View targetView) {
        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        if (!sharedPref.getBoolean(getString(R.string.isTapToTargetShown), false)) {
            // Show tap target view for FAB
            new MaterialTapTargetPrompt.Builder(MainActivity.this)
                    .setTarget(targetView)
                    .setPrimaryText("Sort movies")
                    .setSecondaryText("Tap the sort icon to select the order of movies.")
                    .setPromptStateChangeListener(new MaterialTapTargetPrompt.PromptStateChangeListener() {
                        @Override
                        public void onPromptStateChanged(@NonNull MaterialTapTargetPrompt prompt, int state) {
                            if (state == MaterialTapTargetPrompt.STATE_FOCAL_PRESSED) {
                                // User has pressed the prompt target
                            }
                        }
                    })
                    .show();

            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putBoolean(getString(R.string.isTapToTargetShown), true);
            editor.apply();
        }
    }

    @OnClick(R.id.fab)
    public void transformFabToLayout() {
        mRecyclerView.stopScroll();
        if (fab.getVisibility() == View.VISIBLE) {
            FabTransformation.with(fab).setOverlay(overlayView).transformTo(sortLayout);
        }
    }

    @Override
    public void onBackPressed() {
        if (fab.getVisibility() != View.VISIBLE) {
            FabTransformation.with(fab).setOverlay(overlayView).transformFrom(sortLayout);
            return;
        }
        super.onBackPressed();
    }

    @OnClick(R.id.overlay)
    void onClickOverlay() {
        if (fab.getVisibility() != View.VISIBLE) {
            try {
                FabTransformation.with(fab).setOverlay(overlayView).transformFrom(sortLayout);
            } catch (IllegalStateException e) {
                Log.e(TAG, e.getMessage());
            }
        }
    }

    @OnClick(R.id.ll_most_popular)
    public void onMostPopularSelected() {
        if (!(sortCategory.equals(Constants.CATEGORY_MOST_POPULAR))) {
            resetData();
            sortCategory = Constants.CATEGORY_MOST_POPULAR;
            resetAndSetIconFilters(sortCategory);
            fetchMovies(callPage);
        }
        onClickOverlay();
    }

    @OnClick(R.id.ll_top_rated)
    public void onTopRatedSelected() {
        if (!(sortCategory.equals(Constants.CATEGORY_TOP_RATED))) {
            resetData();
            sortCategory = Constants.CATEGORY_TOP_RATED;
            resetAndSetIconFilters(sortCategory);
            fetchMovies(callPage);
        }
        onClickOverlay();
    }

    @OnClick(R.id.ll_favourite)
    public void onFavouriteSelected() {
        if (!(sortCategory.equals(Constants.CATEGORY_FAVOURITE))) {
            resetData();
            sortCategory = Constants.CATEGORY_FAVOURITE;
            resetAndSetIconFilters(sortCategory);
            setFavourite();
        }
        onClickOverlay();
    }

    public void setFavourite() {
        setTitle(getString(R.string.action_favourite));

        setFavList(favouriteMovies);
        mAdapter.notifyDataSetChanged();
        mRecyclerView.scheduleLayoutAnimation();
    }

    public void resetAndSetIconFilters(String sortCategory) {
        popularIcon.setColorFilter(getResources().getColor(R.color.colorIconGrey));
        topRatedIcon.setColorFilter(getResources().getColor(R.color.colorIconGrey));
        favouriteIcon.setColorFilter(getResources().getColor(R.color.colorIconGrey));

        switch (sortCategory) {
            case Constants.CATEGORY_FAVOURITE:
                favouriteIcon.setColorFilter(getResources().getColor(R.color.colorFilter));
                break;
            case Constants.CATEGORY_MOST_POPULAR:
                popularIcon.setColorFilter(getResources().getColor(R.color.colorFilter));
                break;
            case Constants.CATEGORY_TOP_RATED:
                topRatedIcon.setColorFilter(getResources().getColor(R.color.colorFilter));
        }
    }

    public void fetchLanguages() {
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
                            languageMap = new HashMap<>();
                            for (MovieLang movieLang : finalMovieLanguages)
                                languageMap.put(movieLang.getAbbreviation(), movieLang.getEnglishName());
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.movies, menu);
        /* Return true so that the menu is displayed in the Toolbar */
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_view_type) {
            isCozyView = !isCozyView;
            if (isCozyView) {
                rearrangeRecyclerView(Constants.ARRANGEMENT_COZY, 2);
                item.setIcon(ContextCompat.getDrawable(this, R.drawable.view_small_icon));
            } else {
                rearrangeRecyclerView(Constants.ARRANGEMENT_COMPACT, 3);
                item.setIcon(ContextCompat.getDrawable(this, R.drawable.view_large_icon));
            }
        }
        return true;
    }

    @Override
    public void onMovieClick(int pos, Movie movie, ImageView sharedImageView) {
        Intent intent = new Intent(this, DetailsActivity.class);
        intent.putExtra(EXTRA_MOVIE_ITEM, movie);
        intent.putExtra(EXTRA_MOVIE_IMAGE_TRANSITION_NAME, ViewCompat.getTransitionName(sharedImageView));

        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                this,
                sharedImageView,
                ViewCompat.getTransitionName(sharedImageView));

        MainViewModel mainViewModel = ViewModelProviders.of(this).get(MainViewModel.class);
        mainViewModel.setMovies(movies);

        resumeStatus = "intent";
        startActivity(intent, options.toBundle());
    }

    private void retrieveFavMovies() {
        MainViewModel mainViewModel = ViewModelProviders.of(this)
                .get(MainViewModel.class);

        if (favouriteMovies == null)
            favouriteMovies = new ArrayList<>();

        mainViewModel.getFavMovies().observe(this, new Observer<List<Movie>>() {
            @Override
            public void onChanged(@Nullable List<Movie> movies) {
                if (movies != null) {
                    Log.d(TAG, "Movie Live Data changed in View Model.");

                    favouriteMovies.clear();
                    favouriteMovies.addAll(movies);

                    if (sortCategory.equals(Constants.CATEGORY_FAVOURITE))
                        setFavList(favouriteMovies);

                    mAdapter.notifyDataSetChanged();
                    mRecyclerView.scheduleLayoutAnimation();
                }
            }
        });
    }

    private void setFavList(List<Movie> movies) {
        this.movies.clear();
        this.movies.addAll(movies);
    }
}