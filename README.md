# AppCloner - 基于 Binderceptor 的安卓分身多开应用

## 项目概述

AppCloner 是一个基于 [binderceptor](https://github.com/iofomo/binderceptor) 库的 Android 应用分身/多开解决方案。该项目利用 binderceptor 提供的底层 Binder 拦截能力，实现对应用身份信息的完美伪装。

## 核心技术

### Binderceptor 集成

Binderceptor 是一个 Android 平台下的底层 Binder 拦截器框架，支持：
- 拦截所有 Binder 通信
- 修改 Binder 通信协议数据
- 实现完美的应用伪装

### 伪装策略

1. **包名伪装**：将原始包名映射为伪装包名
2. **设备信息伪装**：伪造 Device ID、IMEI、Serial 等
3. **签名伪装**：绕过签名验证
4. **用户空间隔离**：利用 Android 多用户机制

## 项目结构

```
AppCloner/
├── app/
│   └── src/main/
│       ├── java/com/appcloner/
│       │   ├── AppClonerApplication.java     # 应用入口
│       │   ├── adapter/                      # 列表适配器
│       │   ├── binder/                       # Binder 拦截器
│       │   │   ├── BinderInterceptor.java
│       │   │   └── AdvancedBinderInterceptor.java
│       │   ├── manager/                      # 核心管理类
│       │   │   ├── CloneManager.java         # 克隆管理
│       │   │   └── SpoofConfig.java          # 伪装配置
│       │   ├── model/                        # 数据模型
│       │   ├── receiver/                     # 广播接收器
│       │   ├── service/                      # 后台服务
│       │   ├── ui/                           # 界面
│       │   └── util/                         # 工具类
│       └── res/                              # 资源文件
├── build.gradle                              # 项目配置
└── gradle.properties                         # Gradle 配置
```

## 功能特性

### 1. 应用克隆
- 扫描已安装应用
- 一键创建克隆
- 独立数据空间

### 2. 完美伪装
- 自定义伪装包名
- 设备信息随机生成
- 签名验证绕过
- 图标名称修改

### 3. Binder 拦截
- IPackageManager 服务拦截
- IActivityManager 服务拦截
- IUserManager 服务拦截
- 自定义伪装规则

### 4. 管理功能
- 克隆应用列表
- 快速启动
- 批量管理
- 设置中心

## 快速开始

### 环境要求

- Android Studio 4.2+
- Gradle 6.9.2+
- NDK 21.4.7075529+
- Python 2.7.x/3.x (用于编译 binderceptor)

### 编译步骤

1. **克隆 binderceptor 仓库**：
```bash
git clone https://github.com/iofomo/binderceptor.git
```

2. **编译 binderceptor**：
```bash
cd binderceptor
python mk.py
```

3. **获取编译产物**：
- `cmpt-mts-binderceptor.aar` - Java SDK
- `libifmabinderceptor-jni.so` - Native 库

4. **集成到项目**：
将编译产物复制到 `AppCloner/app/libs` 目录

5. **构建应用**：
```bash
cd AppCloner
./gradlew assembleDebug
```

## API 使用

### 初始化 Binderceptor

```java
BinderceptorManager.init();
BinderceptorManager.setLogger(0);  // 关闭日志
```

### 创建克隆配置

```java
CloneProfile profile = SpoofConfig.generateProfile(originalPackageName);
profile.setClonedPackageName("com.example.clone");
profile.setFakeDeviceId("15xxxxxxxxxxxxxx");
```

### 激活克隆

```java
BinderInterceptor.getInstance().activateProfile(profile);
```

## 注意事项

1. **Root 权限**：部分功能可能需要 Root 权限
2. **系统版本**：推荐 Android 10-13
3. **安全风险**：请遵守相关法律法规
4. **性能影响**：Binder 拦截会带来一定性能开销

## 技术原理

### Binder 拦截流程

1. **初始化**：加载 native 库并注册拦截回调
2. **数据提取**：从 Binder 通信数据中提取关键信息
3. **规则匹配**：根据配置匹配伪装规则
4. **数据修改**：修改回复数据实现伪装
5. **结果返回**：返回伪装后的数据

### 关键服务拦截

| 服务 | 功能码 | 描述 |
|------|--------|------|
| IPackageManager | 10, 23, 56 | 包信息查询和安装 |
| IActivityManager | 1, 2, 18 | 活动和任务管理 |
| IUserManager | 28, 29 | 用户信息管理 |

## 许可证

本项目基于 MIT 许可证开源。

## 致谢

- [binderceptor](https://github.com/iofomo/binderceptor) - 底层 Binder 拦截框架
- Android Open Source Project

## 联系方式

如有问题或建议，请提交 Issue。
