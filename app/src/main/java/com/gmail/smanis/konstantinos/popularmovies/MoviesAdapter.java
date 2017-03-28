package com.gmail.smanis.konstantinos.popularmovies;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.gmail.smanis.konstantinos.popularmovies.provider.MoviesContract.MovieEntry;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

enum SortMode {
    Popular,
    TopRated,
    Favorites;

    private static final SortMode[] sCachedValues = SortMode.values();
    static SortMode fromInt(int i) {
        return sCachedValues[i];
    }
}

class MoviesAdapter extends RecyclerView.Adapter<MoviesAdapter.MoviesViewHolder> {

    interface OnClickHandler {
        void onClick(Movie movie);
    }
    interface OnPageFetchHandler {
        void onPageFetch(SortMode sortMode, int page);
    }

    static class Movie {

        int ID;
        String originalTitle;
        String title;
        String poster;
        String releaseDate;
        double rating;
        String synopsis;

        static Movie fromCursor(Cursor cur) {
            if (cur == null) {
                return null;
            }

            Movie ret = new Movie();
            ret.ID = cur.getInt(cur.getColumnIndex(MovieEntry.COLUMN_ID));
            ret.originalTitle = cur.getString(cur.getColumnIndex(MovieEntry.COLUMN_ORIGINAL_TITLE));
            ret.title = cur.getString(cur.getColumnIndex(MovieEntry.COLUMN_TITLE));
            ret.poster = cur.getString(cur.getColumnIndex(MovieEntry.COLUMN_POSTER));
            ret.releaseDate = cur.getString(cur.getColumnIndex(MovieEntry.COLUMN_RELEASE_DATE));
            ret.rating = cur.getDouble(cur.getColumnIndex(MovieEntry.COLUMN_RATING));
            ret.synopsis = cur.getString(cur.getColumnIndex(MovieEntry.COLUMN_SYNOPSIS));
            return ret;
        }
        static Movie fromJSONObject(JSONObject jsonObject) {
            if (jsonObject == null) {
                return null;
            }

            Movie ret = new Movie();
            ret.ID = jsonObject.optInt("id");
            ret.originalTitle = jsonObject.optString("original_title");
            ret.title = jsonObject.optString("title");
            ret.poster = jsonObject.optString("poster_path");
            ret.releaseDate = jsonObject.optString("release_date");
            ret.rating = jsonObject.optDouble("vote_average");
            ret.synopsis = jsonObject.optString("overview");
            return ret;
        }
    }
    static class MoviesViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private final Context mContext;
        private final ImageView mImageView;
        private final OnClickHandler mOnClickHandler;
        private Movie mMovie;

        MoviesViewHolder(View itemView, OnClickHandler onClickHandler) {
            super(itemView);
            mContext = itemView.getContext();
            mImageView = (ImageView) itemView.findViewById(R.id.imageview_movie);
            mOnClickHandler = onClickHandler;
        }

        @Override
        public void onClick(View v) {
            if (mOnClickHandler != null) {
                mOnClickHandler.onClick(mMovie);
            }
        }

        void bind(Movie movie) {
            mMovie = movie;
            if (movie == null || TextUtils.isEmpty(movie.poster)) {
                mImageView.setImageResource(R.drawable.ic_broken_image_white_48dp);
                mImageView.setOnClickListener(this);
                mImageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                return;
            }

            mImageView.setOnClickListener(null);
            Picasso.with(mContext)
                    .load(NetworkUtils.buildPosterUri(movie.poster, ImageQuality.Default))
                    .error(R.drawable.ic_broken_image_white_48dp)
                    .into(mImageView, new Callback() {
                        @Override
                        public void onSuccess() {
                            mImageView.setOnClickListener(MoviesViewHolder.this);
                            mImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                        }
                        @Override
                        public void onError() {
                            mImageView.setOnClickListener(MoviesViewHolder.this);
                            mImageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                        }
                    });
        }
        void unbind() {
            mImageView.setImageDrawable(null);
            mImageView.setOnClickListener(null);
        }
    }

    private static final int PAGE_SIZE = 20;
    private final OnClickHandler mOnClickHandler;
    private final OnPageFetchHandler mOnPageFetchHandler;
    private SortMode mSortMode;
    private SparseArray<Movie> mMovies;
    private int mItemCount;
    private int mPendingPage;

    MoviesAdapter(OnClickHandler onClickHandler, OnPageFetchHandler onPageFetchHandler) {
        mOnClickHandler = onClickHandler;
        mOnPageFetchHandler = onPageFetchHandler;
        mMovies = new SparseArray<>();
    }

    void addJSONPage(JSONObject jsonPage) {
        if (jsonPage == null || (mSortMode != SortMode.Popular && mSortMode != SortMode.TopRated)) {
            return;
        }

        int page;
        JSONArray results;
        try {
            page = jsonPage.getInt("page");
            results = jsonPage.getJSONArray("results");
            mItemCount = jsonPage.getInt("total_results");
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        int pageOffset = (page - 1) * PAGE_SIZE;
        for (int i = 0; i < results.length(); ++i) {
            mMovies.put(pageOffset + i, Movie.fromJSONObject(results.optJSONObject(i)));
        }
        notifyItemRangeChanged(pageOffset, PAGE_SIZE);
    }
    void setMovies(Cursor cur) {
        if (cur == null || mSortMode != SortMode.Favorites) {
            return;
        }

        mMovies.clear();
        while (cur.moveToNext()) {
            mMovies.put(cur.getPosition(), Movie.fromCursor(cur));
        }
        mItemCount = cur.getCount();
        notifyDataSetChanged();
    }
    void setMode(SortMode sortMode) {
        mSortMode = sortMode;
        mMovies.clear();
        mItemCount = 0;
        mPendingPage = 0;
        notifyDataSetChanged();
    }

    private void fetchPage(int page) {
        if (mOnPageFetchHandler != null && mPendingPage != page) {
            mOnPageFetchHandler.onPageFetch(mSortMode, mPendingPage = page);
        }
    }

    @Override
    public MoviesViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new MoviesViewHolder(LayoutInflater.from(parent.getContext()).inflate(
                R.layout.item_movie, parent, false), mOnClickHandler);
    }
    @Override
    public void onBindViewHolder(MoviesViewHolder holder, int position) {
        Movie movie = mMovies.get(position);
        if (movie != null) {
            holder.bind(movie);
        } else {
            holder.unbind();
            fetchPage(position / PAGE_SIZE + 1);
        }
    }
    @Override
    public int getItemCount() {
        return mItemCount;
    }
}
