# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.kts.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Keep Compose and related (common for modern apps)
-keep class androidx.compose.** { *; }
-keep class androidx.compose.ui.** { *; }

# Room
-keep class com.pixelsnap.app.data.** { *; }

# CameraX (keep use cases and callbacks)
-keep class androidx.camera.** { *; }

# Serialization (kotlinx)
-keep class kotlinx.serialization.** { *; }
-keepclassmembers class ** {
    *** Companion;
}
-keepclasseswithmembers class ** {
    kotlinx.serialization.KSerializer serializer(...);
}

# If using default Android rules + R8, most things are covered.
# Add any app-specific keeps here if you see shrinking issues in release.