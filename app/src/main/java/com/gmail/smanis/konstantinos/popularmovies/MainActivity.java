package com.gmail.smanis.konstantinos.popularmovies;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;

import com.gmail.smanis.konstantinos.popularmovies.provider.MoviesContract.MovieEntry;

import org.json.JSONObject;

import java.io.IOException;

import static com.gmail.smanis.konstantinos.popularmovies.SortBy.Favorites;
import static com.gmail.smanis.konstantinos.popularmovies.SortBy.Rating;

public class MainActivity extends AppCompatActivity
        implements MoviesAdapter.OnClickHandler, MoviesAdapter.OnPageFetchHandler {

    private static class PageFetchLoader extends AsyncTaskLoader<JSONObject> {

        private final SortBy mSortMode;
        private final int mPage;
        private JSONObject mData;

        PageFetchLoader(Context context, SortBy sortMode, int page) {
            super(context);
            mSortMode = (sortMode != null ? sortMode : SortBy.Popularity);
            mPage = (page >= 1 && page <= 1000 ? page : 1);
        }

        @Override
        protected void onStartLoading() {
            if (mData != null) {
                super.deliverResult(mData);
            } else {
                forceLoad();
            }
        }
        @Override
        public JSONObject loadInBackground() {
            try {
                return NetworkUtils.fetchMovies(mSortMode, mPage);
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }
        @Override
        public void deliverResult(JSONObject data) {
            mData = data;
            super.deliverResult(data);
        }
    }
    private class PageFetchLoaderCallbacks implements LoaderManager.LoaderCallbacks<JSONObject> {

        @Override
        public Loader<JSONObject> onCreateLoader(int id, Bundle args) {
            switch (id) {
            case PAGE_FETCH_LOADER_ID:
                return new PageFetchLoader(
                        MainActivity.this,
                        SortBy.fromInt(args.getInt(BUNDLE_KEY_SORT_MODE)),
                        args.getInt(BUNDLE_KEY_PAGE)
                );
            default:
                throw new IllegalArgumentException("Unknown loader id: " + id);
            }
        }
        @Override
        public void onLoadFinished(Loader<JSONObject> loader, JSONObject data) {
            if (data != null) {
                mAdapter.addJSONPage(data);
            } else {
                init();
            }
        }
        @Override
        public void onLoaderReset(Loader<JSONObject> loader) {
        }
    }
    private class FavoriteLoaderCallbacks implements LoaderManager.LoaderCallbacks<Cursor> {

        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            switch (id) {
            case FAVORITE_LOADER_ID:
                return new CursorLoader(
                        MainActivity.this,
                        MovieEntry.CONTENT_URI,
                        null,
                        null,
                        null,
                        null
                );
            default:
                throw new IllegalArgumentException("Unknown loader id: " + id);
            }
        }
        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            mAdapter.setMovies(data);
        }
        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
        }
    }

    public static final String EXTRA_MOVIE_ID = BuildConfig.APPLICATION_ID + ".MOVIE_ID";
    public static final String EXTRA_MOVIE_ORIGINAL_TITLE = BuildConfig.APPLICATION_ID + ".MOVIE_ORIGINAL_TITLE";
    public static final String EXTRA_MOVIE_TITLE = BuildConfig.APPLICATION_ID + ".MOVIE_TITLE";
    public static final String EXTRA_MOVIE_POSTER = BuildConfig.APPLICATION_ID + ".MOVIE_POSTER";
    public static final String EXTRA_MOVIE_RELEASE_DATE = BuildConfig.APPLICATION_ID + ".MOVIE_RELEASE_DATE";
    public static final String EXTRA_MOVIE_RATING = BuildConfig.APPLICATION_ID + ".MOVIE_RATING";
    public static final String EXTRA_MOVIE_SYNOPSIS = BuildConfig.APPLICATION_ID + ".MOVIE_SYNOPSIS";
    private static final int PAGE_FETCH_LOADER_ID = 0;
    private static final int FAVORITE_LOADER_ID = 1;
    private static final String BUNDLE_KEY_SORT_MODE = "sortMode";
    private static final String BUNDLE_KEY_PAGE = "page";
    private RecyclerView mRecyclerView;
    private MoviesAdapter mAdapter;
    private LinearLayout mLinearLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerview_main);
        mRecyclerView.post(new Runnable() {
            @Override
            public void run() {
                RecyclerView.LayoutManager lm = mRecyclerView.getLayoutManager();
                if (lm instanceof LinearLayoutManager) {
                    LinearLayoutManager llm = (LinearLayoutManager) lm;
                    switch (llm.getOrientation()) {
                    case LinearLayoutManager.HORIZONTAL:
                        NetworkUtils.adjustPosterSize(mRecyclerView.getHeight() * 2 / 3);
                        break;
                    case LinearLayoutManager.VERTICAL:
                        if (llm instanceof GridLayoutManager) {
                            GridLayoutManager glm = (GridLayoutManager) llm;
                            NetworkUtils.adjustPosterSize(mRecyclerView.getWidth() / glm.getSpanCount());
                        }
                        break;
                    }
                }
            }
        });
        mRecyclerView.setAdapter(mAdapter = new MoviesAdapter(this, this));
        mLinearLayout = (LinearLayout) findViewById(R.id.linearlayout_main);

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
            sortBy = Rating;
            break;
        case R.id.sort_favorites:
            sortBy = Favorites;
            break;
        }

        if (sortBy != getSortPreference()) {
            setSortPreference(sortBy);
            item.setChecked(true);
            load();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(MoviesAdapter.Movie movie) {
        Intent intent = new Intent(this, DetailsActivity.class);
        intent.putExtra(EXTRA_MOVIE_ID, movie.ID);
        intent.putExtra(EXTRA_MOVIE_ORIGINAL_TITLE, movie.originalTitle);
        intent.putExtra(EXTRA_MOVIE_TITLE, movie.title);
        intent.putExtra(EXTRA_MOVIE_POSTER, movie.poster);
        intent.putExtra(EXTRA_MOVIE_RELEASE_DATE, movie.releaseDate);
        intent.putExtra(EXTRA_MOVIE_RATING, movie.rating);
        intent.putExtra(EXTRA_MOVIE_SYNOPSIS, movie.synopsis);
        startActivity(intent);
    }
    @Override
    public void onPageFetch(SortBy sortMode, int page) {
        Bundle bundle = new Bundle();
        bundle.putInt(BUNDLE_KEY_SORT_MODE, sortMode.ordinal());
        bundle.putInt(BUNDLE_KEY_PAGE, page);
        getSupportLoaderManager().restartLoader(PAGE_FETCH_LOADER_ID, bundle, new PageFetchLoaderCallbacks());
    }

    public void reload(View view) {
        init();
    }

    private void init() {
        if (NetworkUtils.isOnline(this)) {
            mLinearLayout.setVisibility(View.INVISIBLE);
            mRecyclerView.setVisibility(View.VISIBLE);
            load();
        } else {
            mRecyclerView.setVisibility(View.INVISIBLE);
            mLinearLayout.setVisibility(View.VISIBLE);
        }
        invalidateOptionsMenu();
    }
    private void load() {
        SortBy mode = getSortPreference();
        mAdapter.setMode(mode);
        switch (mode) {
        case Popularity:
        case Rating:
            onPageFetch(mode, 1);
            break;
        case Favorites:
            getSupportLoaderManager().restartLoader(FAVORITE_LOADER_ID, null, new FavoriteLoaderCallbacks());
            break;
        }
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
