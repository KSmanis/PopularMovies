package com.gmail.smanis.konstantinos.popularmovies;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;

enum SortBy {
    Popularity,
    Rating
}

class MoviesAdapter extends RecyclerView.Adapter<MoviesAdapter.MoviesViewHolder> {

    interface ConnectivityHandler {
        void onConnectionFail();
    }
    interface OnClickHandler {
        void onClick(JSONObject movie);
    }

    private class PageFetchTask extends AsyncTask<Void, Void, JSONObject> {

        final private int mPage;
        private boolean mConnectionError;

        PageFetchTask(int page) {
            mPage = (page >= 1 && page <= 1000 ? page : 1);
            mConnectionError = false;
        }

        @Override
        protected JSONObject doInBackground(Void... params) {
            try {
                return NetworkUtils.fetchMovies(mSortMode, mPage);
            } catch (IOException e) {
                mConnectionError = true;
                return null;
            }
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            if (mConnectionError && mConnectivityHandler != null) {
                mConnectivityHandler.onConnectionFail();
            }
            updatePage(mPage, jsonObject);
        }
    }

    class MoviesViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        final private Context mContext;
        final private ImageView mIvMovie;

        MoviesViewHolder(View itemView) {
            super(itemView);

            mContext = itemView.getContext();
            mIvMovie = (ImageView) itemView.findViewById(R.id.iv_movie);
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            int page = position / PAGE_SIZE + 1;
            JSONObject jsonPage = mPages.get(page), jsonMovie = null;
            if (jsonPage != null) {
                try {
                    jsonMovie = jsonPage.getJSONArray("results").getJSONObject(position % PAGE_SIZE);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            if (mOnClickHandler != null && jsonMovie != null) {
                mOnClickHandler.onClick(jsonMovie);
            }
        }

        void bind(String posterPath) {
            if (posterPath == null) {
                mIvMovie.setImageResource(R.drawable.ic_broken_image_white_48dp);
                mIvMovie.setOnClickListener(MoviesViewHolder.this);
                mIvMovie.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                return;
            }

            mIvMovie.setOnClickListener(null);
            mIvMovie.setScaleType(ImageView.ScaleType.CENTER_CROP);
            Picasso.with(mContext)
                    .load(NetworkUtils.buildPosterUri(posterPath, ImageQuality.Default))
                    .into(mIvMovie, new Callback() {
                        @Override
                        public void onSuccess() {
                            mIvMovie.setOnClickListener(MoviesViewHolder.this);
                        }

                        @Override
                        public void onError() {
                            if (mConnectivityHandler != null) {
                                mConnectivityHandler.onConnectionFail();
                            }
                        }
                    });
        }
        void unbind() {
            mIvMovie.setImageDrawable(null);
            mIvMovie.setOnClickListener(null);
        }
    }

    private static final int PAGE_SIZE = 20;
    final private SortBy mSortMode;
    final private ConnectivityHandler mConnectivityHandler;
    final private OnClickHandler mOnClickHandler;
    final private SparseArray<JSONObject> mPages;
    private int mItemCount;

    MoviesAdapter(SortBy sortMode, ConnectivityHandler connectivityHandler, OnClickHandler onClickHandler) {
        mSortMode = (sortMode != null ? sortMode : SortBy.Popularity);
        mConnectivityHandler = connectivityHandler;
        mOnClickHandler = onClickHandler;
        mPages = new SparseArray<>();
        mItemCount = -1;

        fetchPage(1);
    }

    private void fetchPage(int page) {
        if (mPages.indexOfKey(page) < 0) {
            mPages.append(page, null);
            new PageFetchTask(page).execute();
        }
    }
    private void updatePage(int page, JSONObject jsonObject) {
        if (jsonObject == null) {
            mPages.delete(page);
            return;
        }

        mItemCount = jsonObject.optInt("total_results", -1);
        mPages.put(page, jsonObject);
        notifyItemRangeChanged((page - 1) * PAGE_SIZE, PAGE_SIZE);
    }

    @Override
    public MoviesViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new MoviesViewHolder(LayoutInflater.from(parent.getContext()).inflate(
                R.layout.view_movie, parent, false));
    }
    @Override
    public void onBindViewHolder(MoviesViewHolder holder, int position) {
        int page = position / PAGE_SIZE + 1;

        JSONObject jsonPage = mPages.get(page);
        if (jsonPage == null) {
            holder.unbind();
            fetchPage(page);
            return;
        }

        try {
            JSONObject movie = jsonPage.getJSONArray("results").getJSONObject(position % PAGE_SIZE);
            holder.bind(movie.isNull("poster_path") ? null : movie.getString("poster_path"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    @Override
    public int getItemCount() {
        return mItemCount;
    }
}
