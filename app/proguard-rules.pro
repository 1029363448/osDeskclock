# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in E:\work\android\tools\sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

-optimizationpasses 5

-dontusemixedcaseclassnames

-dontskipnonpubliclibraryclasses

-dontoptimize

-dontpreverify

-verbose

-dontwarn com.android.deskclock.alarms.**
-dontwarn android.os.**

-keepattributes *Annotation*

-keepattributes SourceFile,LineNumberTable

-keep class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator *;
}
# Preference objects are inflated via reflection
-keep public class android.support.v7.widget.SearchView {
    public <init>(android.content.Context);
}

-keep public class android.os.SystemProperties
-keep public class android.os.SystemProperties { *;}
-keep public class android.app.StatusBarManager
-keep public class android.app.StatusBarManager { *;}

#-keepclassmembers class android.os.SystemProperties { *;}
-keep public class android.support.v7.** { *;}

-keep public class com.android.deskclock.alarms.PowerOffAlarmPlatformMTK { *;}
-keep public class com.android.deskclock.NumberPickerCompat
-keep public class com.ape.util.** { *;}
-keepclassmembers class com.ape.util.ApeConfigParser { *;}
-keep public class com.ape.util.ApeConfigParser
-keep public class com.myos.** {*;}

-keep public class myos.widget.** {*;}

-dontwarn android.support.**

-ignorewarning