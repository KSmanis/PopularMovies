package com.gmail.smanis.konstantinos.popularmovies;

import android.app.Application;

import com.squareup.leakcanary.LeakCanary;

public class PopularMovies extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        if (LeakCanary.isInAnalyzerProcess(this)) {
            return;
        }
        LeakCanary.install(this);
    }
}
