package com.gmail.smanis.konstantinos.popularmovies.provider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

public class MoviesContentProvider extends ContentProvider {

    private static final int MOVIES = 100;
    private static final int MOVIES_ID = 101;
    private static final UriMatcher sUriMatcher;
    static {
        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sUriMatcher.addURI(MoviesContract.AUTHORITY, MoviesContract.PATH_MOVIES, MOVIES);
        sUriMatcher.addURI(MoviesContract.AUTHORITY, MoviesContract.PATH_MOVIES + "/#", MOVIES_ID);
    }
    private MoviesDbHelper mDbHelper;

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection,
                      @Nullable String[] selectionArgs) {
        int ret;
        switch (sUriMatcher.match(uri)) {
        case MOVIES:
            ret = mDbHelper.getWritableDatabase().delete(
                    MoviesContract.MovieEntry.TABLE_NAME,
                    (selection != null ? selection : "1"),
                    selectionArgs
            );
            break;
        case MOVIES_ID:
            ret = mDbHelper.getWritableDatabase().delete(
                    MoviesContract.MovieEntry.TABLE_NAME,
                    MoviesContract.MovieEntry._ID + "=" + ContentUris.parseId(uri) +
                            (!TextUtils.isEmpty(selection) ? " AND (" + selection + ")" : ""),
                    selectionArgs
            );
            break;
        default:
            throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        if (ret != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return ret;
    }
    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        switch (sUriMatcher.match(uri)) {
        case MOVIES:
            return MoviesContract.MovieEntry.CONTENT_TYPE;
        case MOVIES_ID:
            return MoviesContract.MovieEntry.CONTENT_ITEM_TYPE;
        default:
            throw new IllegalArgumentException("Unknown URI: " + uri);
        }
    }
    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        if (sUriMatcher.match(uri) != MOVIES) {
            throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        long rowId = mDbHelper.getWritableDatabase().insert(
                MoviesContract.MovieEntry.TABLE_NAME,
                null,
                values
        );
        if (rowId == -1) {
            throw new SQLException("Error inserting row into URI: " + uri);
        }

        Uri ret = ContentUris.withAppendedId(MoviesContract.MovieEntry.CONTENT_URI, rowId);
        getContext().getContentResolver().notifyChange(ret, null);
        return ret;
    }
    @Override
    public boolean onCreate() {
        mDbHelper = new MoviesDbHelper(getContext());
        return true;
    }
    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection,
                        @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        Cursor ret;
        switch (sUriMatcher.match(uri)) {
        case MOVIES:
            ret = mDbHelper.getReadableDatabase().query(
                    MoviesContract.MovieEntry.TABLE_NAME,
                    projection,
                    selection,
                    selectionArgs,
                    null,
                    null,
                    sortOrder
            );
            break;
        case MOVIES_ID:
            ret = mDbHelper.getReadableDatabase().query(
                    MoviesContract.MovieEntry.TABLE_NAME,
                    projection,
                    MoviesContract.MovieEntry._ID + "=" + ContentUris.parseId(uri) +
                            (!TextUtils.isEmpty(selection) ? " AND (" + selection + ")" : ""),
                    selectionArgs,
                    null,
                    null,
                    sortOrder
            );
            break;
        default:
            throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        ret.setNotificationUri(getContext().getContentResolver(), uri);
        return ret;
    }
    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection,
                      @Nullable String[] selectionArgs) {
        int ret;
        switch (sUriMatcher.match(uri)) {
        case MOVIES:
            ret = mDbHelper.getWritableDatabase().update(
                    MoviesContract.MovieEntry.TABLE_NAME,
                    values,
                    selection,
                    selectionArgs
            );
            break;
        case MOVIES_ID:
            ret = mDbHelper.getWritableDatabase().update(
                    MoviesContract.MovieEntry.TABLE_NAME,
                    values,
                    MoviesContract.MovieEntry._ID + "=" + ContentUris.parseId(uri) +
                            (!TextUtils.isEmpty(selection) ? " AND (" + selection + ")" : ""),
                    selectionArgs
            );
            break;
        default:
            throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        if (ret > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return ret;
    }
}
