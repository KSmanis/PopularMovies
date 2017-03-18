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

        final ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressbar_poster);
        final ImageView imageView = (ImageView) findViewById(R.id.imageview_poster);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        Intent intent = getIntent();
        String posterPath;
        if (intent == null || (posterPath = intent.getStringExtra(MainActivity.EXTRA_MOVIE_POSTER)) == null) {
            progressBar.setVisibility(View.GONE);
            imageView.setImageResource(R.drawable.ic_broken_image_white_48dp);
            return;
        }

        Picasso.with(this)
                .load(NetworkUtils.buildPosterUri(posterPath, ImageQuality.Best))
                .error(R.drawable.ic_broken_image_white_48dp)
                .into(imageView, new Callback() {
                    @Override
                    public void onSuccess() {
                        progressBar.setVisibility(View.GONE);
                    }

                    @Override
                    public void onError() {
                        progressBar.setVisibility(View.GONE);
                    }
                });
    }
}
