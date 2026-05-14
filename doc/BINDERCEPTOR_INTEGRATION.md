# Binderceptor 集成开发指南

## 概述

本项目基于 binderceptor 库实现 Android 应用克隆和伪装功能。本文档详细介绍 binderceptor 的集成方法和使用技巧。

## Binderceptor 简介

Binderceptor 是 Android 平台下的底层 Binder 拦截器框架，具有以下特点：

- 支持 Android 6.x ~ 14.x 系统版本
- 支持所有 Binder 通信拦截
- 支持 Binder 通信日志打印
- 支持 Binder 通信特征数据过滤
- 支持 Binder 通信协议拦截/修改/伪装

## 编译 binderceptor

### 环境准备

```bash
# 安装 Python
python --version  # 2.7.x 或 3.x

# 安装 Android NDK
export ANDROID_NDK_HOME=/path/to/ndk
```

### 编译命令

```bash
cd binderceptor
python mk.py
```

### 编译产物

```
out/
├── debug/
│   ├── app.apk
│   ├── cmpt-mts-binderceptor.aar    # Java SDK
│   └── libs/
│       ├── arm64-v8a/
│       │   └── libifmabinderceptor-jni.so
│       └── armeabi-v7a/
│           └── libifmabinderceptor-jni.so
└── release/
    └── ...
```

## 集成步骤

### 1. 添加依赖

将 `cmpt-mts-binderceptor.aar` 复制到 `app/libs/` 目录：

```gradle
dependencies {
    implementation(name: 'cmpt-mts-binderceptor', ext: 'aar')
}
```

### 2. 添加 Native 库

将编译生成的 `.so` 文件复制到 `app/libs/` 对应目录：

```
app/libs/
├── arm64-v8a/
│   └── libifmabinderceptor-jni.so
├── armeabi-v7a/
│   └── libifmabinderceptor-jni.so
└── cmpt-mts-binderceptor.aar
```

### 3. 配置 build.gradle

```gradle
android {
    sourceSets {
        main {
            jniLibs.srcDirs = ['libs']
        }
    }

    packagingOptions {
        pickFirst '**/*.so'
    }
}
```

## API 使用

### 初始化

```java
import android.app.ifma.mts.binderceptor.BinderceptorManager;

public class MyApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        // 初始化 Binderceptor
        BinderceptorManager.init();
        BinderceptorManager.setLogger(0);

        Logger.d("Binderceptor initialized successfully");
    }
}
```

### 日志配置

```java
// 简单日志 - 包含 Binder 服务名称和方法 Code 值
BinderceptorManager.setLogger(
    BinderceptorManager.EBinderceptorDemoFlag_Print_Simple
);

// 详细日志 - 包含 txn 完整数据结构
BinderceptorManager.setLogger(
    BinderceptorManager.EBinderceptorDemoFlag_Print_Simple |
    BinderceptorManager.EBinderceptorDemoFlag_Print_Transaction_Data
);

// 完整日志 - 包含第一层 Binder 通信命令
BinderceptorManager.setLogger(
    BinderceptorManager.EBinderceptorDemoFlag_Print_Simple |
    BinderceptorManager.EBinderceptorDemoFlag_Print_Transaction_Data |
    BinderceptorManager.EBinderceptorDemoFlag_Print_Write_Read
);
```

### 注册回调

```java
IBinderceptorCallback callback = new IBinderceptorCallback.Stub() {
    @Override
    public void onBinderTransaction(String service, int code,
                                    byte[] data, byte[] reply) {
        // 处理同步 Binder 调用
        if (service.contains("IPackageManager")) {
            handlePackageManagerCall(code, data, reply);
        }
    }

    @Override
    public void onBinderOneway(String service, int code, byte[] data) {
        // 处理异步 Binder 调用
        if (service.contains("IActivityManager")) {
            handleActivityManagerCall(code, data);
        }
    }
};

BinderceptorManager.registerCallback(callback);
```

## 关键服务拦截

### IPackageManager

用于伪装包名、签名、应用信息等。

**常用方法码**：

| 方法 | Code | 描述 |
|------|------|------|
| getPackageInfo | 10 | 获取包信息 |
| getApplicationInfo | 23 | 获取应用信息 |
| getInstalledPackages | 56 | 获取已安装包列表 |
| checkSignaturePermission | 27 | 检查签名权限 |

**数据解析示例**：

