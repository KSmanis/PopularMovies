package com.gmail.smanis.konstantinos.popularmovies;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

enum ImageQuality {
    Default,
    Best
}

class NetworkUtils {

    private static final String TMDB_API_BASE_URL = "https://api.themoviedb.org/3/";
    private static final String TMDB_API_MOVIE_PATH = "movie";
    private static final String TMDB_API_MOVIE_POPULAR_PATH = "popular";
    private static final String TMDB_API_MOVIE_TOP_RATED_PATH = "top_rated";
    private static final String TMDB_API_MOVIE_VIDEOS_PATH = "videos";
    private static final String TMDB_API_MOVIE_REVIEWS_PATH = "reviews";
    private static final String TMDB_IMG_BASE_URL = "https://image.tmdb.org/t/p/";
    private static final String TMDB_API_KEY_PARAM = "api_key";
    private static final String TMDB_PAGE_PARAM = "page";
    private static final String[] TMDB_POSTER_SIZES = {
            "w92",
            "w154",
            "w185",
            "w342",
            "w500",
            "w780",
            "original"
    };

    private static final String YT_VID_BASE_URL = "https://www.youtube.com/";
    private static final String YT_VID_WATCH_PATH = "watch";
    private static final String YT_IMG_BASE_URL = "https://img.youtube.com/vi/";
    private static final String YT_IMG_QUALITY = "0.jpg";
    private static final String YT_VID_PARAM = "v";

    private static String sPosterSize = "w185";

    static void adjustPosterSize(int posterWidth) {
        int minDiff = Integer.MAX_VALUE;
        for (String posterSize : TMDB_POSTER_SIZES) {
            if (!posterSize.matches("^w\\d+$")) {
                continue;
            }

            int diff = Math.abs(Integer.valueOf(posterSize.substring(1)) - posterWidth);
            if (diff < minDiff) {
                minDiff = diff;
                sPosterSize = posterSize;
            }
        }
    }
    static Uri buildPosterUri(String posterPath, ImageQuality quality) {
        String posterSize, posterSizes[] = TMDB_POSTER_SIZES;
        switch (quality) {
        default:
        case Default:
            posterSize = sPosterSize;
            break;
        case Best:
            posterSize = posterSizes[posterSizes.length - 1];
            break;
        }
        return buildImageUri(posterSize, posterPath);
    }
    static Uri buildYouTubeVideoThumbnailUri(String videoID) {
        return Uri.parse(YT_IMG_BASE_URL).buildUpon()
                .appendEncodedPath(videoID)
                .appendEncodedPath(YT_IMG_QUALITY)
                .build();
    }
    static Uri buildYouTubeVideoUri(String videoID) {
        return Uri.parse(YT_VID_BASE_URL).buildUpon()
                .appendEncodedPath(YT_VID_WATCH_PATH)
                .appendQueryParameter(YT_VID_PARAM, videoID)
                .build();
    }
    static JSONObject fetchMovies(SortBy sortMode, int page) throws IOException {
        switch (sortMode) {
        case Popularity:
            return jsonFromUri(buildMoviesUri(TMDB_API_MOVIE_POPULAR_PATH, page));
        case Rating:
            return jsonFromUri(buildMoviesUri(TMDB_API_MOVIE_TOP_RATED_PATH, page));
        default:
            return null;
        }
    }
    static JSONObject fetchReviews(int movieID, int page) throws IOException {
        return jsonFromUri(buildReviewsUri(movieID, page));
    }
    static JSONObject fetchVideos(int movieID) throws IOException {
        return jsonFromUri(buildVideosUri(movieID));
    }
    static boolean isOnline(Context context) {
        ConnectivityManager connManager =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = connManager.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    private static Uri buildImageUri(String size, String imagePath) {
        return Uri.parse(TMDB_IMG_BASE_URL).buildUpon()
                .appendEncodedPath(size)
                .appendEncodedPath(imagePath)
                .build();
    }
    private static Uri buildMoviesUri(String endpoint, int page) {
        return Uri.parse(TMDB_API_BASE_URL).buildUpon()
                .appendEncodedPath(TMDB_API_MOVIE_PATH)
                .appendEncodedPath(endpoint)
                .appendQueryParameter(TMDB_API_KEY_PARAM, BuildConfig.TMDB_API_KEY)
                .appendQueryParameter(TMDB_PAGE_PARAM, String.valueOf(page))
                .build();
    }
    private static Uri buildReviewsUri(int movieID, int page) {
        return Uri.parse(TMDB_API_BASE_URL).buildUpon()
                .appendEncodedPath(TMDB_API_MOVIE_PATH)
                .appendEncodedPath(String.valueOf(movieID))
                .appendEncodedPath(TMDB_API_MOVIE_REVIEWS_PATH)
                .appendQueryParameter(TMDB_API_KEY_PARAM, BuildConfig.TMDB_API_KEY)
                .appendQueryParameter(TMDB_PAGE_PARAM, String.valueOf(page))
                .build();
    }
    private static Uri buildVideosUri(int movieID) {
        return Uri.parse(TMDB_API_BASE_URL).buildUpon()
                .appendEncodedPath(TMDB_API_MOVIE_PATH)
                .appendEncodedPath(String.valueOf(movieID))
                .appendEncodedPath(TMDB_API_MOVIE_VIDEOS_PATH)
                .appendQueryParameter(TMDB_API_KEY_PARAM, BuildConfig.TMDB_API_KEY)
                .build();
    }

    private static JSONObject jsonFromUri(Uri uri) throws IOException {
        try {
            return new JSONObject(responseFromUrl(new URL(uri.toString())));
        } catch (JSONException | MalformedURLException e) {
            e.printStackTrace();
            return null;
        }
    }
    private static String responseFromUrl(URL url) throws IOException {
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        try {
            Scanner scanner = new Scanner(urlConnection.getInputStream()).useDelimiter("\\A");
            return scanner.hasNext() ? scanner.next() : null;
        } finally {
            urlConnection.disconnect();
        }
    }
}
