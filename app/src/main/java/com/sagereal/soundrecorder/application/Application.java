package com.sagereal.soundrecorder.application;

import android.annotation.SuppressLint;
import android.content.Context;

public class Application extends android.app.Application {
    @SuppressLint("StaticFieldLeak")
    public static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        context = this.getApplicationContext();
    }
}

