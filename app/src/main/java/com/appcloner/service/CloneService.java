package com.appcloner.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.appcloner.binder.BinderInterceptor;
import com.appcloner.manager.CloneManager;
import com.appcloner.util.Logger;

public class CloneService extends Service {

    private static final String TAG = "CloneService";
    private BinderInterceptor binderInterceptor;
    private CloneManager cloneManager;

    @Override
    public void onCreate() {
        super.onCreate();
        Logger.d(TAG, "CloneService created");

        cloneManager = CloneManager.getInstance();
        binderInterceptor = BinderInterceptor.getInstance();

        binderInterceptor.init();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Logger.d(TAG, "CloneService started");

        if (intent != null) {
            String action = intent.getAction();
            if ("android.app.action.ACTIVATE_CLONE".equals(action)) {
                String packageName = intent.getStringExtra("package_name");
                if (packageName != null) {
                    activateClone(packageName);
                }
            } else if ("android.app.action.DEACTIVATE_CLONE".equals(action)) {
                String packageName = intent.getStringExtra("package_name");
                if (packageName != null) {
                    deactivateClone(packageName);
                }
            }
        }

        return START_STICKY;
    }

    private void activateClone(String packageName) {
        try {
            com.appcloner.model.CloneProfile profile = cloneManager.getCloneProfile(packageName);
            if (profile != null) {
                binderInterceptor.activateProfile(profile);
                Logger.d(TAG, "Activated clone: " + packageName);
            }
        } catch (Exception e) {
            Logger.e(TAG, "Failed to activate clone", e);
        }
    }

    private void deactivateClone(String packageName) {
        try {
            binderInterceptor.deactivateProfile(packageName);
            Logger.d(TAG, "Deactivated clone: " + packageName);
        } catch (Exception e) {
            Logger.e(TAG, "Failed to deactivate clone", e);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        binderInterceptor.cleanup();
        Logger.d(TAG, "CloneService destroyed");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
