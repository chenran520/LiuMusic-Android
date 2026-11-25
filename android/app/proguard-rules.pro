# JavaScript bridges are invoked by name from bundled WebView assets.
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}

# JSON/domain objects are decoded explicitly; retain enum names used in persisted settings.
-keepclassmembers enum com.liuguanyi.liumusic.** {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Kotlin file-level static functions are resolved by R8 as *Kt classes.
# Retain the ones that are referenced across module boundaries or via companion objects.
-keep class com.liuguanyi.liumusic.core.account.NeteaseSessionStoreKt { *; }
-keep class com.liuguanyi.liumusic.core.provider.qqmusic.QQMusicSessionStoreKt { *; }
-keep class com.liuguanyi.liumusic.core.provider.kugou.KugouSessionStoreKt { *; }
-keep class com.liuguanyi.liumusic.core.provider.qqmusic.QQMusicApiClientKt { *; }
-keep class com.liuguanyi.liumusic.core.network.NeteaseSearchClientKt { *; }

# Shizuku and optional vendor bridges are discovered through Android framework metadata.
-keep class rikka.shizuku.** { *; }
-dontwarn rikka.shizuku.**
-keep class com.liuguanyi.liumusic.platform.xiaomi.ShizukuKeepAliveService { *; }
-keep class org.lsposed.hiddenapibypass.** { *; }
-dontwarn org.lsposed.hiddenapibypass.**
-dontwarn com.apple.android.music.**

## Rules for NewPipeExtractor
-keep class org.mozilla.javascript.** { *; }
-keep class org.mozilla.classfile.ClassFileWriter
-dontwarn org.mozilla.javascript.tools.**

# Rhino JS engine references desktop APIs that are unavailable on Android.
-dontwarn java.beans.BeanDescriptor
-dontwarn java.beans.BeanInfo
-dontwarn java.beans.IntrospectionException
-dontwarn java.beans.Introspector
-dontwarn java.beans.PropertyDescriptor
-dontwarn javax.script.AbstractScriptEngine
-dontwarn javax.script.Bindings
-dontwarn javax.script.Compilable
-dontwarn javax.script.CompiledScript
-dontwarn javax.script.Invocable
-dontwarn javax.script.ScriptContext
-dontwarn javax.script.ScriptEngine
-dontwarn javax.script.ScriptEngineFactory
-dontwarn javax.script.ScriptException
-dontwarn javax.script.SimpleBindings
-dontwarn jdk.dynalink.**
# ── 液态玻璃（Kyant0/AndroidLiquidGlass）──────────────────────────────────
#
# 这套库的渲染核心是 **AGSL RuntimeShader 源码字符串** + **反射式的合成层调用**。
# R8 在 release 构建里会：
#   ① 剥离「看起来没被引用」的 shader 常量字符串；
#   ② 重命名 Backdrop / LayerBackdrop 家族里被反射访问的成员。
# 一旦发生，glass 就在**只有 release 包**里失效 —— debug 正常、release 变实心，
# 是这个问题最典型的症状（0.6.8 就是这样坏掉的）。
#
# 这几条 keep 保证：类名、成员名、以及 shader 相关字符串都原样保留。
-keep class com.kyant.backdrop.** { *; }
-keep class com.kyant.shapes.** { *; }
-keep class com.kyant.capsule.** { *; }
-dontwarn com.kyant.backdrop.**
-dontwarn com.kyant.shapes.**
-dontwarn com.kyant.capsule.**

# 保留本项目的玻璃封装层，避免 liuMusicGlassAvailable 等入口被内联/裁剪后门控失效。
-keep class com.liuguanyi.liumusic.ui.glass.** { *; }
