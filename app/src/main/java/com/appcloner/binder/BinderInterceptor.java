package com.appcloner.binder;

import com.appcloner.manager.CloneManager;
import com.appcloner.model.CloneProfile;
import com.appcloner.util.Logger;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class BinderInterceptor {

    private static final String TAG = "BinderInterceptor";
    private static BinderInterceptor instance;
    private boolean isInitialized = false;

    private final Map<String, CloneProfile> activeProfiles = new ConcurrentHashMap<>();

    private BinderInterceptor() {}

    public static BinderInterceptor getInstance() {
        if (instance == null) {
            synchronized (BinderInterceptor.class) {
                if (instance == null) {
                    instance = new BinderInterceptor();
                }
            }
        }
        return instance;
    }

    public void init() {
        if (isInitialized) {
            Logger.d(TAG, "Already initialized");
            return;
        }

        try {
            isInitialized = true;
            Logger.d(TAG, "Binder interceptor initialized successfully");
        } catch (Exception e) {
            Logger.e(TAG, "Failed to initialize binder interceptor", e);
        }
    }

    public void activateProfile(CloneProfile profile) {
        if (profile != null) {
            activeProfiles.put(profile.getClonedPackageName(), profile);
            Logger.d(TAG, "Activated profile for " + profile.getClonedPackageName());
        }
    }

    public void deactivateProfile(String packageName) {
        activeProfiles.remove(packageName);
        Logger.d(TAG, "Deactivated profile for " + packageName);
    }

    public void clearProfiles() {
        activeProfiles.clear();
        Logger.d(TAG, "Cleared all active profiles");
    }

    public boolean isProfileActive(String packageName) {
        return activeProfiles.containsKey(packageName);
    }

    public void setLoggingEnabled(boolean enabled) {
    }

    public void cleanup() {
        if (isInitialized) {
            activeProfiles.clear();
            isInitialized = false;
            Logger.d(TAG, "Binder interceptor cleaned up");
        }
    }
}
