package com.gmail.smanis.konstantinos.popularmovies;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

public class PosterActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_poster);

        final ProgressBar pbImageLoading = (ProgressBar) findViewById(R.id.pb_image_loading);
        final ImageView ivMoviePoster = (ImageView) findViewById(R.id.iv_movie_poster);
        ivMoviePoster.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        Intent intent = getIntent();
        String posterPath;
        if (intent == null || (posterPath = intent.getStringExtra(MainActivity.EXTRA_MOVIE_POSTER)) == null) {
            pbImageLoading.setVisibility(View.GONE);
            ivMoviePoster.setImageResource(R.drawable.ic_broken_image_white_48dp);
            return;
        }

        Picasso.with(this)
                .load(NetworkUtils.buildPosterUri(posterPath, ImageQuality.Best))
                .error(R.drawable.ic_broken_image_white_48dp)
                .into(ivMoviePoster, new Callback() {
                    @Override
                    public void onSuccess() {
                        pbImageLoading.setVisibility(View.GONE);
                    }

                    @Override
                    public void onError() {
                        pbImageLoading.setVisibility(View.GONE);
                    }
                });
    }
}
