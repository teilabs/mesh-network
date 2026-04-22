# Сохраняем Core-интерфейсы и классы (вызываются через рефлексию/callback)
-keep class io.github.teilabs.meshnet.core.** { *; }
-keep interface io.github.teilabs.meshnet.core.CoreEvents { *; }

# Модели SDK-протокола (Gson требует доступа к полям)
-keep class io.github.teilabs.meshnet.client.android.sdk.model.** { *; }

# Gson-specific правила
-keepattributes Signature, InnerClasses, EnclosingMethod
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}
-dontwarn com.google.gson.**
-dontwarn javax.annotation.**