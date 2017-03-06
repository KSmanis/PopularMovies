package com.gmail.smanis.konstantinos.popularmovies;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;

import org.json.JSONObject;

public class MainActivity extends AppCompatActivity
        implements MoviesAdapter.ConnectivityHandler, MoviesAdapter.OnClickHandler {

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
        mRvMovies.post(new Runnable() {
            @Override
            public void run() {
                RecyclerView.LayoutManager lm = mRvMovies.getLayoutManager();
                if (lm instanceof LinearLayoutManager) {
                    LinearLayoutManager llm = (LinearLayoutManager) lm;
                    switch (llm.getOrientation()) {
                    case LinearLayoutManager.HORIZONTAL:
                        NetworkUtils.adjustPosterSize(mRvMovies.getHeight() * 2 / 3);
                        break;
                    case LinearLayoutManager.VERTICAL:
                        if (llm instanceof GridLayoutManager) {
                            GridLayoutManager glm = (GridLayoutManager) llm;
                            NetworkUtils.adjustPosterSize(mRvMovies.getWidth() / glm.getSpanCount());
                        }
                        break;
                    }
                }
            }
        });
        mLlError = (LinearLayout) findViewById(R.id.ll_error);

        init();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!NetworkUtils.isOnline(this)) {
            return false;
        }

        getMenuInflater().inflate(R.menu.menu_main, menu);
        menu.getItem(getSortPreference().ordinal()).setChecked(true);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        SortBy sortBy = null;
        switch (item.getItemId()) {
        case R.id.sort_popular:
            sortBy = SortBy.Popularity;
            break;
        case R.id.sort_rating:
            sortBy = SortBy.Rating;
            break;
        }

        if (sortBy != getSortPreference()) {
            setSortPreference(sortBy);
            item.setChecked(true);
            mRvMovies.setAdapter(new MoviesAdapter(sortBy, this, this));
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
        if (NetworkUtils.isOnline(this)) {
            mLlError.setVisibility(View.INVISIBLE);
            mRvMovies.setVisibility(View.VISIBLE);
            mRvMovies.setAdapter(new MoviesAdapter(getSortPreference(), this, this));
        } else {
            mRvMovies.setVisibility(View.INVISIBLE);
            mLlError.setVisibility(View.VISIBLE);
        }
        invalidateOptionsMenu();
    }
    private SortBy getSortPreference() {
        return SortBy.fromInt(PreferenceManager.getDefaultSharedPreferences(this).getInt(
                getString(R.string.pref_sort_key),
                SortBy.Popularity.ordinal()
        ));
    }
    private void setSortPreference(SortBy sortBy) {
        PreferenceManager.getDefaultSharedPreferences(this).edit().putInt(
                getString(R.string.pref_sort_key),
                sortBy.ordinal()
        ).apply();
    }
}
