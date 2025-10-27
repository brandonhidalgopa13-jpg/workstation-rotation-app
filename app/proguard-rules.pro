# ═══════════════════════════════════════════════════════════════════════════════════════════════
# 🛡️ PROGUARD RULES - SISTEMA DE ROTACIÓN INTELIGENTE
# ═══════════════════════════════════════════════════════════════════════════════════════════════
# Configuración optimizada para release builds
# Mantiene funcionalidad mientras reduce el tamaño de la APK

# ═══════════════════════════════════════════════════════════════════════════════════════════════
# 📊 DEBUGGING Y STACK TRACES
# ═══════════════════════════════════════════════════════════════════════════════════════════════
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Mantener información de debugging para crash reports
-keepattributes *Annotation*
-keepattributes Signature
-keepattributes InnerClasses
-keepattributes EnclosingMethod

# ═══════════════════════════════════════════════════════════════════════════════════════════════
# 🏗️ ANDROID FRAMEWORK
# ═══════════════════════════════════════════════════════════════════════════════════════════════
# Mantener clases del framework Android
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.app.backup.BackupAgentHelper
-keep public class * extends android.preference.Preference

# Mantener métodos de ciclo de vida
-keepclassmembers class * extends android.app.Activity {
   public void *(android.view.View);
}

# Mantener constructores de View
-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet);
}
-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

# ═══════════════════════════════════════════════════════════════════════════════════════════════
# 🗄️ ROOM DATABASE
# ═══════════════════════════════════════════════════════════════════════════════════════════════
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keep @androidx.room.Dao class *
-keep @androidx.room.Database class *
-keep @androidx.room.TypeConverter class *

# Mantener métodos de Room
-keepclassmembers class * extends androidx.room.RoomDatabase {
    public abstract *;
}

# ═══════════════════════════════════════════════════════════════════════════════════════════════
# 📦 KOTLINX SERIALIZATION
# ═══════════════════════════════════════════════════════════════════════════════════════════════
-dontnote kotlinx.serialization.AnnotationsKt
-dontnote kotlinx.serialization.SerializationKt

-keep,includedescriptorclasses class com.workstation.rotation.**$$serializer { *; }
-keepclassmembers class com.workstation.rotation.** {
    *** Companion;
}
-keepclasseswithmembers class com.workstation.rotation.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Mantener clases serializables
-keep @kotlinx.serialization.Serializable class * { *; }

# ═══════════════════════════════════════════════════════════════════════════════════════════════
# 🏭 ENTIDADES Y MODELOS DE DATOS
# ═══════════════════════════════════════════════════════════════════════════════════════════════
# Mantener todas las entidades de datos
-keep class com.workstation.rotation.data.entities.** { *; }
-keep class com.workstation.rotation.models.** { *; }
-keep class com.workstation.rotation.data.sync.BackupManager$** { *; }
-keep class com.workstation.rotation.data.cloud.** { *; }

# ═══════════════════════════════════════════════════════════════════════════════════════════════
# 🎯 VIEWMODELS Y ARQUITECTURA
# ═══════════════════════════════════════════════════════════════════════════════════════════════
-keep class * extends androidx.lifecycle.ViewModel {
    <init>(...);
}
-keep class * extends androidx.lifecycle.ViewModelProvider$Factory {
    <init>(...);
}

# Mantener ViewModels específicos
-keep class com.workstation.rotation.viewmodels.** { *; }

# ═══════════════════════════════════════════════════════════════════════════════════════════════
# 🔥 FIREBASE
# ═══════════════════════════════════════════════════════════════════════════════════════════════
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.firebase.**
-dontwarn com.google.android.gms.**

# Firebase Firestore
-keepclassmembers class * {
    @com.google.firebase.firestore.PropertyName <methods>;
    @com.google.firebase.firestore.PropertyName <fields>;
}

# ═══════════════════════════════════════════════════════════════════════════════════════════════
# 🎨 UI Y ADAPTERS
# ═══════════════════════════════════════════════════════════════════════════════════════════════
# Mantener adapters de RecyclerView
-keep class * extends androidx.recyclerview.widget.RecyclerView$Adapter {
    <init>(...);
}
-keep class * extends androidx.recyclerview.widget.RecyclerView$ViewHolder {
    <init>(...);
}

# Mantener clases de UI específicas
-keep class com.workstation.rotation.adapters.** { *; }
-keep class com.workstation.rotation.tutorial.** { *; }

# ═══════════════════════════════════════════════════════════════════════════════════════════════
# 🔧 UTILIDADES Y HELPERS
# ═══════════════════════════════════════════════════════════════════════════════════════════════
-keep class com.workstation.rotation.utils.** { *; }

# ═══════════════════════════════════════════════════════════════════════════════════════════════
# 📱 WORK MANAGER
# ═══════════════════════════════════════════════════════════════════════════════════════════════
-keep class * extends androidx.work.Worker
-keep class * extends androidx.work.InputMerger
-keep class androidx.work.impl.WorkManagerImpl

# ═══════════════════════════════════════════════════════════════════════════════════════════════
# 🚫 WARNINGS SUPPRESSION
# ═══════════════════════════════════════════════════════════════════════════════════════════════
-dontwarn okio.**
-dontwarn retrofit2.**
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**

# ═══════════════════════════════════════════════════════════════════════════════════════════════
# 🎯 OPTIMIZACIÓN ESPECÍFICA
# ═══════════════════════════════════════════════════════════════════════════════════════════════
# Permitir optimización agresiva pero mantener funcionalidad crítica
-optimizations !code/simplification/arithmetic,!code/simplification/cast,!field/*,!class/merging/*
-optimizationpasses 5
-allowaccessmodification
-dontpreverify

# Mantener nombres de métodos para reflection si es necesario
-keepclassmembernames class * {
    java.lang.Class class$(java.lang.String);
    java.lang.Class class$(java.lang.String, boolean);
}

# ═══════════════════════════════════════════════════════════════════════════════════════════════
# 📊 LOGGING Y DEBUGGING
# ═══════════════════════════════════════════════════════════════════════════════════════════════
# Remover logs en release (opcional)
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int i(...);
    public static int w(...);
    public static int d(...);
    public static int e(...);
}