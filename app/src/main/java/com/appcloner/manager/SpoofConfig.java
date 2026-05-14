package com.appcloner.manager;

import com.appcloner.model.CloneProfile;
import com.appcloner.model.SpoofType;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SpoofConfig {

    private static final String PREFS_NAME = "spoof_config";
    private static final String KEY_DEVICE_CONFIGS = "device_configs";

    private final Context context;
    private final SharedPreferences prefs;
    private final Gson gson;
    private Map<String, Map<String, String>> deviceConfigs;

    public SpoofConfig(Context context) {
        this.context = context;
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.gson = new Gson();
        loadConfigs();
    }

    private void loadConfigs() {
        String json = prefs.getString(KEY_DEVICE_CONFIGS, null);
        if (json != null) {
            Type type = new TypeToken<Map<String, Map<String, String>>>(){}.getType();
            deviceConfigs = gson.fromJson(json, type);
        } else {
            deviceConfigs = new HashMap<>();
        }
    }

    private void saveConfigs() {
        String json = gson.toJson(deviceConfigs);
        prefs.edit().putString(KEY_DEVICE_CONFIGS, json).apply();
    }

    public CloneProfile generateProfile(String originalPackageName) {
        CloneProfile profile = new CloneProfile();
        profile.setOriginalPackageName(originalPackageName);

        String fakePackageName = generateFakePackageName(originalPackageName);
        profile.setClonedPackageName(fakePackageName);

        profile.setFakeDeviceId(generateFakeDeviceId());
        profile.setFakeAndroidId(generateFakeAndroidId());
        profile.setFakeSerial(generateFakeSerial());
        profile.setFakeImei(generateFakeImei());

        profile.setSpoofSignature(true);
        profile.setHideIcon(false);

        return profile;
    }

    public String getSpoofedValue(String packageName, SpoofType type, String defaultValue) {
        Map<String, String> config = deviceConfigs.get(packageName);
        if (config != null && config.containsKey(type.getValue())) {
            return config.get(type.getValue());
        }
        return defaultValue;
    }

    public void setSpoofedValue(String packageName, SpoofType type, String value) {
        if (!deviceConfigs.containsKey(packageName)) {
            deviceConfigs.put(packageName, new HashMap<>());
        }
        deviceConfigs.get(packageName).put(type.getValue(), value);
        saveConfigs();
    }

    public CloneProfile getProfile(String packageName) {
        CloneProfile profile = new CloneProfile();
        profile.setOriginalPackageName(packageName);
        profile.setClonedPackageName(getSpoofedValue(packageName, SpoofType.PACKAGE_NAME, packageName));
        profile.setFakeDeviceId(getSpoofedValue(packageName, SpoofType.DEVICE_ID, generateFakeDeviceId()));
        profile.setFakeAndroidId(getSpoofedValue(packageName, SpoofType.ANDROID_ID, generateFakeAndroidId()));
        profile.setFakeSerial(getSpoofedValue(packageName, SpoofType.SERIAL, generateFakeSerial()));
        profile.setFakeImei(getSpoofedValue(packageName, SpoofType.IMEI, generateFakeImei()));
        return profile;
    }

    public void saveProfile(CloneProfile profile) {
        setSpoofedValue(profile.getOriginalPackageName(), SpoofType.PACKAGE_NAME, profile.getClonedPackageName());
        setSpoofedValue(profile.getOriginalPackageName(), SpoofType.DEVICE_ID, profile.getFakeDeviceId());
        setSpoofedValue(profile.getOriginalPackageName(), SpoofType.ANDROID_ID, profile.getFakeAndroidId());
        setSpoofedValue(profile.getOriginalPackageName(), SpoofType.SERIAL, profile.getFakeSerial());
        setSpoofedValue(profile.getOriginalPackageName(), SpoofType.IMEI, profile.getFakeImei());
    }

    private String generateFakePackageName(String originalPackageName) {
        String uuid = UUID.randomUUID().toString().substring(0, 8);
        if (originalPackageName.contains(".")) {
            String[] parts = originalPackageName.split("\\.");
            String lastPart = parts[parts.length - 1];
            String newLastPart = lastPart + uuid;
            return originalPackageName.replace("." + lastPart, "." + newLastPart);
        }
        return originalPackageName + uuid;
    }

    private String generateFakeDeviceId() {
        return "15" + String.format("%014d", (long)(Math.random() * 100000000000000L));
    }

    private String generateFakeAndroidId() {
        return String.format("%016x", (long)(Math.random() * 0xFFFFFFFFFFFFL));
    }

    private String generateFakeSerial() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 16; i++) {
            sb.append(chars.charAt((int)(Math.random() * chars.length())));
        }
        return sb.toString();
    }

    private String generateFakeImei() {
        StringBuilder sb = new StringBuilder("35");
        sb.append(String.format("%02d", (int)(Math.random() * 100)));
        sb.append(String.format("%08d", (long)(Math.random() * 100000000)));
        sb.append(calculateLuhn(sb.toString()));
        return sb.toString();
    }

    private int calculateLuhn(String number) {
        int sum = 0;
        boolean alternate = true;
        for (int i = number.length() - 1; i >= 0; i--) {
            int n = Character.getNumericValue(number.charAt(i));
            if (alternate) {
                n *= 2;
                if (n > 9) {
                    n -= 9;
                }
            }
            sum += n;
            alternate = !alternate;
        }
        return (10 - (sum % 10)) % 10;
    }

    public String getGlobalSpoofedValue(SpoofType type) {
        return getSpoofedValue("_global_", type, getDefaultSpoofedValue(type));
    }

    public void setGlobalSpoofedValue(SpoofType type, String value) {
        setSpoofedValue("_global_", type, value);
    }

    private String getDefaultSpoofedValue(SpoofType type) {
        switch (type) {
            case DEVICE_ID:
                return generateFakeDeviceId();
            case ANDROID_ID:
                return generateFakeAndroidId();
            case SERIAL:
                return generateFakeSerial();
            case IMEI:
                return generateFakeImei();
            case MANUFACTURER:
                return Build.MANUFACTURER;
            case MODEL:
                return Build.MODEL;
            case BRAND:
                return Build.BRAND;
            case BOARD:
                return Build.BOARD;
            case HARDWARE:
                return Build.HARDWARE;
            default:
                return "";
        }
    }
}
