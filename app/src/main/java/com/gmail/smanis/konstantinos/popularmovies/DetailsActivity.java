package com.gmail.smanis.konstantinos.popularmovies;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

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

public class DetailsActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<JSONObject> {

    private static class VideoLoader extends AsyncTaskLoader<JSONObject> {

        final private int mMovieID;
        private JSONObject mData;

        VideoLoader(Context context, int movieID) {
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

    private static final int VIDEOS_LOADER_ID = 0;
    private static final String BUNDLE_KEY_MOVIE_ID = "movieID";
    private View mHDivTrailers;
    private TextView mTvTrailersLabel;
    private RecyclerView mRvTrailers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        final TextView tvMovieTitle = (TextView) findViewById(R.id.tv_movie_title);
        final ImageView ivMoviePoster = (ImageView) findViewById(R.id.iv_movie_poster);
        final TextView tvMovieReleaseDate = (TextView) findViewById(R.id.tv_movie_release_date);
        final TextView tvMovieRating = (TextView) findViewById(R.id.tv_movie_rating);
        final TextView tvMovieSynopsis = (TextView) findViewById(R.id.tv_movie_synopsis);
        mHDivTrailers = findViewById(R.id.hdiv_trailers);
        mTvTrailersLabel = (TextView) findViewById(R.id.tv_trailers_label);
        mRvTrailers = (RecyclerView) findViewById(R.id.rv_trailers);
        mRvTrailers.setHasFixedSize(true);
        mRvTrailers.setNestedScrollingEnabled(false);

        Intent intent = getIntent();
        if (intent != null) {
            if (intent.hasExtra(MainActivity.MOVIE_ORIGINAL_TITLE)) {
                String originalTitle = intent.getStringExtra(MainActivity.MOVIE_ORIGINAL_TITLE);
                StringBuilder builder = new StringBuilder(originalTitle);
                if (intent.hasExtra(MainActivity.MOVIE_TITLE)) {
                    String title = intent.getStringExtra(MainActivity.MOVIE_TITLE);
                    if (!originalTitle.equals(title)) {
                        builder.append(String.format(" (%s)", title));
                    }
                }
                tvMovieTitle.setText(builder.toString());
            }
            if (intent.hasExtra(MainActivity.MOVIE_POSTER)) {
                final String posterPath = intent.getStringExtra(MainActivity.MOVIE_POSTER);
                Picasso.with(this)
                        .load(NetworkUtils.buildPosterUri(posterPath, ImageQuality.Default))
                        .error(R.drawable.ic_broken_image_white_48dp)
                        .into(ivMoviePoster);
                ivMoviePoster.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(DetailsActivity.this, PosterActivity.class);
                        intent.putExtra(MainActivity.MOVIE_POSTER, posterPath);
                        startActivity(intent);
                    }
                });
            } else {
                ivMoviePoster.setImageResource(R.drawable.ic_broken_image_white_48dp);
            }
            if (intent.hasExtra(MainActivity.MOVIE_RELEASE_DATE)) {
                String releaseDate = intent.getStringExtra(MainActivity.MOVIE_RELEASE_DATE);
                try {
                    Date date = new SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(releaseDate);
                    releaseDate = DateFormat.getDateInstance().format(date);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                tvMovieReleaseDate.setText(releaseDate);
            }
            if (intent.hasExtra(MainActivity.MOVIE_RATING)) {
                double rating = intent.getDoubleExtra(MainActivity.MOVIE_RATING, 0.f);
                tvMovieRating.setText(String.format(Locale.getDefault(), "%.1f/%d", rating, 10));
            }
            if (intent.hasExtra(MainActivity.MOVIE_SYNOPSIS)) {
                tvMovieSynopsis.setText(intent.getStringExtra(MainActivity.MOVIE_SYNOPSIS));
            }
            if (intent.hasExtra(MainActivity.MOVIE_ID)) {
                Bundle bundle = new Bundle();
                bundle.putInt(BUNDLE_KEY_MOVIE_ID, intent.getIntExtra(MainActivity.MOVIE_ID, -1));
                getSupportLoaderManager().initLoader(VIDEOS_LOADER_ID, bundle, this);
            }
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mRvTrailers.setAdapter(null);
    }

    @Override
    public Loader<JSONObject> onCreateLoader(int id, Bundle args) {
        switch (id) {
        case VIDEOS_LOADER_ID:
            return new VideoLoader(this, args.getInt(BUNDLE_KEY_MOVIE_ID));
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
            mHDivTrailers.setVisibility(View.VISIBLE);
            mTvTrailersLabel.setVisibility(View.VISIBLE);
            mRvTrailers.setVisibility(View.VISIBLE);
            mRvTrailers.setAdapter(new TrailersAdapter(trailers));
        }
    }
    @Override
    public void onLoaderReset(Loader<JSONObject> loader) {
    }
}
