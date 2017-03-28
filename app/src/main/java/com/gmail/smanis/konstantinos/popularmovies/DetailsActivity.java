package com.gmail.smanis.konstantinos.popularmovies;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.gmail.smanis.konstantinos.popularmovies.provider.MoviesContract.MovieEntry;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DetailsActivity extends AppCompatActivity {

    private static class VideosLoader extends AsyncTaskLoader<JSONObject> {

        private final int mMovieID;
        private JSONObject mData;

        VideosLoader(Context context, int movieID) {
            super(context);
            mMovieID = movieID;
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
                return NetworkUtils.fetchVideos(mMovieID);
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
    private class VideosLoaderCallbacks implements LoaderManager.LoaderCallbacks<JSONObject> {

        @Override
        public Loader<JSONObject> onCreateLoader(int id, Bundle args) {
            switch (id) {
            case VIDEOS_LOADER_ID:
                return new VideosLoader(DetailsActivity.this, args.getInt(BUNDLE_KEY_MOVIE_ID));
            default:
                throw new IllegalArgumentException("Unknown loader id: " + id);
            }
        }
        @Override
        public void onLoadFinished(Loader<JSONObject> loader, JSONObject data) {
            if (data == null) {
                return;
            }

            JSONArray results = data.optJSONArray("results");
            if (results == null) {
                return;
            }

            List<TrailersAdapter.Trailer> trailers = new ArrayList<>(results.length());
            for (int i = 0; i < results.length(); ++i) {
                JSONObject result = results.optJSONObject(i);
                if (result == null) {
                    continue;
                }

                String ID = result.optString("key");
                String title = result.optString("name");
                if (!TextUtils.isEmpty(ID) && !TextUtils.isEmpty(title)
                        && result.optString("site").equals("YouTube")
                        && result.optString("type").equals("Trailer")) {
                    trailers.add(new TrailersAdapter.Trailer(ID, title));
                }
            }
            if (!trailers.isEmpty()) {
                mHorizontalDividerTrailers.setVisibility(View.VISIBLE);
                mTextViewTrailersLabel.setVisibility(View.VISIBLE);
                mRecyclerViewTrailers.setVisibility(View.VISIBLE);
                mRecyclerViewTrailers.setAdapter(new TrailersAdapter(trailers));
            }
        }
        @Override
        public void onLoaderReset(Loader<JSONObject> loader) {
        }
    }
    private static class ReviewsLoader extends AsyncTaskLoader<List<JSONObject>> {

        private final int mMovieID;
        private List<JSONObject> mData;

        ReviewsLoader(Context context, int movieID) {
            super(context);
            mMovieID = movieID;
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
        public List<JSONObject> loadInBackground() {
            List<JSONObject> ret = new ArrayList<>();
            for (int page = 1, totalPages = Integer.MAX_VALUE; page < totalPages; ++page) {
                JSONObject jsonPage;
                try {
                    jsonPage = NetworkUtils.fetchReviews(mMovieID, page);
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }

                ret.add(jsonPage);
                totalPages = jsonPage.optInt("total_pages");
            }
            return ret;
        }
        @Override
        public void deliverResult(List<JSONObject> data) {
            mData = data;
            super.deliverResult(data);
        }
    }
    private class ReviewsLoaderCallbacks implements LoaderManager.LoaderCallbacks<List<JSONObject>> {

        @Override
        public Loader<List<JSONObject>> onCreateLoader(int id, Bundle args) {
            switch (id) {
            case REVIEWS_LOADER_ID:
                return new ReviewsLoader(DetailsActivity.this, args.getInt(BUNDLE_KEY_MOVIE_ID));
            default:
                throw new IllegalArgumentException("Unknown loader id: " + id);
            }
        }
        @Override
        public void onLoadFinished(Loader<List<JSONObject>> loader, List<JSONObject> data) {
            if (data == null) {
                return;
            }

            List<ReviewsAdapter.Review> reviews = new ArrayList<>();
            for (int page = 0; page < data.size(); ++page) {
                JSONArray results = data.get(page).optJSONArray("results");
                if (results == null) {
                    continue;
                }

                for (int i = 0; i < results.length(); ++i) {
                    JSONObject result = results.optJSONObject(i);
                    if (result == null) {
                        continue;
                    }

                    String author = result.optString("author");
                    String content = result.optString("content");
                    if (!TextUtils.isEmpty(author) && !TextUtils.isEmpty(content)) {
                        reviews.add(new ReviewsAdapter.Review(author, content));
                    }
                }
            }
            if (!reviews.isEmpty()) {
                mHorizontalDividerReviews.setVisibility(View.VISIBLE);
                mTextViewReviewsLabel.setVisibility(View.VISIBLE);
                mRecyclerViewReviews.setVisibility(View.VISIBLE);
                mRecyclerViewReviews.setAdapter(new ReviewsAdapter(reviews));
            }
        }
        @Override
        public void onLoaderReset(Loader<List<JSONObject>> loader) {
        }
    }
    private class FavoriteLoaderCallbacks implements LoaderManager.LoaderCallbacks<Cursor> {

        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            switch (id) {
            case FAVORITE_LOADER_ID:
                return new CursorLoader(
                        DetailsActivity.this,
                        MovieEntry.CONTENT_URI,
                        null,
                        MovieEntry.COLUMN_ID + "=?",
                        new String[]{String.valueOf(args.getInt(BUNDLE_KEY_MOVIE_ID))},
                        null
                );
            default:
                throw new IllegalArgumentException("Unknown loader id: " + id);
            }
        }
        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            if (data == null) {
                return;
            }

            mFavorite = (data.getCount() != 0);
            mImageViewFavorite.setImageResource(mFavorite ? R.drawable.ic_star_white_48dp :
                    R.drawable.ic_star_border_white_48dp);
        }
        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
        }
    }

    private static final int VIDEOS_LOADER_ID = 0;
    private static final int REVIEWS_LOADER_ID = 1;
    private static final int FAVORITE_LOADER_ID = 2;
    private static final String BUNDLE_KEY_MOVIE_ID = "movieID";
    private ImageView mImageViewFavorite;
    private View mHorizontalDividerTrailers;
    private TextView mTextViewTrailersLabel;
    private RecyclerView mRecyclerViewTrailers;
    private View mHorizontalDividerReviews;
    private TextView mTextViewReviewsLabel;
    private RecyclerView mRecyclerViewReviews;
    private boolean mFavorite;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        final TextView textViewTitle = (TextView) findViewById(R.id.textview_details_title);
        final ImageView imageViewPoster = (ImageView) findViewById(R.id.imageview_details_poster);
        final TextView textViewReleaseDate = (TextView) findViewById(R.id.textview_details_releasedate);
        final TextView textViewRating = (TextView) findViewById(R.id.textview_details_rating);
        final TextView textViewSynopsis = (TextView) findViewById(R.id.textview_details_synopsis);
        mImageViewFavorite = (ImageView) findViewById(R.id.imageview_details_favorite);
        mImageViewFavorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = getIntent();
                if (intent == null) {
                    return;
                }

                if (mFavorite) {
                    getContentResolver().delete(
                            MovieEntry.CONTENT_URI,
                            MovieEntry.COLUMN_ID + "=?",
                            new String[]{String.valueOf(intent.getIntExtra(MainActivity.EXTRA_MOVIE_ID, -1))}
                    );
                } else {
                    ContentValues cv = new ContentValues();
                    cv.put(MovieEntry.COLUMN_ID, intent.getIntExtra(MainActivity.EXTRA_MOVIE_ID, -1));
                    cv.put(MovieEntry.COLUMN_ORIGINAL_TITLE, intent.getStringExtra(MainActivity.EXTRA_MOVIE_ORIGINAL_TITLE));
                    cv.put(MovieEntry.COLUMN_TITLE, intent.getStringExtra(MainActivity.EXTRA_MOVIE_TITLE));
                    cv.put(MovieEntry.COLUMN_POSTER, intent.getStringExtra(MainActivity.EXTRA_MOVIE_POSTER));
                    cv.put(MovieEntry.COLUMN_RELEASE_DATE, intent.getStringExtra(MainActivity.EXTRA_MOVIE_RELEASE_DATE));
                    cv.put(MovieEntry.COLUMN_RATING, intent.getDoubleExtra(MainActivity.EXTRA_MOVIE_RATING, 0.f));
                    cv.put(MovieEntry.COLUMN_SYNOPSIS, intent.getStringExtra(MainActivity.EXTRA_MOVIE_SYNOPSIS));
                    getContentResolver().insert(
                            MovieEntry.CONTENT_URI,
                            cv
                    );
                }
            }
        });
        mHorizontalDividerTrailers = findViewById(R.id.horizontaldivider_details_trailers);
        mTextViewTrailersLabel = (TextView) findViewById(R.id.textview_details_trailerslabel);
        mRecyclerViewTrailers = (RecyclerView) findViewById(R.id.recyclerview_details_trailers);
        mRecyclerViewTrailers.setHasFixedSize(true);
        mRecyclerViewTrailers.setNestedScrollingEnabled(false);
        mHorizontalDividerReviews = findViewById(R.id.horizontaldivider_details_reviews);
        mTextViewReviewsLabel = (TextView) findViewById(R.id.textview_details_reviewslabel);
        mRecyclerViewReviews = (RecyclerView) findViewById(R.id.recyclerview_details_reviews);
        mRecyclerViewReviews.setHasFixedSize(true);
        mRecyclerViewReviews.setNestedScrollingEnabled(false);

        Intent intent = getIntent();
        if (intent != null) {
            if (intent.hasExtra(MainActivity.EXTRA_MOVIE_ID)) {
                Bundle bundle = new Bundle();
                bundle.putInt(BUNDLE_KEY_MOVIE_ID, intent.getIntExtra(MainActivity.EXTRA_MOVIE_ID, -1));
                getSupportLoaderManager().initLoader(VIDEOS_LOADER_ID, bundle, new VideosLoaderCallbacks());
                getSupportLoaderManager().initLoader(REVIEWS_LOADER_ID, bundle, new ReviewsLoaderCallbacks());
                getSupportLoaderManager().initLoader(FAVORITE_LOADER_ID, bundle, new FavoriteLoaderCallbacks());
            }
            if (intent.hasExtra(MainActivity.EXTRA_MOVIE_ORIGINAL_TITLE)) {
                String originalTitle = intent.getStringExtra(MainActivity.EXTRA_MOVIE_ORIGINAL_TITLE);
                StringBuilder builder = new StringBuilder(originalTitle);
                if (intent.hasExtra(MainActivity.EXTRA_MOVIE_TITLE)) {
                    String title = intent.getStringExtra(MainActivity.EXTRA_MOVIE_TITLE);
                    if (!originalTitle.equals(title)) {
                        builder.append(String.format(" (%s)", title));
                    }
                }
                textViewTitle.setText(builder.toString());
            }
            if (intent.hasExtra(MainActivity.EXTRA_MOVIE_POSTER)) {
                final String posterPath = intent.getStringExtra(MainActivity.EXTRA_MOVIE_POSTER);
                Picasso.with(this)
                        .load(NetworkUtils.buildPosterUri(posterPath, ImageQuality.Default))
                        .error(R.drawable.ic_broken_image_white_48dp)
                        .into(imageViewPoster);
                imageViewPoster.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(DetailsActivity.this, PosterActivity.class);
                        intent.putExtra(MainActivity.EXTRA_MOVIE_POSTER, posterPath);
                        startActivity(intent);
                    }
                });
            } else {
                imageViewPoster.setImageResource(R.drawable.ic_broken_image_white_48dp);
            }
            if (intent.hasExtra(MainActivity.EXTRA_MOVIE_RELEASE_DATE)) {
                String releaseDate = intent.getStringExtra(MainActivity.EXTRA_MOVIE_RELEASE_DATE);
                try {
                    Date date = new SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(releaseDate);
                    releaseDate = DateFormat.getDateInstance().format(date);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                textViewReleaseDate.setText(releaseDate);
            }
            if (intent.hasExtra(MainActivity.EXTRA_MOVIE_RATING)) {
                double rating = intent.getDoubleExtra(MainActivity.EXTRA_MOVIE_RATING, 0.f);
                textViewRating.setText(String.format(Locale.getDefault(), "%.1f/%d", rating, 10));
            }
            if (intent.hasExtra(MainActivity.EXTRA_MOVIE_SYNOPSIS)) {
                textViewSynopsis.setText(intent.getStringExtra(MainActivity.EXTRA_MOVIE_SYNOPSIS));
            }
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mRecyclerViewTrailers.setAdapter(null);
    }
}
