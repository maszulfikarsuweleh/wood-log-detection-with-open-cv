package com.suweleh.opencv

import android.app.Application
import android.util.Log
import org.opencv.android.OpenCVLoader

class MainApp: Application() {

    override fun onCreate() {
        super.onCreate()
        if (!OpenCVLoader.initDebug())
            Log.e("OpenCV", "Unable to load OpenCV!");
        else
            Log.d("OpenCV", "OpenCV loaded Successfully!");
    }
}