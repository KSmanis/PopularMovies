package com.gmail.smanis.konstantinos.popularmovies;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DetailsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        final TextView tvMovieTitle = (TextView) findViewById(R.id.tv_movie_title);
        final ImageView ivMoviePoster = (ImageView) findViewById(R.id.iv_movie_poster);
        final TextView tvMovieReleaseDate = (TextView) findViewById(R.id.tv_movie_release_date);
        final TextView tvMovieRating = (TextView) findViewById(R.id.tv_movie_rating);
        final TextView tvMovieSynopsis = (TextView) findViewById(R.id.tv_movie_synopsis);

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
        }
    }
}
