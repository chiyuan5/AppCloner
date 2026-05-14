package com.appcloner;

import android.app.Application;
import android.app.ifma.mts.binderceptor.BinderceptorManager;

import com.appcloner.manager.CloneManager;
import com.appcloner.manager.SpoofConfig;
import com.appcloner.util.Logger;

public class AppClonerApplication extends Application {

    private static AppClonerApplication instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;

        Logger.init(this);
        initBinderceptor();
        CloneManager.getInstance().init(this);
    }

    private void initBinderceptor() {
        try {
            BinderceptorManager.init();
            BinderceptorManager.setLogger(0);

            Logger.d("AppClonerApplication", "Binderceptor initialized successfully");
        } catch (Exception e) {
            Logger.e("AppClonerApplication", "Failed to initialize Binderceptor", e);
        }
    }

    public static AppClonerApplication getInstance() {
        return instance;
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        CloneManager.getInstance().cleanup();
    }
}
