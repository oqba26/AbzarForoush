# Keep Room components
-keepclassmembers class * extends androidx.room.RoomDatabase {
    <init>(...);
}
-keep class androidx.room.BoundLocation { *; }

# Keep our data models (Entities and DTOs)
-keep @androidx.room.Entity class com.oqba26.abzarforoush.data.** { *; }
-keep class com.oqba26.abzarforoush.data.** { *; }

# Keep Kotlin Serialization requirements
-keepattributes *Annotation*, InnerClasses, EnclosingMethod, Signature, Exceptions
-keep @kotlinx.serialization.Serializable class com.oqba26.abzarforoush.** { *; }
-keepclassmembers class com.oqba26.abzarforoush.** {
    *** Companion;
    *** serializer(...);
}

# Keep Supabase and Ktor classes if needed
-keep class io.github.jan.** { *; }
-keep class io.ktor.** { *; }

# Keep names of fields in our entities to prevent mapping errors
-keepclassmembers class com.oqba26.abzarforoush.data.** {
    <fields>;
}
