# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Optimization is turned on by default with proguard-android-optimize.txt
# This defaults to optimizing for file size (which also improves performance)

# Keep classes used by DRM implementation
-keep class com.example.telepartyproject.model.** { *; }

# Keep API and service classes
-keepclassmembers class com.example.telepartyproject.service.** {
    <fields>;
}

# Security-specific rules
-keepattributes SourceFile,LineNumberTable
-keepattributes *Annotation*
-keepattributes Signature
-keepattributes Exceptions

# Remove the dictionary references - we'll handle obfuscation with the default
# -obfuscationdictionary dictionary.txt
# -classobfuscationdictionary dictionary.txt
# -packageobfuscationdictionary dictionary.txt

# Keep Media3/ExoPlayer classes
-keep class androidx.media3.** { *; }

# Keep JSON classes
-keep class org.json.** { *; }

# Rules for AndroidX Security Library
-keep class androidx.security.crypto.** { *; }

# Rules for Play Integrity API
-keep class com.google.android.play.core.integrity.** { *; }

# Remove logging in release builds
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int d(...);
    public static int i(...);
}

# Obscure method names that relate to security
-keepclasseswithmembers class * {
    @androidx.annotation.Keep <methods>;
}

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