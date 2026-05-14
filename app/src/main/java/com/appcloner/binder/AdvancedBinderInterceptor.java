package com.appcloner.binder;

import android.app.ifma.mts.binderceptor.BinderceptorManager;
import android.app.ifma.mts.binderceptor.BinderParcel;
import android.app.ifma.mts.binderceptor.EIoctlAction;
import android.app.ifma.mts.binderceptor.TBinderInfo;
import android.app.ifma.mts.binderceptor.TBinderTokenItem;
import android.app.ifma.mts.binderceptor.binder_txn_st;

import com.appcloner.manager.CloneManager;
import com.appcloner.model.CloneProfile;
import com.appcloner.util.Logger;

import java.util.HashMap;
import java.util.Map;

public class AdvancedBinderInterceptor {

    private static final String TAG = "AdvancedBinderInterceptor";
    private static AdvancedBinderInterceptor instance;
    private boolean isActive = false;

    private static final String IPACKAGE_MANAGER = "android.content.pm.IPackageManager";
    private static final String IACTIVITY_MANAGER = "android.app.IActivityManager";
    private static final String IUSER_MANAGER = "android.os.IUserManager";

    private static final int CODE_GET_PACKAGE_INFO = 10;
    private static final int CODE_GET_APPLICATION_INFO = 23;
    private static final int CODE_GET_PACKAGE_ARCHIVE_INFO = 68;
    private static final int CODE_GET_INSTALLED_PACKAGES = 56;
    private static final int CODE_CHECK_SIGNATURE_PERMISSION = 27;

    private static final int CODE_GET_ACTIVITY_INFO = 1;
    private static final int CODE_START_ACTIVITY = 2;

    private static final int CODE_GET_USER_HANDLE = 28;
    private static final int CODE_GET_USER_NAME = 29;

    private final Map<String, CloneProfile> spoofingRules = new HashMap<>();
    private final BinderceptorCallback callback;

