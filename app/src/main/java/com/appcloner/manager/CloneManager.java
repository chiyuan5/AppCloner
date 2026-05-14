package com.appcloner.manager;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.UserHandle;
import android.os.UserManager;

import com.appcloner.model.AppInfo;
import com.appcloner.model.CloneProfile;
import com.appcloner.util.Logger;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CloneManager {

    private static final String TAG = "CloneManager";
    private static final String CLONE_DATA_FILE = "clone_profiles.dat";
    private static final int BASE_USER_ID = 10000;

    private static CloneManager instance;
    private Context context;
    private SpoofConfig spoofConfig;
    private Map<String, CloneProfile> cloneProfiles;
    private Map<String, Integer> packageToUserId;
    private Gson gson;

    private CloneManager() {
        cloneProfiles = new HashMap<>();
        packageToUserId = new HashMap<>();
        gson = new Gson();
    }

    public static CloneManager getInstance() {
        if (instance == null) {
            synchronized (CloneManager.class) {
                if (instance == null) {
                    instance = new CloneManager();
                }
            }
        }
        return instance;
    }

    public void init(Context context) {
        this.context = context.getApplicationContext();
        this.spoofConfig = new SpoofConfig(context);
        loadProfiles();
        Logger.d(TAG, "CloneManager initialized with " + cloneProfiles.size() + " profiles");
    }

    public void cleanup() {
        saveProfiles();
    }

    private void loadProfiles() {
        try {
            File file = new File(context.getFilesDir(), CLONE_DATA_FILE);
            if (file.exists()) {
                byte[] bytes = new byte[(int) file.length()];
                try (java.io.FileInputStream fis = new java.io.FileInputStream(file)) {
                    fis.read(bytes);
                }
                String json = new String(bytes);
                Type type = new TypeToken<Map<String, CloneProfile>>(){}.getType();
                cloneProfiles = gson.fromJson(json, type);
                if (cloneProfiles == null) {
                    cloneProfiles = new HashMap<>();
                }
            }
        } catch (Exception e) {
            Logger.e(TAG, "Failed to load profiles", e);
            cloneProfiles = new HashMap<>();
        }
    }

    private void saveProfiles() {
        try {
            File file = new File(context.getFilesDir(), CLONE_DATA_FILE);
            String json = gson.toJson(cloneProfiles);
            try (FileOutputStream fos = new FileOutputStream(file)) {
                fos.write(json.getBytes());
            }
        } catch (Exception e) {
            Logger.e(TAG, "Failed to save profiles", e);
        }
    }

    public List<AppInfo> getInstalledApps(boolean includeSystemApps) {
        List<AppInfo> appList = new ArrayList<>();
        PackageManager pm = context.getPackageManager();

        List<PackageInfo> packages = pm.getInstalledPackages(0);
        for (PackageInfo packageInfo : packages) {
            if ((packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0 || includeSystemApps) {
                AppInfo appInfo = createAppInfo(packageInfo, pm);
                appList.add(appInfo);
            }
        }

        return appList;
    }

    public List<AppInfo> getClonedApps() {
        List<AppInfo> appList = new ArrayList<>();
        PackageManager pm = context.getPackageManager();

        for (Map.Entry<String, CloneProfile> entry : cloneProfiles.entrySet()) {
            CloneProfile profile = entry.getValue();
            try {
                PackageInfo packageInfo = pm.getPackageInfo(profile.getClonedPackageName(), 0);
                AppInfo appInfo = createAppInfo(packageInfo, pm);
                appInfo.setCloned(true);
                appInfo.setClonedPackageName(profile.getClonedPackageName());
                appList.add(appInfo);
            } catch (PackageManager.NameNotFoundException e) {
                Logger.w(TAG, "Cloned package not found: " + profile.getClonedPackageName());
            }
        }

        return appList;
    }

    private AppInfo createAppInfo(PackageInfo packageInfo, PackageManager pm) {
        AppInfo appInfo = new AppInfo();
        appInfo.setPackageName(packageInfo.packageName);
        appInfo.setAppName(packageInfo.applicationInfo.loadLabel(pm).toString());
        appInfo.setVersionName(packageInfo.versionName);
        appInfo.setVersionCode(packageInfo.versionCode);
        appInfo.setIcon(packageInfo.applicationInfo.loadIcon(pm));
        appInfo.setSystemApp((packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0);
        appInfo.setInstalledTime(packageInfo.firstInstallTime);
        appInfo.setUpdatedTime(packageInfo.lastUpdateTime);

        String clonedPkg = getClonedPackageName(packageInfo.packageName);
        appInfo.setCloned(clonedPkg != null);
        appInfo.setClonedPackageName(clonedPkg);

        return appInfo;
    }

    public CloneProfile createCloneProfile(String packageName) {
        if (cloneProfiles.containsKey(packageName)) {
            return cloneProfiles.get(packageName);
        }

        CloneProfile profile = spoofConfig.generateProfile(packageName);
        int userId = allocateUserId(packageName);
        profile.setUserId(userId);

        cloneProfiles.put(packageName, profile);
        saveProfiles();

        Logger.d(TAG, "Created clone profile for " + packageName + " with user ID " + userId);
        return profile;
    }

    public CloneProfile getCloneProfile(String packageName) {
        return cloneProfiles.get(packageName);
    }

    public String getClonedPackageName(String originalPackageName) {
        CloneProfile profile = cloneProfiles.get(originalPackageName);
        return profile != null ? profile.getClonedPackageName() : null;
    }

    public boolean isAppCloned(String packageName) {
        return cloneProfiles.containsKey(packageName);
    }

    public void removeCloneProfile(String packageName) {
        CloneProfile profile = cloneProfiles.remove(packageName);
        if (profile != null) {
            Integer userId = packageToUserId.remove(packageName);
            saveProfiles();
            Logger.d(TAG, "Removed clone profile for " + packageName);
        }
    }

    private int allocateUserId(String packageName) {
        int nextUserId = BASE_USER_ID;
        while (packageToUserId.containsValue(nextUserId)) {
            nextUserId++;
        }
        packageToUserId.put(packageName, nextUserId);
        return nextUserId;
    }

    public boolean startClonedApp(String packageName) {
        CloneProfile profile = cloneProfiles.get(packageName);
        if (profile == null) {
            Logger.w(TAG, "No profile found for " + packageName);
            return false;
        }

        try {
            Intent intent = context.getPackageManager().getLaunchIntentForPackage(profile.getClonedPackageName());
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
                return true;
            }
        } catch (Exception e) {
            Logger.e(TAG, "Failed to start cloned app", e);
        }
        return false;
    }

    public boolean uninstallClone(String packageName) {
        CloneProfile profile = cloneProfiles.get(packageName);
        if (profile == null) {
            return false;
        }

        try {
            Intent intent = new Intent(Intent.ACTION_DELETE);
            intent.setData(android.net.Uri.parse("package:" + profile.getClonedPackageName()));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);

            removeCloneProfile(packageName);
            return true;
        } catch (Exception e) {
            Logger.e(TAG, "Failed to uninstall clone", e);
            return false;
        }
    }

    public SpoofConfig getSpoofConfig() {
        return spoofConfig;
    }

    public int getCloneCount() {
        return cloneProfiles.size();
    }

    public void updateProfile(CloneProfile profile) {
        cloneProfiles.put(profile.getOriginalPackageName(), profile);
        spoofConfig.saveProfile(profile);
        saveProfiles();
    }
}
