package com.gmail.smanis.konstantinos.popularmovies;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.List;

class TrailersAdapter extends RecyclerView.Adapter<TrailersAdapter.TrailerViewHolder> {

    static class Trailer {

        private final String mID;
        private final String mTitle;

        Trailer(String ID, String title) {
            mID = ID;
            mTitle = title;
        }
        String getID() {
            return mID;
        }
        String getTitle() {
            return mTitle;
        }
    }
    static class TrailerViewHolder extends RecyclerView.ViewHolder implements Target {

        private final Context mContext;
        private final TextView mTvTrailer;
        private Trailer mTrailer;

        TrailerViewHolder(View itemView) {
            super(itemView);
            mContext = itemView.getContext();
            mTvTrailer = (TextView) itemView.findViewById(R.id.tv_trailer);
            mTvTrailer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mContext.startActivity(new Intent(
                            Intent.ACTION_VIEW,
                            NetworkUtils.buildYouTubeVideoUri(mTrailer.getID())
                    ));
                }
            });
        }

        void bind(Trailer trailer) {
            mTrailer = trailer;
            Picasso.with(mContext)
                    .load(NetworkUtils.buildYouTubeVideoThumbnailUri(trailer.getID()))
                    .placeholder(R.drawable.ic_play_circle_filled_white_48dp)
                    .into(this);
            mTvTrailer.setText(trailer.getTitle());
        }
        void unbind() {
            Picasso.with(mContext).cancelRequest(this);
        }

        @Override
        public void onPrepareLoad(Drawable placeHolderDrawable) {
            mTvTrailer.setCompoundDrawablesWithIntrinsicBounds(
                    null,
                    placeHolderDrawable,
                    null,
                    null
            );
        }
        @Override
        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
            mTvTrailer.setCompoundDrawablesWithIntrinsicBounds(
                    null,
                    new BitmapDrawable(mContext.getResources(), bitmap),
                    null,
                    null
            );
        }
        @Override
        public void onBitmapFailed(Drawable errorDrawable) {
        }
    }

    private final List<Trailer> mTrailers;

    TrailersAdapter(List<Trailer> trailers) {
        mTrailers = trailers;
    }

    @Override
    public TrailerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new TrailerViewHolder(LayoutInflater.from(parent.getContext()).inflate(
                R.layout.view_trailer, parent, false));
    }
    @Override
    public void onBindViewHolder(TrailerViewHolder holder, int position) {
        holder.bind(mTrailers.get(position));
    }
    @Override
    public void onViewRecycled(TrailerViewHolder holder) {
        holder.unbind();
    }
    @Override
    public int getItemCount() {
        return (mTrailers != null ? mTrailers.size() : 0);
    }
}
