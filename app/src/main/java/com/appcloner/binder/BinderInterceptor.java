package com.appcloner.binder;

import android.app.ifma.mts.binderceptor.BinderceptorManager;
import android.app.ifma.mts.binderceptor.IBinderceptorCallback;
import android.app.ifma.mts.binderceptor.BinderParcel;

import com.appcloner.manager.CloneManager;
import com.appcloner.manager.SpoofConfig;
import com.appcloner.model.CloneProfile;
import com.appcloner.util.Logger;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class BinderInterceptor {

    private static final String TAG = "BinderInterceptor";
    private static BinderInterceptor instance;
    private boolean isInitialized = false;

    private static final String PACKAGE_MANAGER = "android.content.pm.IPackageManager";
    private static final String ACTIVITY_MANAGER = "android.app.IActivityManager";
    private static final String USER_MANAGER = "android.os.IUserManager";

    private static final int CODE_GET_PACKAGE_INFO = 10;
    private static final int CODE_GET_APPLICATION_INFO = 23;
    private static final int CODE_CHECK_PERMISSION = 27;
    private static final int CODE_GET_INSTALLED_PACKAGES = 56;

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
            BinderceptorManager.init();
            BinderceptorManager.setLogger(0);

            registerCallbacks();

            isInitialized = true;
            Logger.d(TAG, "Binder interceptor initialized successfully");
        } catch (Exception e) {
            Logger.e(TAG, "Failed to initialize binder interceptor", e);
        }
    }

    private void registerCallbacks() {
        try {
            BinderceptorManager.registerCallback(new IBinderceptorCallback.Stub() {
                @Override
                public void onBinderTransaction(String service, int code, byte[] data, byte[] reply) {
                    handleBinderTransaction(service, code, data, reply);
                }

                @Override
                public void onBinderOneway(String service, int code, byte[] data) {
                    handleBinderOneway(service, code, data);
                }
            });
        } catch (Exception e) {
            Logger.e(TAG, "Failed to register callbacks", e);
        }
    }

    private void handleBinderTransaction(String service, int code, byte[] data, byte[] reply) {
        try {
            if (service.contains("IPackageManager") || service.contains("PackageManager")) {
                handlePackageManagerTransaction(code, data, reply);
            } else if (service.contains("IActivityManager") || service.contains("ActivityManager")) {
                handleActivityManagerTransaction(code, data, reply);
            } else if (service.contains("IUserManager") || service.contains("UserManager")) {
                handleUserManagerTransaction(code, data, reply);
            }
        } catch (Exception e) {
            Logger.e(TAG, "Error handling binder transaction", e);
        }
    }

    private void handleBinderOneway(String service, int code, byte[] data) {
        try {
            if (service.contains("IPackageManager") || service.contains("PackageManager")) {
                handlePackageManagerOneway(code, data);
            } else if (service.contains("IActivityManager") || service.contains("ActivityManager")) {
                handleActivityManagerOneway(code, data);
            }
        } catch (Exception e) {
            Logger.e(TAG, "Error handling binder oneway", e);
        }
    }

    private void handlePackageManagerTransaction(int code, byte[] data, byte[] reply) {
        switch (code) {
            case CODE_GET_PACKAGE_INFO:
                spoofPackageInfo(data, reply);
                break;
            case CODE_GET_APPLICATION_INFO:
                spoofApplicationInfo(data, reply);
                break;
            case CODE_GET_INSTALLED_PACKAGES:
                spoofInstalledPackages(data, reply);
                break;
            default:
                break;
        }
    }

    private void handleActivityManagerTransaction(int code, byte[] data, byte[] reply) {
        switch (code) {
            case CODE_CHECK_PERMISSION:
                spoofPermissionCheck(data, reply);
                break;
            default:
                break;
        }
    }

    private void handleUserManagerTransaction(int code, byte[] data, byte[] reply) {
        // Handle user manager related spoofing
    }

    private void handlePackageManagerOneway(int code, byte[] data) {
        // Handle oneway package manager calls
    }

    private void handleActivityManagerOneway(int code, byte[] data) {
        // Handle oneway activity manager calls
    }

    private void spoofPackageInfo(byte[] data, byte[] reply) {
        if (activeProfiles.isEmpty()) {
            return;
        }

        String packageName = extractStringFromData(data);
        CloneProfile profile = findProfileForPackage(packageName);

        if (profile != null && !packageName.equals(profile.getClonedPackageName())) {
            Logger.d(TAG, "Spoofing package info: " + packageName + " -> " + profile.getClonedPackageName());

            modifyPackageInfoReply(reply, packageName, profile.getClonedPackageName());
        }
    }

    private void spoofApplicationInfo(byte[] data, byte[] reply) {
        if (activeProfiles.isEmpty()) {
            return;
        }

        String packageName = extractStringFromData(data);
        CloneProfile profile = findProfileForPackage(packageName);

        if (profile != null) {
            Logger.d(TAG, "Spoofing application info: " + packageName);
            modifyApplicationInfoReply(reply, profile);
        }
    }

    private void spoofInstalledPackages(byte[] data, byte[] reply) {
        if (activeProfiles.isEmpty()) {
            return;
        }

        Logger.d(TAG, "Filtering installed packages");
        filterInstalledPackagesReply(reply);
    }

    private void spoofPermissionCheck(byte[] data, byte[] reply) {
        // Spoof permission check results
    }

    private CloneProfile findProfileForPackage(String packageName) {
        for (Map.Entry<String, CloneProfile> entry : activeProfiles.entrySet()) {
            if (entry.getValue().getClonedPackageName().equals(packageName)) {
                return entry.getValue();
            }
        }

        CloneManager manager = CloneManager.getInstance();
        return manager.getCloneProfile(packageName);
    }

    private String extractStringFromData(byte[] data) {
        if (data == null || data.length < 8) {
            return "";
        }

        try {
            int offset = 8;
            if (data.length > offset + 4) {
                int length = readInt(data, offset);
                if (length > 0 && data.length >= offset + 4 + length * 2) {
                    char[] chars = new char[length];
                    for (int i = 0; i < length; i++) {
                        chars[i] = (char) ((data[offset + 4 + i * 2] & 0xFF) | ((data[offset + 4 + i * 2 + 1] & 0xFF) << 8));
                    }
                    return new String(chars);
                }
            }
        } catch (Exception e) {
            Logger.e(TAG, "Error extracting string from data", e);
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

    private void modifyPackageInfoReply(byte[] reply, String originalPackage, String spoofedPackage) {
        if (reply == null) {
            return;
        }

        try {
            byte[] spoofedBytes = encodeString(spoofedPackage);
            byte[] originalBytes = encodeString(originalPackage);

            for (int i = 0; i < reply.length - spoofedBytes.length; i++) {
                boolean match = true;
                for (int j = 0; j < originalBytes.length && match; j++) {
                    if (reply[i + j] != originalBytes[j]) {
                        match = false;
                    }
                }

                if (match) {
                    System.arraycopy(spoofedBytes, 0, reply, i, spoofedBytes.length);
                    Logger.d(TAG, "Modified package name in reply");
                    break;
                }
            }
        } catch (Exception e) {
            Logger.e(TAG, "Error modifying package info reply", e);
        }
    }

    private void modifyApplicationInfoReply(byte[] reply, CloneProfile profile) {
        if (reply == null) {
            return;
        }

        try {
            modifyPackageInfoReply(reply, profile.getClonedPackageName(), profile.getClonedPackageName());
        } catch (Exception e) {
            Logger.e(TAG, "Error modifying application info reply", e);
        }
    }

    private void filterInstalledPackagesReply(byte[] reply) {
        if (reply == null) {
            return;
        }

        Logger.d(TAG, "Filtering installed packages reply");
    }

    private byte[] encodeString(String str) {
        if (str == null) {
            str = "";
        }

        byte[] bytes = new byte[4 + str.length() * 2];
        bytes[0] = (byte) (str.length() & 0xFF);
        bytes[1] = (byte) ((str.length() >> 8) & 0xFF);
        bytes[2] = (byte) ((str.length() >> 16) & 0xFF);
        bytes[3] = (byte) ((str.length() >> 24) & 0xFF);

        for (int i = 0; i < str.length(); i++) {
            int charValue = str.charAt(i);
            bytes[4 + i * 2] = (byte) (charValue & 0xFF);
            bytes[4 + i * 2 + 1] = (byte) ((charValue >> 8) & 0xFF);
        }

        return bytes;
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
        int flags = enabled ?
            BinderceptorManager.EBinderceptorDemoFlag_Print_Simple |
            BinderceptorManager.EBinderceptorDemoFlag_Print_Transaction_Data :
            0;
        BinderceptorManager.setLogger(flags);
    }

    public void cleanup() {
        if (isInitialized) {
            activeProfiles.clear();
            isInitialized = false;
            Logger.d(TAG, "Binder interceptor cleaned up");
        }
    }
}
