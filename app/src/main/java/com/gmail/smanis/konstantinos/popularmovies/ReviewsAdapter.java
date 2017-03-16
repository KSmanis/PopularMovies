package com.gmail.smanis.konstantinos.popularmovies;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

class ReviewsAdapter extends RecyclerView.Adapter<ReviewsAdapter.ReviewViewHolder> {

    static class Review {

        private final String mAuthor;
        private final String mContent;

        Review(String author, String content) {
            mAuthor = author;
            mContent = content;
        }
        String getAuthor() {
            return mAuthor;
        }
        String getContent() {
            return mContent;
        }
    }
    static class ReviewViewHolder extends RecyclerView.ViewHolder {

        private final Context mContext;
        private final TextView mTvReviewAuthor;
        private final TextView mTvReviewContent;

        ReviewViewHolder(View itemView) {
            super(itemView);
            mContext = itemView.getContext();
            mTvReviewAuthor = (TextView) itemView.findViewById(R.id.tv_review_author);
            mTvReviewContent = (TextView) itemView.findViewById(R.id.tv_review_content);
        }

        void bind(Review review) {
            mTvReviewAuthor.setText(mContext.getString(R.string.tv_review_author, review.getAuthor()));
            mTvReviewContent.setText(review.getContent());
        }
    }

    private final List<Review> mReviews;

    ReviewsAdapter(List<Review> reviews) {
        mReviews = reviews;
    }

    @Override
    public ReviewViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ReviewViewHolder(LayoutInflater.from(parent.getContext()).inflate(
                R.layout.item_review, parent, false));
    }
    @Override
    public void onBindViewHolder(ReviewViewHolder holder, int position) {
        holder.bind(mReviews.get(position));
    }
    @Override
    public int getItemCount() {
        return (mReviews != null ? mReviews.size() : 0);
    }
}
