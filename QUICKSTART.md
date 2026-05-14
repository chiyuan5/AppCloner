# 快速入门指南

## 前置要求

1. **Android Studio 4.2+**
2. **Android SDK** (API 29-33)
3. **Android NDK 21.4.7075529+**
4. **Python 2.7.x 或 3.x** (用于编译 binderceptor)
5. **Git**

## 步骤 1: 克隆并编译 binderceptor

```bash
# 克隆 binderceptor 仓库
git clone https://github.com/iofomo/binderceptor.git

# 进入目录
cd binderceptor

# 编译 (需要 Python 环境)
python mk.py

# 编译产物在 out/ 目录
ls out/debug/
```

## 步骤 2: 获取编译产物

从 `binderceptor/out/debug/` 复制以下文件到 `AppCloner/app/libs/`:

```
AppCloner/app/libs/
├── arm64-v8a/
│   └── libifmabinderceptor-jni.so
├── armeabi-v7a/
│   └── libifmabinderceptor-jni.so
└── cmpt-mts-binderceptor.aar
```

## 步骤 3: 在 Android Studio 中打开项目

```bash
cd AppCloner
open android-studio .
```

等待 Gradle 同步完成。

## 步骤 4: 编译并运行

在 Android Studio 中:
1. 选择 `app` 模块
2. 点击 `Run` → `Run 'app'`
3. 选择连接的设备或模拟器

或者使用命令行:

```bash
./gradlew assembleDebug
./gradlew installDebug
```

## 项目结构

```
AppCloner/
├── app/
│   └── src/main/
│       ├── java/com/appcloner/
│       │   ├── AppClonerApplication.java  # 应用入口
│       │   ├── adapter/                   # RecyclerView 适配器
│       │   ├── binder/                    # Binder 拦截核心
│       │   ├── manager/                   # 业务管理类
│       │   ├── model/                     # 数据模型
│       │   ├── receiver/                  # 广播接收器
│       │   ├── service/                   # 后台服务
│       │   ├── ui/                        # 界面 Activity
│       │   └── util/                      # 工具类
│       └── res/                           # 资源文件
├── build.gradle                           # 项目配置
└── gradle.properties                       # Gradle 属性
```

## 核心文件说明

| 文件 | 功能 |
|------|------|
| `CloneManager.java` | 应用克隆核心逻辑 |
| `SpoofConfig.java` | 伪装配置管理 |
| `BinderInterceptor.java` | Binder 拦截器 |
| `AdvancedBinderInterceptor.java` | 高级拦截实现 |
| `MainActivity.java` | 主界面 |
| `CloneDetailActivity.java` | 克隆详情/设置 |

## 使用说明

1. **打开应用**: 查看已安装应用列表
2. **创建克隆**: 点击应用 → "Clone"
3. **配置伪装**: 管理 → 选择克隆 → 修改伪装设置
4. **启动克隆**: 点击 "Start" 按钮

## 调试日志

如需查看 Binder 拦截日志:

```java
// 在 SettingsActivity 中开启
BinderceptorManager.setLogger(
    BinderceptorManager.EBinderceptorDemoFlag_Print_Simple |
    BinderceptorManager.EBinderceptorDemoFlag_Print_Transaction_Data
);
```

## 常见问题

**Q: 编译失败找不到 binderceptor**
A: 确保已将 `cmpt-mts-binderceptor.aar` 复制到 `app/libs/` 目录

**Q: 应用启动崩溃**
A: 检查 AndroidManifest 中的权限配置，确保包含 `QUERY_ALL_PACKAGES`

**Q: 克隆应用无法启动**
A: 检查目标应用是否支持克隆，部分系统应用有保护机制

## 下一步

- 阅读 [BINDERCEPTOR_INTEGRATION.md](doc/BINDERCEPTOR_INTEGRATION.md) 了解详细集成方法
- 阅读 [README.md](README.md) 了解项目整体架构
- 查看代码注释了解具体实现细节
