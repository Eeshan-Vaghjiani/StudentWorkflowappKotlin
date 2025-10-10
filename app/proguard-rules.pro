# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Preserve line number information for debugging stack traces
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Keep generic signature for reflection
-keepattributes Signature
-keepattributes *Annotation*
-keepattributes EnclosingMethod

# ===========================
# Firebase Rules
# ===========================

# Keep all Firebase classes
-keep class com.google.firebase.** { *; }
-keep interface com.google.firebase.** { *; }
-dontwarn com.google.firebase.**

# Firebase Authentication
-keep class com.google.firebase.auth.** { *; }
-keepclassmembers class com.google.firebase.auth.** { *; }

# Firebase Firestore
-keep class com.google.firebase.firestore.** { *; }
-keepclassmembers class com.google.firebase.firestore.** { *; }
-keep class com.google.firestore.** { *; }

# Firebase Storage
-keep class com.google.firebase.storage.** { *; }
-keepclassmembers class com.google.firebase.storage.** { *; }

# Firebase Messaging (FCM)
-keep class com.google.firebase.messaging.** { *; }
-keepclassmembers class com.google.firebase.messaging.** { *; }
-keep class com.google.firebase.iid.** { *; }

# Firebase Analytics
-keep class com.google.firebase.analytics.** { *; }

# Google Play Services
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.android.gms.**
-keep class com.google.android.gms.auth.** { *; }
-keep class com.google.android.gms.common.** { *; }

# ===========================
# Data Model Classes
# ===========================

# Keep all data model classes and their members
-keep class com.example.loginandregistration.models.** { *; }
-keepclassmembers class com.example.loginandregistration.models.** { *; }

# Keep data class properties for Firestore serialization
-keepclassmembers class com.example.loginandregistration.models.** {
    <fields>;
    <init>(...);
}

# Specific model classes
-keep class com.example.loginandregistration.models.Chat { *; }
-keep class com.example.loginandregistration.models.Message { *; }
-keep class com.example.loginandregistration.models.FirebaseUser { *; }
-keep class com.example.loginandregistration.models.FirebaseGroup { *; }
-keep class com.example.loginandregistration.models.FirebaseTask { *; }
-keep class com.example.loginandregistration.models.FirebaseSession { *; }
-keep class com.example.loginandregistration.models.UIModels** { *; }

# Keep enums
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# ===========================
# Coil Image Loading Library
# ===========================

# Keep Coil classes
-keep class coil.** { *; }
-keep interface coil.** { *; }
-dontwarn coil.**

# Keep Coil's ImageLoader and related classes
-keep class coil.ImageLoader { *; }
-keep class coil.request.** { *; }
-keep class coil.transform.** { *; }
-keep class coil.decode.** { *; }
-keep class coil.fetch.** { *; }
-keep class coil.size.** { *; }

# OkHttp (used by Coil)
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
-dontwarn okhttp3.**
-dontwarn okio.**

# ===========================
# Kotlin Coroutines
# ===========================

# Keep coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}

# Keep coroutines debug info
-keepclassmembernames class kotlinx.** {
    volatile <fields>;
}

# ServiceLoader support for coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

# Most of volatile fields are updated with AFU and should not be mangled
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}

# Same story for the standard library's SafeContinuation that also uses AtomicReferenceFieldUpdater
-keepclassmembers class kotlin.coroutines.SafeContinuation {
    volatile <fields>;
}

# ===========================
# Kotlin Serialization
# ===========================

-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# ===========================
# AndroidX and Lifecycle
# ===========================

# Keep AndroidX classes
-keep class androidx.** { *; }
-keep interface androidx.** { *; }
-dontwarn androidx.**

# Lifecycle
-keep class androidx.lifecycle.** { *; }
-keepclassmembers class androidx.lifecycle.** { *; }

# ViewModel
-keep class * extends androidx.lifecycle.ViewModel {
    <init>();
}
-keep class * extends androidx.lifecycle.AndroidViewModel {
    <init>(android.app.Application);
}

# LiveData
-keepclassmembers class * extends androidx.lifecycle.LiveData {
    <init>();
}

# Navigation Component
-keep class androidx.navigation.** { *; }
-keepnames class androidx.navigation.fragment.NavHostFragment

# WorkManager
-keep class androidx.work.** { *; }
-keep class * extends androidx.work.Worker
-keep class * extends androidx.work.CoroutineWorker {
    <init>(...);
}

# ===========================
# Gson (JSON Serialization)
# ===========================

-keep class com.google.gson.** { *; }
-keep class sun.misc.Unsafe { *; }
-keep class com.google.gson.stream.** { *; }

# Gson specific classes
-keepclassmembers,allowobfuscation class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# ===========================
# Calendar Library
# ===========================

-keep class com.kizitonwose.calendar.** { *; }
-keepclassmembers class com.kizitonwose.calendar.** { *; }

# ===========================
# Material Design Components
# ===========================

-keep class com.google.android.material.** { *; }
-dontwarn com.google.android.material.**
-dontnote com.google.android.material.**

# ===========================
# Parcelable
# ===========================

-keepclassmembers class * implements android.os.Parcelable {
    public static final ** CREATOR;
}

# ===========================
# Serializable
# ===========================

-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# ===========================
# Remove Logging (Optional)
# ===========================

# Remove Log calls in release builds
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
}

# ===========================
# General Android Rules
# ===========================

# Keep native methods
-keepclasseswithmembernames class * {
    native <methods>;
}

# Keep custom views
-keep public class * extends android.view.View {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
    public void set*(...);
}

# Keep Activity and Fragment constructors
-keepclassmembers class * extends android.app.Activity {
    public void *(android.view.View);
}
-keepclassmembers class * extends androidx.fragment.app.Fragment {
    public void *(android.view.View);
}

# Keep R class
-keepclassmembers class **.R$* {
    public static <fields>;
}

# ===========================
# Reflection
# ===========================

-keepattributes RuntimeVisibleAnnotations
-keepattributes RuntimeInvisibleAnnotations
-keepattributes RuntimeVisibleParameterAnnotations
-keepattributes RuntimeInvisibleParameterAnnotations

# ===========================
# Crashlytics (if added later)
# ===========================

-keepattributes *Annotation*
-keepattributes SourceFile,LineNumberTable
-keep public class * extends java.lang.Exception