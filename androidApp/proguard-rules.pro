# Kotlin Serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# SQLDelight
-keep class app.cash.sqldelight.** { *; }
-keep class com.workstation.rotation.database.** { *; }

# Compose
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**
