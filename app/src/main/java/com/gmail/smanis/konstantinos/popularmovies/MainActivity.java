package com.gmail.smanis.konstantinos.popularmovies;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;

import org.json.JSONObject;

public class MainActivity extends AppCompatActivity
        implements MoviesAdapter.ConnectivityHandler, MoviesAdapter.OnClickHandler {

    private class ConfigFetchTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            NetworkUtils.fetchConfiguration();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            DisplayMetrics dm = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(dm);
            NetworkUtils.updateConfiguration(dm.widthPixels / 2);
        }
    }

    public static final String EXTRA_MOVIE_ID = BuildConfig.APPLICATION_ID + ".MOVIE_ID";
    public static final String EXTRA_MOVIE_ORIGINAL_TITLE = BuildConfig.APPLICATION_ID + ".MOVIE_ORIGINAL_TITLE";
    public static final String EXTRA_MOVIE_TITLE = BuildConfig.APPLICATION_ID + ".MOVIE_TITLE";
    public static final String EXTRA_MOVIE_POSTER = BuildConfig.APPLICATION_ID + ".MOVIE_POSTER";
    public static final String EXTRA_MOVIE_RELEASE_DATE = BuildConfig.APPLICATION_ID + ".MOVIE_RELEASE_DATE";
    public static final String EXTRA_MOVIE_RATING = BuildConfig.APPLICATION_ID + ".MOVIE_RATING";
    public static final String EXTRA_MOVIE_SYNOPSIS = BuildConfig.APPLICATION_ID + ".MOVIE_SYNOPSIS";
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
        if (!online()) {
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

    public void reload(View view) {
        init();
    }

    private void init() {
        if (online()) {
            new ConfigFetchTask().execute();
            mLlError.setVisibility(View.INVISIBLE);
            mRvMovies.setVisibility(View.VISIBLE);
            mRvMovies.setAdapter(new MoviesAdapter(SortBy.Popularity, this, this));
        } else {
            mRvMovies.setVisibility(View.INVISIBLE);
            mLlError.setVisibility(View.VISIBLE);
        }
        invalidateOptionsMenu();
    }
    private boolean online() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }
}
