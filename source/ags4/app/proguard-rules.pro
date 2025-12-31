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

-keep class * extends com.jiagu.api.usbserial.driver.UsbSerialDriver { *; }

# jts
-dontwarn org.locationtech.jts.**
-keep class org.locationtech.jts.** {*;}

# qxwz
-keep public class com.qxwz.sdk.core.** { *; }

# cmcc
-keep class com.cmcc.sy.hap.** { *; }

# skydroid
-keep class android.serialport.** {*;}
-keep class com.shenyaocn.android.** {*;}

# h16
-keep class com.fishsemi.sdk.** {*;}
-keep class com.fishsemi.aircontrol.** {*;}
-keep class android.telephony.** {*;}

-keep class com.jiagu.ags4.bean.** {*;}
-keep class com.jiagu.ags.repo.net.model.** {*;}

# This is generated automatically by the Android Gradle plugin.
-dontwarn com.google.android.gms.common.GoogleApiAvailability
-dontwarn com.google.android.gms.location.ActivityRecognition
-dontwarn com.google.android.gms.location.ActivityRecognitionClient
-dontwarn com.google.android.gms.location.ActivityRecognitionResult
-dontwarn com.google.android.gms.location.ActivityTransition
-dontwarn com.google.android.gms.location.ActivityTransition$Builder
-dontwarn com.google.android.gms.location.ActivityTransitionEvent
-dontwarn com.google.android.gms.location.ActivityTransitionRequest
-dontwarn com.google.android.gms.location.ActivityTransitionResult
-dontwarn com.google.android.gms.location.DetectedActivity
-dontwarn com.google.android.gms.location.FusedLocationProvider
-dontwarn com.google.android.gms.location.FusedLocationProviderClient
-dontwarn com.google.android.gms.location.LocationCallback
-dontwarn com.google.android.gms.location.LocationRequest
-dontwarn com.google.android.gms.location.LocationResult
-dontwarn com.google.android.gms.location.LocationServices
-dontwarn com.google.android.gms.tasks.OnCanceledListener
-dontwarn com.google.android.gms.tasks.OnFailureListener
-dontwarn com.google.android.gms.tasks.OnSuccessListener
-dontwarn com.google.android.gms.tasks.RuntimeExecutionException
-dontwarn com.google.android.gms.tasks.Task