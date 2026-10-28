# Room
-keep class * extends androidx.room.RoomDatabase { <init>(); }
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# ML Kit translation
-keep class com.google.mlkit.** { *; }
-dontwarn com.google.mlkit.**

# Kotlin metadata / coroutines
-dontwarn kotlinx.coroutines.**
-keepclassmembers class kotlinx.coroutines.** { volatile <fields>; }

# Keep data classes used by Room / serialization
-keep class com.duolearning.app.data.local.entity.** { *; }
