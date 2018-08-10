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
-renamesourcefileattribute SourceFile

-keep class com.tencent.**{*;}
-keep class okhttp3.**{*;}
-keep class okio.**{*;}
-keep class retrofit2.** { *; }
-keep class com.google.** { *; }
-keep class io.reactivex.**{*;}
-keep class com.squareup.**{*;}
-keep class org.reactivestreams.**{*;}
-keep class javax.annotation.**{*;}
-keep class org.conscrypt.**{*;}
-keep class com.eeseetech.nagrand.entity.**{*;}
-dontwarn javax.annotation.**
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn retrofit2.**