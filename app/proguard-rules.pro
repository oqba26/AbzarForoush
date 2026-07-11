# 1. Base Room & Data Rules
-keepclassmembers class * extends androidx.room.RoomDatabase {
    <init>(...);
}
-keep @androidx.room.Entity class com.oqba26.abzarforoush.data.** { *; }
-keep class com.oqba26.abzarforoush.data.** { *; }
-keepclassmembers class com.oqba26.abzarforoush.data.** {
    <fields>;
}

# 2. Kotlin Serialization
-keepattributes *Annotation*, InnerClasses, EnclosingMethod, Signature, Exceptions
-keep @kotlinx.serialization.Serializable class com.oqba26.abzarforoush.** { *; }
-keepclassmembers class com.oqba26.abzarforoush.** {
    *** Companion;
    *** serializer(...);
}

# 3. Ktor & Supabase (Ignore missing desktop/optional classes)
-keep class io.github.jan.** { *; }
-keep class io.ktor.** { *; }
-dontwarn io.ktor.**
-dontwarn io.github.jan.**

# 4. Apache POI & Sheetz (Excel Support)
# These libraries reference 'java.awt' which doesn't exist on Android.
# We MUST ignore these warnings to complete the build.
-dontwarn java.awt.**
-dontwarn javax.xml.stream.**
-dontwarn org.apache.poi.**
-dontwarn org.apache.xmlbeans.**
-dontwarn net.sf.saxon.**
-dontwarn org.apache.batik.**
-dontwarn aQute.bnd.annotation.**
-dontwarn org.osgi.framework.**
-dontwarn com.microsoft.schemas.office.**

# Keep Sheetz and POI needed classes
-keep class io.github.chitralabs.sheetz.** { *; }
-keep class org.apache.poi.** { *; }

# 5. Gemini / Generative AI
-dontwarn com.google.ai.client.generativeai.**
-keep class com.google.ai.client.generativeai.** { *; }

# 6. General
-ignorewarnings
