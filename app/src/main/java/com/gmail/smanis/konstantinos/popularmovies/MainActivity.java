package com.gmail.smanis.konstantinos.popularmovies;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;

import org.json.JSONObject;

public class MainActivity extends AppCompatActivity
        implements MoviesAdapter.ConnectivityHandler, MoviesAdapter.OnClickHandler,
        LoaderManager.LoaderCallbacks<Void> {

    private static class ConfigLoader extends AsyncTaskLoader<Void> {

        ConfigLoader(Context context) {
            super(context);
        }

        @Override
        protected void onStartLoading() {
            if (!NetworkUtils.hasConfiguration()) {
                forceLoad();
            }
        }
        @Override
        public Void loadInBackground() {
            NetworkUtils.fetchConfiguration();
            return null;
        }
    }

    public static final String EXTRA_MOVIE_ID = BuildConfig.APPLICATION_ID + ".MOVIE_ID";
    public static final String EXTRA_MOVIE_ORIGINAL_TITLE = BuildConfig.APPLICATION_ID + ".MOVIE_ORIGINAL_TITLE";
    public static final String EXTRA_MOVIE_TITLE = BuildConfig.APPLICATION_ID + ".MOVIE_TITLE";
    public static final String EXTRA_MOVIE_POSTER = BuildConfig.APPLICATION_ID + ".MOVIE_POSTER";
    public static final String EXTRA_MOVIE_RELEASE_DATE = BuildConfig.APPLICATION_ID + ".MOVIE_RELEASE_DATE";
    public static final String EXTRA_MOVIE_RATING = BuildConfig.APPLICATION_ID + ".MOVIE_RATING";
    public static final String EXTRA_MOVIE_SYNOPSIS = BuildConfig.APPLICATION_ID + ".MOVIE_SYNOPSIS";
    private static final int CONFIG_LOADER_ID = 0;
    private RecyclerView mRvMovies;
    private LinearLayout mLlError;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mRvMovies = (RecyclerView) findViewById(R.id.rv_movies);
        mLlError = (LinearLayout) findViewById(R.id.ll_error);

        init();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!NetworkUtils.isOnline(this)) {
            return false;
        }

        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.sort_popular:
            if (!item.isChecked()) {
                item.setChecked(true);
                mRvMovies.setAdapter(new MoviesAdapter(SortBy.Popularity, this, this));
            }
            return true;
        case R.id.sort_rating:
            if (!item.isChecked()) {
                item.setChecked(true);
                mRvMovies.setAdapter(new MoviesAdapter(SortBy.Rating, this, this));
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onConnectionFail() {
        init();
    }

    @Override
    public void onClick(JSONObject jsonMovie) {
        if (jsonMovie == null) {
            return;
        }

        Intent intent = new Intent(this, DetailsActivity.class);
        intent.putExtra(EXTRA_MOVIE_ID, jsonMovie.optInt("id", -1));
        intent.putExtra(EXTRA_MOVIE_ORIGINAL_TITLE, jsonMovie.optString("original_title"));
        intent.putExtra(EXTRA_MOVIE_TITLE, jsonMovie.optString("title"));
        intent.putExtra(EXTRA_MOVIE_POSTER, jsonMovie.optString("poster_path"));
        intent.putExtra(EXTRA_MOVIE_RELEASE_DATE, jsonMovie.optString("release_date"));
        intent.putExtra(EXTRA_MOVIE_RATING, jsonMovie.optDouble("vote_average", 0.f));
        intent.putExtra(EXTRA_MOVIE_SYNOPSIS, jsonMovie.optString("overview"));
        startActivity(intent);
    }

    @Override
    public Loader<Void> onCreateLoader(int id, Bundle args) {
        switch (id) {
        case CONFIG_LOADER_ID:
            return new ConfigLoader(this);
        default:
            throw new IllegalArgumentException("Unknown loader id: " + id);
        }
    }
    @Override
    public void onLoadFinished(Loader<Void> loader, Void data) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        NetworkUtils.updateConfiguration(displayMetrics.widthPixels / 2);
    }
    @Override
    public void onLoaderReset(Loader<Void> loader) {
    }

    public void reload(View view) {
        init();
    }

    private void init() {
        if (NetworkUtils.isOnline(this)) {
            getSupportLoaderManager().initLoader(CONFIG_LOADER_ID, null, this);
            mLlError.setVisibility(View.INVISIBLE);
            mRvMovies.setVisibility(View.VISIBLE);
            mRvMovies.setAdapter(new MoviesAdapter(SortBy.Popularity, this, this));
        } else {
            mRvMovies.setVisibility(View.INVISIBLE);
            mLlError.setVisibility(View.VISIBLE);
        }
        invalidateOptionsMenu();
    }
}
