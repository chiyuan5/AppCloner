package com.appcloner.model;

public class CloneProfile {
    private String originalPackageName;
    private String clonedPackageName;
    private String clonedAppName;
    private int userId;
    private long createdTime;
    private String fakeDeviceId;
    private String fakeAndroidId;
    private String fakeSerial;
    private String fakeImei;
    private boolean hideIcon;
    private boolean spoofSignature;

    public CloneProfile() {
        this.createdTime = System.currentTimeMillis();
    }

    public String getOriginalPackageName() {
        return originalPackageName;
    }

    public void setOriginalPackageName(String originalPackageName) {
        this.originalPackageName = originalPackageName;
    }

    public String getClonedPackageName() {
        return clonedPackageName;
    }

    public void setClonedPackageName(String clonedPackageName) {
        this.clonedPackageName = clonedPackageName;
    }

    public String getClonedAppName() {
        return clonedAppName;
    }

    public void setClonedAppName(String clonedAppName) {
        this.clonedAppName = clonedAppName;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public long getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(long createdTime) {
        this.createdTime = createdTime;
    }

    public String getFakeDeviceId() {
        return fakeDeviceId;
    }

    public void setFakeDeviceId(String fakeDeviceId) {
        this.fakeDeviceId = fakeDeviceId;
    }

    public String getFakeAndroidId() {
        return fakeAndroidId;
    }

    public void setFakeAndroidId(String fakeAndroidId) {
        this.fakeAndroidId = fakeAndroidId;
    }

    public String getFakeSerial() {
        return fakeSerial;
    }

    public void setFakeSerial(String fakeSerial) {
        this.fakeSerial = fakeSerial;
    }

    public String getFakeImei() {
        return fakeImei;
    }

    public void setFakeImei(String fakeImei) {
        this.fakeImei = fakeImei;
    }

    public boolean isHideIcon() {
        return hideIcon;
    }

    public void setHideIcon(boolean hideIcon) {
        this.hideIcon = hideIcon;
    }

    public boolean isSpoofSignature() {
        return spoofSignature;
    }

    public void setSpoofSignature(boolean spoofSignature) {
        this.spoofSignature = spoofSignature;
    }
}
