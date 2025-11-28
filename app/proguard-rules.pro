# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# --- VOSK RULES (PENTING) ---
# Jaga agar kelas Vosk tidak diubah namanya karena dipanggil oleh Native C++
-keep class org.vosk.** { *; }
-keep interface org.vosk.** { *; }
-keep class com.sun.jna.** { *; }

# --- FIREBASE & GSON RULES (Jaga-jaga) ---
-keep class com.google.firebase.** { *; }
-keep class com.google.gson.** { *; }

# --- COROUTINES ---
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.coroutines.android.AndroidExceptionPreHandler {
    <init>();
}