    private AdvancedBinderInterceptor() {
        callback = new BinderceptorCallback();
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
            BinderceptorManager.init();
            BinderceptorManager.registerCallback(callback);
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
            java.util.List<com.appcloner.model.AppInfo> clonedApps = manager.getClonedApps();
            for (com.appcloner.model.AppInfo appInfo : clonedApps) {
                CloneProfile profile = manager.getCloneProfile(appInfo.getPackageName());
                if (profile != null && profile.getClonedPackageName() != null) {
                    spoofingRules.put(profile.getClonedPackageName(), profile);
                }
            }
        } catch (Exception e) {
            Logger.e(TAG, "Failed to load spoofing rules", e);
        }
    }

    public BinderceptorCallback getCallback() {
        return callback;
    }

    private class BinderceptorCallback extends android.app.ifma.mts.binderceptor.IBinderceptorCallback.Stub {

        @Override
        public void onBinderTransaction(String service, int code, byte[] data, byte[] reply) {
            if (!isActive || spoofingRules.isEmpty()) {
                return;
            }

            try {
                if (service != null && (service.contains("IPackageManager") || service.contains("PackageManager"))) {
                    handlePackageManagerTransaction(service, code, data, reply);
                } else if (service != null && (service.contains("IActivityManager") || service.contains("ActivityManager"))) {
                    handleActivityManagerTransaction(service, code, data, reply);
                } else if (service != null && (service.contains("IUserManager") || service.contains("UserManager"))) {
                    handleUserManagerTransaction(service, code, data, reply);
                }
            } catch (Exception e) {
                Logger.e(TAG, "Error in onBinderTransaction", e);
            }
        }

        @Override
        public void onBinderOneway(String service, int code, byte[] data) {
            if (!isActive || spoofingRules.isEmpty()) {
                return;
            }

            try {
                if (service != null && (service.contains("IPackageManager") || service.contains("PackageManager"))) {
                    handlePackageManagerOneway(service, code, data);
                } else if (service != null && (service.contains("IActivityManager") || service.contains("ActivityManager"))) {
                    handleActivityManagerOneway(service, code, data);
                }
            } catch (Exception e) {
                Logger.e(TAG, "Error in onBinderOneway", e);
            }
        }

        @Override
        public void onBinderAfterFaker(String service, int code, byte[] reply) {
            if (!isActive || spoofingRules.isEmpty()) {
                return;
            }

            try {
                if (service != null && (service.contains("IPackageManager") || service.contains("PackageManager"))) {
                    processSpoofedPackageManagerReply(service, code, reply);
                }
            } catch (Exception e) {
                Logger.e(TAG, "Error in onBinderAfterFaker", e);
            }
        }
    }

    private void handlePackageManagerTransaction(String service, int code, byte[] data, byte[] reply) {
        switch (code) {
            case CODE_GET_PACKAGE_INFO:
            case CODE_GET_APPLICATION_INFO:
            case CODE_GET_PACKAGE_ARCHIVE_INFO:
            case CODE_GET_INSTALLED_PACKAGES:
                processPackageQuery(service, code, data, reply);
                break;

            case CODE_CHECK_SIGNATURE_PERMISSION:
                Logger.d(TAG, "Intercepted signature permission check");
                break;

            default:
                break;
        }
    }

    private void handleActivityManagerTransaction(String service, int code, byte[] data, byte[] reply) {
        switch (code) {
            case CODE_GET_ACTIVITY_INFO:
            case CODE_START_ACTIVITY:
                Logger.d(TAG, "Intercepted activity manager call: " + code);
                break;

            default:
                break;
        }
    }

    private void handleUserManagerTransaction(String service, int code, byte[] data, byte[] reply) {
        switch (code) {
            case CODE_GET_USER_HANDLE:
            case CODE_GET_USER_NAME:
                Logger.d(TAG, "Intercepted user manager call: " + code);
                break;

            default:
                break;
        }
    }

    private void handlePackageManagerOneway(String service, int code, byte[] data) {
        Logger.d(TAG, "Oneway package manager call: " + code);
    }

    private void handleActivityManagerOneway(String service, int code, byte[] data) {
        Logger.d(TAG, "Oneway activity manager call: " + code);
    }

    private void processPackageQuery(String service, int code, byte[] data, byte[] reply) {
        if (data == null || data.length < 8) {
            return;
        }

        String packageName = extractPackageNameFromData(data);
        if (packageName == null || packageName.isEmpty()) {
            return;
        }

        CloneProfile profile = findProfileByPackageName(packageName);
        if (profile != null) {
            Logger.d(TAG, "Found spoofing profile for: " + packageName);
        }
    }

    private void processSpoofedPackageManagerReply(String service, int code, byte[] reply) {
        if (reply == null || spoofingRules.isEmpty()) {
            return;
        }

        Logger.d(TAG, "Processing spoofed reply for code: " + code);
    }

    private String extractPackageNameFromData(byte[] data) {
        try {
            if (data.length < 8) {
                return "";
            }

            int offset = 0;
            offset += 4;

            int length = readInt(data, offset);
            if (length <= 0 || length > 256) {
                return "";
            }

            offset += 4;
            if (offset + length * 2 > data.length) {
                return "";
            }

            char[] chars = new char[length];
            for (int i = 0; i < length; i++) {
                int charValue = (data[offset + i * 2] & 0xFF) | ((data[offset + i * 2 + 1] & 0xFF) << 8);
                chars[i] = (char) charValue;
            }

            return new String(chars);
        } catch (Exception e) {
            Logger.e(TAG, "Failed to extract package name", e);
        }

        return "";
    }

    private int readInt(byte[] data, int offset) {
        if (offset + 4 > data.length) {
            return 0;
        }
        return (data[offset] & 0xFF) |
               ((data[offset + 1] & 0xFF) << 8) |
               ((data[offset + 2] & 0xFF) << 16) |
               ((data[offset + 3] & 0xFF) << 24);
    }

    private CloneProfile findProfileByPackageName(String packageName) {
        for (Map.Entry<String, CloneProfile> entry : spoofingRules.entrySet()) {
            CloneProfile profile = entry.getValue();
            if (profile.getClonedPackageName().equals(packageName) ||
                profile.getOriginalPackageName().equals(packageName)) {
                return profile;
            }
        }
        return null;
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
