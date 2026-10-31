# ========================================================================
# R8 / ProGuard Keep Rules - PsicoRuta
# ========================================================================

# 1. Atributos estándar necesarios para reflexión y serialización
-keepattributes *Annotation*, InnerClasses, EnclosingMethod, Signature, Exceptions

# 2. Kotlinx Serialization
-keepclassmembers class * {
    @kotlinx.serialization.Serializable <fields>;
}
-keepclassmembers class * {
    @kotlinx.serialization.SerialName <fields>;
}
-keepclassmembers class * {
    *** Companion;
}
-keepclasseswithmembers class * {
    @kotlinx.serialization.Serializable class *;
}
-keep,allowobfuscation class * extends kotlinx.serialization.internal.GeneratedSerializer { *; }

# 3. Modelos de datos del proyecto (Estudiante, Asignatura, Horario, etc.)
-keep class com.renea.psicologiauv.model.** { *; }

# 4. Google Play In-App Updates
-keep class com.google.android.play.core.** { *; }
-dontwarn com.google.android.play.core.**

# 5. PdfBox Android
-keep class com.tom_roush.pdfbox.** { *; }
-dontwarn com.tom_roush.pdfbox.**

# 6. Compose runtime / UI keep rules
-dontwarn androidx.compose.**
