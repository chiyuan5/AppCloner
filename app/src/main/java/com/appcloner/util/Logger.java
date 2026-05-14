package com.appcloner.util;

import android.content.Context;
import android.util.Log;

public class Logger {

    private static final String TAG = "AppCloner";
    private static boolean debugEnabled = true;
    private static Context appContext;

    public static void init(Context context) {
        appContext = context.getApplicationContext();
    }

    public static void d(String tag, String message) {
        if (debugEnabled) {
            Log.d(TAG + "/" + tag, message);
        }
    }

    public static void i(String tag, String message) {
        if (debugEnabled) {
            Log.i(TAG + "/" + tag, message);
        }
    }

    public static void w(String tag, String message) {
        Log.w(TAG + "/" + tag, message);
    }

    public static void e(String tag, String message) {
        Log.e(TAG + "/" + tag, message);
    }

    public static void e(String tag, String message, Throwable throwable) {
        Log.e(TAG + "/" + tag, message, throwable);
    }

    public static void e(String tag, String message, Exception e) {
        Log.e(TAG + "/" + tag, message);
        if (debugEnabled && e != null) {
            e.printStackTrace();
        }
    }

    public static void setDebugEnabled(boolean enabled) {
        debugEnabled = enabled;
    }

    public static boolean isDebugEnabled() {
        return debugEnabled;
    }
}
