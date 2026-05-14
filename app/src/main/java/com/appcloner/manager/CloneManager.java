package com.appcloner.manager;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;

import com.appcloner.model.AppInfo;
import com.appcloner.model.CloneProfile;
import com.appcloner.util.Logger;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CloneManager {

    private static final String TAG = "CloneManager";
    private static final String CLONE_DATA_FILE = "clone_profiles.dat";
    private static final String CLONE_DIR_NAME = "clone_apks";

    private static CloneManager instance;
    private Context context;
    private SpoofConfig spoofConfig;
    private Map<String, CloneProfile> cloneProfiles;
    private Gson gson;

    private CloneManager() {
        cloneProfiles = new HashMap<>();
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
                try (FileInputStream fis = new FileInputStream(file)) {
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
                if (!packageInfo.packageName.equals(context.getPackageName())) {
                    AppInfo appInfo = createAppInfo(packageInfo, pm);
                    appList.add(appInfo);
                }
            }
        }

        return appList;
    }

    public List<AppInfo> getClonedApps() {
        List<AppInfo> appList = new ArrayList<>();
        PackageManager pm = context.getPackageManager();

        for (Map.Entry<String, CloneProfile> entry : cloneProfiles.entrySet()) {
            CloneProfile profile = entry.getValue();
            if (profile.getClonedPackageName() != null) {
                try {
                    PackageInfo packageInfo = pm.getPackageInfo(profile.getClonedPackageName(), 0);
                    AppInfo appInfo = new AppInfo();
                    appInfo.setPackageName(entry.getKey());
                    appInfo.setAppName(packageInfo.applicationInfo.loadLabel(pm).toString());
                    appInfo.setVersionName(packageInfo.versionName);
                    appInfo.setVersionCode(packageInfo.versionCode);
                    appInfo.setIcon(packageInfo.applicationInfo.loadIcon(pm));
                    appInfo.setCloned(true);
                    appInfo.setClonedPackageName(profile.getClonedPackageName());
                    appList.add(appInfo);
                } catch (PackageManager.NameNotFoundException e) {
                    Logger.w(TAG, "Clone not installed: " + profile.getClonedPackageName());
                }
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
        CloneProfile profile;
        if (cloneProfiles.containsKey(packageName)) {
            profile = cloneProfiles.get(packageName);
        } else {
            profile = spoofConfig.generateProfile(packageName);
            cloneProfiles.put(packageName, profile);
            saveProfiles();
        }
        Logger.d(TAG, "Created clone profile for " + packageName);
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
        if (!cloneProfiles.containsKey(packageName)) {
            return false;
        }
        CloneProfile profile = cloneProfiles.get(packageName);
        if (profile == null || profile.getClonedPackageName() == null) {
            return false;
        }
        try {
            context.getPackageManager().getPackageInfo(profile.getClonedPackageName(), 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    public void removeCloneProfile(String packageName) {
        cloneProfiles.remove(packageName);
        saveProfiles();
        Logger.d(TAG, "Removed clone profile for " + packageName);
    }

    public boolean startClonedApp(String packageName) {
        CloneProfile profile = cloneProfiles.get(packageName);
        if (profile == null || profile.getClonedPackageName() == null) {
            Logger.w(TAG, "No profile found for " + packageName);
            return false;
        }

        try {
            PackageManager pm = context.getPackageManager();
            Intent launchIntent = pm.getLaunchIntentForPackage(profile.getClonedPackageName());
            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(launchIntent);
                Logger.d(TAG, "Started cloned app: " + profile.getClonedPackageName());
                return true;
            }
        } catch (Exception e) {
            Logger.e(TAG, "Failed to start cloned app", e);
        }
        return false;
    }

    public Intent getCloneInstallIntent(String packageName) {
        CloneProfile profile = cloneProfiles.get(packageName);
        if (profile == null) {
            return null;
        }

        try {
            PackageManager pm = context.getPackageManager();
            PackageInfo sourcePkg = pm.getPackageInfo(packageName, 0);
            
            File sourceApk = new File(sourcePkg.applicationInfo.sourceDir);
            File cloneDir = new File(context.getFilesDir(), CLONE_DIR_NAME);
            if (!cloneDir.exists()) {
                cloneDir.mkdirs();
            }
            
            File destApk = new File(cloneDir, profile.getClonedPackageName() + ".apk");
            
            copyFile(sourceApk, destApk);
            
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.fromFile(destApk), "application/vnd.android.package-archive");
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.GRANT_READ_URI_PERMISSION);
            
            Logger.d(TAG, "Prepared clone APK: " + destApk.getAbsolutePath());
            return intent;
            
        } catch (Exception e) {
            Logger.e(TAG, "Failed to prepare clone APK", e);
            return null;
        }
    }

    private void copyFile(File source, File dest) throws Exception {
        try (InputStream in = new FileInputStream(source);
             OutputStream out = new FileOutputStream(dest)) {
            byte[] buffer = new byte[65536];
            int length;
            while ((length = in.read(buffer)) > 0) {
                out.write(buffer, 0, length);
            }
        }
    }

    public boolean uninstallClone(String packageName) {
        CloneProfile profile = cloneProfiles.get(packageName);
        if (profile == null || profile.getClonedPackageName() == null) {
            return false;
        }

        try {
            Intent intent = new Intent(Intent.ACTION_DELETE);
            intent.setData(Uri.parse("package:" + profile.getClonedPackageName()));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
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
        if (profile != null && profile.getOriginalPackageName() != null) {
            cloneProfiles.put(profile.getOriginalPackageName(), profile);
            spoofConfig.saveProfile(profile);
            saveProfiles();
        }
    }
}
