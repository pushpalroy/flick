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
import com.pushpal.popularmoviesstage1.adapter.GridAutoFitLayoutManager;
import com.pushpal.popularmoviesstage1.adapter.MovieAdapter;
import com.pushpal.popularmoviesstage1.adapter.MovieClickListener;
import com.pushpal.popularmoviesstage1.database.MainViewModel;
import com.pushpal.popularmoviesstage1.model.Movie;
import com.pushpal.popularmoviesstage1.model.MovieResponse;
import com.pushpal.popularmoviesstage1.networking.ConnectivityReceiver;
import com.pushpal.popularmoviesstage1.networking.RESTClient;
import com.pushpal.popularmoviesstage1.networking.RESTClientInterface;
import com.pushpal.popularmoviesstage1.utilities.Constants;
import com.pushpal.popularmoviesstage1.utilities.MovieApplication;
import com.pushpal.popularmoviesstage1.utilities.MovieUtils;
import com.victor.loading.newton.NewtonCradleLoading;

import java.util.ArrayList;
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

    private static final String TAG = MainActivity.class.getSimpleName();
    public static Map<String, String> sLanguageMap;
    public static List<Movie> sFavouriteMovies;
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
    @BindView(R.id.toolbarExtended)
    TextView toolbarExt;
    @BindView(R.id.iv_popular_icon)
    ImageView popularIcon;
    @BindView(R.id.iv_top_rated_icon)
    ImageView topRatedIcon;
    @BindView(R.id.iv_favourite_icon)
    ImageView favouriteIcon;
    private MainViewModel mMainViewModel;
    private GridAutoFitLayoutManager mLayoutManager;
    private MovieAdapter mAdapter;
    private Snackbar mSnackBar;
    private int mCallPage, mCallPagePending, mAdapterPosition = 0;
    private String mSortCategory, mArrangementType, mResumeType = Constants.RESUME_NORMAL;
    private boolean mViewToggle = false;
    private List<Movie> mMovieList;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        setUpActionBar();
        resetData();
        setupRecyclerView();
        implementPagination();

        startLoader();
        MovieUtils.fetchLanguages();
        retrieveFavMovies();

        // Fetching page 1, top rated
        topRatedIcon.setColorFilter(getResources()
                .getColor(R.color.colorAccent));
    }

    private void setUpActionBar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbar.setNavigationIcon(getResources().getDrawable(R.drawable.ic_flick, null));
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString(Constants.SORT_TYPE, mSortCategory);
        outState.putInt(Constants.CALL_PAGE, mCallPage);
        outState.putInt(Constants.CALL_PAGE_PENDING, mCallPagePending);
        outState.putInt(Constants.ADAPTER_POSITION, mLayoutManager.findFirstCompletelyVisibleItemPosition());
        outState.putString(Constants.ARRANGEMENT_TYPE, mAdapter.getArrangementType());

        mMainViewModel.setMovies(mMovieList);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        mCallPage = savedInstanceState.getInt(Constants.CALL_PAGE);
        mCallPagePending = savedInstanceState.getInt(Constants.CALL_PAGE_PENDING);

        if (savedInstanceState.containsKey(Constants.SORT_TYPE) &&
                savedInstanceState.containsKey(Constants.ADAPTER_POSITION)) {
            mSortCategory = savedInstanceState.getString(Constants.SORT_TYPE);
            mAdapterPosition = savedInstanceState.getInt(Constants.ADAPTER_POSITION);
            mArrangementType = savedInstanceState.getString(Constants.ARRANGEMENT_TYPE);
            resetAndSetIconFilters(mSortCategory);
            mResumeType = Constants.RESUME_AFTER_ROTATION;
        } else {
            mSortCategory = Constants.CATEGORY_TOP_RATED;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mResumeType.equals(Constants.RESUME_NORMAL)) {
            if (!mSortCategory.equals(Constants.CATEGORY_FAVOURITE))
                fetchMovies(mCallPage);
            else {
                setFavourite();
                dismissLoader();
                fab.setVisibility(View.VISIBLE);
            }
        } else if (mResumeType.equals(Constants.RESUME_AFTER_ROTATION)) {
            if (mMovieList != null) {
                mMovieList.clear();
                mMovieList.addAll(mMainViewModel.getMovies());
            }

            toolbarExt.setText(mSortCategory);
            rearrangeRecyclerView(mArrangementType);

            dismissLoader();
            fab.setVisibility(View.VISIBLE);
            mResumeType = Constants.RESUME_NORMAL;
        }

        // Register connection status listener
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);

        ConnectivityReceiver connectivityReceiver = new ConnectivityReceiver();
        registerReceiver(connectivityReceiver, intentFilter);

        /* Register connection status listener */
        MovieApplication.getInstance().setConnectivityListener(this);
    }

    private void fetchMovies(final int page) {
        RESTClientInterface restClientInterface = RESTClient.getClient().create(RESTClientInterface.class);
        Call<MovieResponse> call = null;

        switch (mSortCategory) {
            case Constants.CATEGORY_TOP_RATED:
                toolbarExt.setText(getString(R.string.action_top_rated));
                call = restClientInterface.getTopRatedMovies(Constants.API_KEY, page);
                break;
            case Constants.CATEGORY_MOST_POPULAR:
                toolbarExt.setText(getString(R.string.action_most_popular));
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
                            mMovieList.addAll(response.body().getResults());
                            mAdapter.notifyItemInserted(mMovieList.size() - 1);
                            mAdapter.notifyDataSetChanged();
                            mRecyclerView.scheduleLayoutAnimation();
                            if (mAdapterPosition != 0) {
                                mRecyclerView.smoothScrollToPosition(mAdapterPosition);
                                mAdapterPosition = 0;
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
                    mCallPagePending = mCallPage;
                    mCallPage--;
                }
            });
        }
    }

    private void resetData() {
        mMainViewModel = ViewModelProviders.of(this).get(MainViewModel.class);

        if (mMovieList != null)
            mMovieList.clear();

        // By default sort order is set to top rated
        mSortCategory = Constants.CATEGORY_TOP_RATED;
        mCallPagePending = 0;
        mCallPage = 1;
    }

    private void implementPagination() {
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {

                int totalItemCount = mLayoutManager.getItemCount();

                // mCallPage: Has value 1 at first, increments by 1
                // dy > 0: Checks if scroll direction is downwards
                // ((totalItemCount / 20) == mCallPage)): Checks if page needs to be incremented,
                // here the API returns 20 items per page

                if ((dy > 0) && ((totalItemCount / 20) == mCallPage)) {
                    int visibleItemCount = mLayoutManager.getChildCount();
                    int pastVisibleItems = mLayoutManager.findFirstVisibleItemPosition();

                    // Checks whether reached near the end of recycler view, 10 items less than total items
                    if (pastVisibleItems + visibleItemCount >= (totalItemCount - 10)) {
                        mCallPage++;
                        fetchMovies(mCallPage);
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
            if (mCallPagePending != 0) {
                fetchMovies(mCallPagePending);
                mCallPage = mCallPagePending;
                mCallPagePending = 0;
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
        loader.setLoadingColor(R.color.colorAccent);
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
        mMovieList = new ArrayList<>();

        mAdapter = new MovieAdapter(mMovieList, Constants.ARRANGEMENT_COMPACT, this);
        mLayoutManager = new GridAutoFitLayoutManager(this, 300);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mAdapter);
    }

    private void rearrangeRecyclerView(String arrangementType) {

        if (!mResumeType.equals("rotated"))
            mAdapterPosition = mLayoutManager.findFirstVisibleItemPosition();

        mAdapter = new MovieAdapter(mMovieList, arrangementType, this);

        if (arrangementType.equals(Constants.ARRANGEMENT_COZY))
            mLayoutManager = new GridAutoFitLayoutManager(this, 500);
        else mLayoutManager = new GridAutoFitLayoutManager(this, 300);

        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();
        mRecyclerView.scheduleLayoutAnimation();

        if (mAdapterPosition != 0) {
            mRecyclerView.scrollToPosition(mAdapterPosition);
            mAdapterPosition = 0;
        }
    }

    private void showTapTargetView(View targetView) {
        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        if (!sharedPref.getBoolean(getString(R.string.isTapToTargetShown), false)) {
            // Show tap target view for FAB
            new MaterialTapTargetPrompt.Builder(MainActivity.this)
                    .setTarget(targetView)
                    .setPrimaryText("Sort mMovieList")
                    .setSecondaryText("Tap the sort icon to select the order of mMovieList.")
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
        if (!(mSortCategory.equals(Constants.CATEGORY_MOST_POPULAR))) {
            resetData();
            mSortCategory = Constants.CATEGORY_MOST_POPULAR;
            resetAndSetIconFilters(mSortCategory);
            fetchMovies(mCallPage);
        }
        onClickOverlay();
    }

    @OnClick(R.id.ll_top_rated)
    public void onTopRatedSelected() {
        if (!(mSortCategory.equals(Constants.CATEGORY_TOP_RATED))) {
            resetData();
            mSortCategory = Constants.CATEGORY_TOP_RATED;
            resetAndSetIconFilters(mSortCategory);
            fetchMovies(mCallPage);
        }
        onClickOverlay();
    }

    @OnClick(R.id.ll_favourite)
    public void onFavouriteSelected() {
        if (!(mSortCategory.equals(Constants.CATEGORY_FAVOURITE))) {
            resetData();
            mSortCategory = Constants.CATEGORY_FAVOURITE;
            resetAndSetIconFilters(mSortCategory);
            setFavourite();
        }
        onClickOverlay();
    }

    private void setFavourite() {
        toolbarExt.setText(getString(R.string.action_favourite));
        setFavList(sFavouriteMovies);
        mAdapter.notifyDataSetChanged();
        mRecyclerView.scheduleLayoutAnimation();
    }

    private void resetAndSetIconFilters(String sortCategory) {
        popularIcon.setColorFilter(getResources().getColor(R.color.colorIconGrey));
        topRatedIcon.setColorFilter(getResources().getColor(R.color.colorIconGrey));
        favouriteIcon.setColorFilter(getResources().getColor(R.color.colorIconGrey));

        switch (sortCategory) {
            case Constants.CATEGORY_FAVOURITE:
                favouriteIcon.setColorFilter(getResources().getColor(R.color.colorAccent));
                break;
            case Constants.CATEGORY_MOST_POPULAR:
                popularIcon.setColorFilter(getResources().getColor(R.color.colorAccent));
                break;
            case Constants.CATEGORY_TOP_RATED:
                topRatedIcon.setColorFilter(getResources().getColor(R.color.colorAccent));
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
            mViewToggle = !mViewToggle;
            if (mViewToggle) {
                rearrangeRecyclerView(Constants.ARRANGEMENT_COZY);
                item.setIcon(ContextCompat.getDrawable(this, R.drawable.view_small_icon));
            } else {
                rearrangeRecyclerView(Constants.ARRANGEMENT_COMPACT);
                item.setIcon(ContextCompat.getDrawable(this, R.drawable.view_large_icon));
            }
        }
        return true;
    }

    @Override
    public void onMovieClick(int pos, Movie movie, ImageView sharedImageView) {
        Intent intent = new Intent(this, DetailsActivity.class);
        intent.putExtra(Constants.EXTRA_MOVIE_ITEM, movie);
        intent.putExtra(Constants.EXTRA_MOVIE_IMAGE_TRANSITION_NAME, ViewCompat.getTransitionName(sharedImageView));

        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                this,
                sharedImageView,
                ViewCompat.getTransitionName(sharedImageView));

        mMainViewModel.setMovies(mMovieList);
        mResumeType = "intent";
        startActivity(intent, options.toBundle());
    }

    private void retrieveFavMovies() {
        if (sFavouriteMovies == null)
            sFavouriteMovies = new ArrayList<>();

        mMainViewModel.getFavMovies().observe(this, new Observer<List<Movie>>() {
            @Override
            public void onChanged(@Nullable List<Movie> movies) {
                if (movies != null) {
                    Log.e(TAG, "Movie Live Data changed in View Model.");

                    sFavouriteMovies.clear();
                    sFavouriteMovies.addAll(movies);

                    if (mSortCategory.equals(Constants.CATEGORY_FAVOURITE))
                        setFavList(sFavouriteMovies);

                    mAdapter.notifyDataSetChanged();
                    mRecyclerView.scheduleLayoutAnimation();
                }
            }
        });
    }

    private void setFavList(List<Movie> movies) {
        this.mMovieList.clear();
        this.mMovieList.addAll(movies);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e(TAG, "onDestroy: .");
    }
}