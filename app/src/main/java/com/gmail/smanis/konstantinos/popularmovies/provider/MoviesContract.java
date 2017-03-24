package com.gmail.smanis.konstantinos.popularmovies.provider;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

import com.gmail.smanis.konstantinos.popularmovies.BuildConfig;

public final class MoviesContract {
    private MoviesContract() {}
    public static final String AUTHORITY = BuildConfig.APPLICATION_ID;
    public static final Uri BASE_CONTENT_URI =
            new Uri.Builder().scheme(ContentResolver.SCHEME_CONTENT).authority(AUTHORITY).build();
    public static final String PATH_MOVIES = "movies";

    public static final class MovieEntry implements BaseColumns {
        private MovieEntry() {}
        public static final String TABLE_NAME = "movies";
        public static final String COLUMN_ID = "id";
        public static final String COLUMN_ORIGINAL_TITLE = "original_title";
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_POSTER = "poster";
        public static final String COLUMN_RELEASE_DATE = "release_date";
        public static final String COLUMN_RATING = "rating";
        public static final String COLUMN_SYNOPSIS = "synopsis";
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_MOVIES);
        public static final String CONTENT_TYPE =
                String.format("vnd.android.cursor.dir/vnd.%s.%s", AUTHORITY, TABLE_NAME);
        public static final String CONTENT_ITEM_TYPE =
                String.format("vnd.android.cursor.item/vnd.%s.%s", AUTHORITY, TABLE_NAME);
    }
}