```java
private String extractPackageName(byte[] data) {
    // Binder 数据格式：header + strict_mode + token + string16
    int offset = 8;  // header + strict_mode + token
    int length = readInt(data, offset);
    return readString16(data, offset + 4, length);
}
```

### IActivityManager

用于伪装进程信息、活动栈等。

**常用方法码**：

| 方法 | Code | 描述 |
|------|------|------|
| getActivityInfo | 1 | 获取活动信息 |
| startActivity | 2 | 启动活动 |
| getTask | 18 | 获取任务信息 |

### IUserManager

用于伪装用户信息。

**常用方法码**：

| 方法 | Code | 描述 |
|------|------|------|
| getUserHandle | 28 | 获取用户句柄 |
| getUserName | 29 | 获取用户名 |

## 伪装实现

### 包名映射

```java
private final Map<String, CloneProfile> spoofingRules = new HashMap<>();

public String getSpoofedPackageName(String originalPackage) {
    CloneProfile profile = spoofingRules.get(originalPackage);
    if (profile != null) {
        return profile.getClonedPackageName();
    }
    return originalPackage;
}
```

### 设备信息生成

```java
public static String generateFakeDeviceId() {
    // 生成 15 位设备 ID
    return "15" + String.format("%014d",
        (long)(Math.random() * 100000000000000L));
}

public static String generateFakeAndroidId() {
    // 生成 16 位十六进制 Android ID
    return String.format("%016x",
        (long)(Math.random() * 0xFFFFFFFFFFFFL));
}

public static String generateFakeSerial() {
    // 生成 16 位序列号
    String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < 16; i++) {
        sb.append(chars.charAt((int)(Math.random() * chars.length())));
    }
    return sb.toString();
}

public static String generateFakeImei() {
    // 生成 15 位 IMEI（含校验位）
    StringBuilder sb = new StringBuilder("35");
    sb.append(String.format("%02d", (int)(Math.random() * 100)));
    sb.append(String.format("%08d", (long)(Math.random() * 100000000)));
    sb.append(calculateLuhn(sb.toString()));
    return sb.toString();
}

private static int calculateLuhn(String number) {
    int sum = 0;
    boolean alternate = true;
    for (int i = number.length() - 1; i >= 0; i--) {
        int n = Character.getNumericValue(number.charAt(i));
        if (alternate) {
            n *= 2;
            if (n > 9) n -= 9;
        }
        sum += n;
        alternate = !alternate;
    }
    return (10 - (sum % 10)) % 10;
}
```

### 签名伪装

```java
private void handleSignatureCheck(int code, byte[] data, byte[] reply) {
    // 当检测到签名验证请求时
    if (code == CODE_CHECK_SIGNATURE_PERMISSION) {
        // 返回成功或自定义签名信息
        modifyReplyForSignatureSpoof(reply);
    }
}
```

## 调试技巧

### 启用日志

```java
BinderceptorManager.setLogger(
    BinderceptorManager.EBinderceptorDemoFlag_Print_Simple |
    BinderceptorManager.EBinderceptorDemoFlag_Print_Transaction_Data
);
```

### 过滤特定包

```java
static uint16_t g_debug_target[] = {'c','o','m','.','d','e','m','o'};
static const uint32_t g_debug_target_length = 8;

if (containsTargetString(data, g_debug_target, g_debug_target_length)) {
    // 打印日志
}
```

### 常见问题

1. **编译失败**：确保 NDK 版本正确，SO 库路径配置无误
2. **初始化失败**：检查 SO 库是否正确加载
3. **拦截无效**：确认回调注册成功，数据格式正确
4. **应用闪退**：检查权限配置，处理空指针异常

## 性能优化

1. **减少日志输出**：生产环境关闭详细日志
2. **异步处理**：拦截回调中避免耗时操作
3. **缓存策略**：复用解析后的数据
4. **条件拦截**：只对目标应用启用拦截

## 最佳实践

1. **延迟初始化**：在应用启动后延迟初始化 Binderceptor
2. **按需激活**：只对需要伪装的包启用拦截
3. **优雅降级**：拦截失败时不影响应用正常功能
4. **安全存储**：敏感配置加密存储

## 参考资料

- [binderceptor GitHub](https://github.com/iofomo/binderceptor)
- [Binder 通信机制](https://blog.csdn.net/chendianbo/article/details/134719327)
- [Android 底层 Binder 拦截技术](https://www.iofomo.com/docs/mobile/binderceptor/Introduce/)
