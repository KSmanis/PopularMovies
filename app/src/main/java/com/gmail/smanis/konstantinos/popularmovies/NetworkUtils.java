package com.gmail.smanis.konstantinos.popularmovies;

import android.net.Uri;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;
import java.util.Scanner;

enum ImageQuality {
    Default,
    Best
}

class NetworkUtils {

    private static final String TMDB_API_BASE_URL = "https://api.themoviedb.org/3/";
    private static final String TMDB_FALLBACK_IMG_BASE_URL = "http://image.tmdb.org/t/p/";
    private static final String[] TMDB_FALLBACK_POSTER_SIZES = {
            "w92",
            "w154",
            "w185",
            "w342",
            "w500",
            "w780",
            "original"
    };
    private static final String TMDB_FALLBACK_POSTER_SIZE = "w185";
    private static final String TMDB_API_ENDPOINT_CONFIGURATION = "configuration";
    private static final String TMDB_API_ENDPOINT_POPULAR_MOVIES = "movie/popular";
    private static final String TMDB_API_ENDPOINT_TOP_RATED_MOVIES = "movie/top_rated";
    private static final String TMDB_API_ENDPOINT_MOVIE_VIDEOS = "movie/%d/videos";
    private static final String TMDB_API_KEY_PARAM = "api_key";
    private static final String TMDB_PAGE_PARAM = "page";

    private static final String YT_VID_BASE_URL = "https://www.youtube.com/watch";
    private static final String YT_IMG_BASE_URL = "https://img.youtube.com/vi/";
    private static final String YT_IMG_QUALITY = "0.jpg";
    private static final String YT_VID_PARAM = "v";

    private static String gImageBaseUrl;
    private static String[] gPosterSizes;
    private static String gPosterSize;

    static Uri buildPosterUri(String posterPath, ImageQuality quality) {
        String posterSize, posterSizes[] = posterSizes();
        switch (quality) {
        default:
        case Default:
            posterSize = posterSize();
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
                .appendQueryParameter(YT_VID_PARAM, videoID)
                .build();
    }
    static void fetchConfiguration() {
        if (gImageBaseUrl != null && gPosterSizes != null) {
            return;
        }

        JSONObject jsonConfig = null;
        try {
            jsonConfig = jsonFromUri(buildConfigUri());
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (jsonConfig == null) {
            return;
        }

        JSONObject imagesConfig = null;
        try {
            imagesConfig = jsonConfig.getJSONObject("images");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (imagesConfig == null) {
            return;
        }

        if (gImageBaseUrl == null) {
            gImageBaseUrl = imagesConfig.optString("base_url");
        }
        if (gPosterSizes == null) {
            gPosterSizes = stringArrayFromJsonArray(imagesConfig.optJSONArray("poster_sizes"));
        }
    }
    static JSONObject fetchMovies(SortBy sortMode, int page) throws IOException {
        switch (sortMode) {
        case Popularity:
            return jsonFromUri(buildMoviesUri(TMDB_API_ENDPOINT_POPULAR_MOVIES, page));
        case Rating:
            return jsonFromUri(buildMoviesUri(TMDB_API_ENDPOINT_TOP_RATED_MOVIES, page));
        default:
            return null;
        }
    }
    static JSONObject fetchVideos(int movieID) throws IOException {
        return jsonFromUri(buildVideosUri(movieID));
    }
    static void updateConfiguration(int desiredPosterWidth) {
        int minDiff = Integer.MAX_VALUE;
        for (String posterSize : posterSizes()) {
            if (!posterSize.matches("^w\\d+$")) {
                continue;
            }

            int diff = Math.abs(Integer.valueOf(posterSize.substring(1)) - desiredPosterWidth);
            if (diff < minDiff) {
                minDiff = diff;
                gPosterSize = posterSize;
            }
        }
    }

    private static String imageBaseUrl() {
        return gImageBaseUrl != null ? gImageBaseUrl : TMDB_FALLBACK_IMG_BASE_URL;
    }
    private static String[] posterSizes() {
        return gPosterSizes != null ? gPosterSizes : TMDB_FALLBACK_POSTER_SIZES;
    }
    private static String posterSize() {
        return gPosterSize != null ? gPosterSize : TMDB_FALLBACK_POSTER_SIZE;
    }

    private static Uri buildConfigUri() {
        return Uri.parse(TMDB_API_BASE_URL).buildUpon()
                .appendEncodedPath(TMDB_API_ENDPOINT_CONFIGURATION)
                .appendQueryParameter(TMDB_API_KEY_PARAM, BuildConfig.TMDB_API_KEY)
                .build();
    }
    private static Uri buildImageUri(String size, String imagePath) {
        return Uri.parse(imageBaseUrl()).buildUpon()
                .appendEncodedPath(size)
                .appendEncodedPath(imagePath)
                .build();
    }
    private static Uri buildMoviesUri(String endpoint, int page) {
        return Uri.parse(TMDB_API_BASE_URL).buildUpon()
                .appendEncodedPath(endpoint)
                .appendQueryParameter(TMDB_API_KEY_PARAM, BuildConfig.TMDB_API_KEY)
                .appendQueryParameter(TMDB_PAGE_PARAM, String.valueOf(page))
                .build();
    }
    private static Uri buildVideosUri(int movieID) {
        return Uri.parse(TMDB_API_BASE_URL).buildUpon()
                .appendEncodedPath(String.format(Locale.US, TMDB_API_ENDPOINT_MOVIE_VIDEOS, movieID))
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
    private static String[] stringArrayFromJsonArray(JSONArray jsonArray) {
        if (jsonArray == null) {
            return null;
        }

        String[] ret = new String[jsonArray.length()];
        for (int i = 0; i < ret.length; ++i) {
            ret[i] = jsonArray.optString(i);
        }
        return ret;
    }
}
