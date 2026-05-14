package com.appcloner.binder;

import com.appcloner.manager.CloneManager;
import com.appcloner.model.CloneProfile;
import com.appcloner.model.AppInfo;
import com.appcloner.util.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdvancedBinderInterceptor {

    private static final String TAG = "AdvancedBinderInterceptor";
    private static AdvancedBinderInterceptor instance;
    private boolean isActive = false;

    private final Map<String, CloneProfile> spoofingRules = new HashMap<>();

    private AdvancedBinderInterceptor() {
    }

    public static AdvancedBinderInterceptor getInstance() {
        if (instance == null) {
            synchronized (AdvancedBinderInterceptor.class) {
                if (instance == null) {
                    instance = new AdvancedBinderInterceptor();
                }
            }
        }
        return instance;
    }

    public void activate() {
        if (isActive) {
            return;
        }

        try {
            loadSpoofingRules();
            isActive = true;
            Logger.d(TAG, "Advanced binder interceptor activated");
        } catch (Exception e) {
            Logger.e(TAG, "Failed to activate advanced binder interceptor", e);
        }
    }

    public void deactivate() {
        if (!isActive) {
            return;
        }

        spoofingRules.clear();
        isActive = false;
        Logger.d(TAG, "Advanced binder interceptor deactivated");
    }

    private void loadSpoofingRules() {
        CloneManager manager = CloneManager.getInstance();
        spoofingRules.clear();

        try {
            List<AppInfo> clonedApps = manager.getClonedApps();
            for (AppInfo appInfo : clonedApps) {
                CloneProfile profile = manager.getCloneProfile(appInfo.getPackageName());
                if (profile != null && profile.getClonedPackageName() != null) {
                    spoofingRules.put(profile.getClonedPackageName(), profile);
                }
            }
        } catch (Exception e) {
            Logger.e(TAG, "Failed to load spoofing rules", e);
        }
    }

    public void addSpoofingRule(CloneProfile profile) {
        if (profile != null && profile.getClonedPackageName() != null) {
            spoofingRules.put(profile.getClonedPackageName(), profile);
            Logger.d(TAG, "Added spoofing rule for: " + profile.getClonedPackageName());
        }
    }

    public void removeSpoofingRule(String packageName) {
        spoofingRules.remove(packageName);
        Logger.d(TAG, "Removed spoofing rule for: " + packageName);
    }

    public void clearSpoofingRules() {
        spoofingRules.clear();
        Logger.d(TAG, "Cleared all spoofing rules");
    }

    public boolean isPackageSpoofed(String packageName) {
        return spoofingRules.containsKey(packageName);
    }

    public boolean isActive() {
        return isActive;
    }
}
