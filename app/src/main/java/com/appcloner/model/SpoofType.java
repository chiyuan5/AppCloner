package com.appcloner.model;

public enum SpoofType {
    PACKAGE_NAME("package_name"),
    APP_NAME("app_name"),
    VERSION("version"),
    SIGNATURE("signature"),
    DEVICE_ID("device_id"),
    ANDROID_ID("android_id"),
    SERIAL("serial"),
    IMEI("imei"),
    IMSI("imsi"),
    MAC_ADDRESS("mac_address"),
    BOARD("board"),
    HARDWARE("hardware"),
    MANUFACTURER("manufacturer"),
    MODEL("model"),
    BRAND("brand"),
    FINGERPRINT("fingerprint"),
    USER_ID("user_id"),
    USER_HANDLE("user_handle");

    private final String value;

    SpoofType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static SpoofType fromValue(String value) {
        for (SpoofType type : values()) {
            if (type.value.equals(value)) {
                return type;
            }
        }
        return null;
    }
}
