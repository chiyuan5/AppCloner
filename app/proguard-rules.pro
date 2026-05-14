# AppCloner ProGuard 规则

-keepattributes *Annotation*
-keepattributes SourceFile,LineNumberTable
-keep public class * extends java.lang.Exception

# Gson
-keepattributes Signature
-keep class com.google.gson.** { *; }
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# 应用数据模型
-keep class com.appcloner.model.** { *; }

# Binderceptor
-keep class android.app.ifma.mts.binderceptor.** { *; }
