-keep class io.github.teilabs.meshnet.client.android.MeshClientService { *; }

-keep class io.github.teilabs.meshnet.client.android.ClientEngine { *; }
-keep interface io.github.teilabs.meshnet.client.android.** { public *; }

-keep class io.github.teilabs.meshnet.core.** {
    public protected *;
}
-keep interface io.github.teilabs.meshnet.core.** { *; }

-keep class io.github.teilabs.meshnet.core.MeshCore { *; }
-keep interface io.github.teilabs.meshnet.core.CoreEvents { *; }
-keep class io.github.teilabs.meshnet.core.CoreInput { *; }

-keepattributes Signature, RuntimeVisibleAnnotations, AnnotationDefault

-keep class io.github.teilabs.meshnet.client.android.sdk.model.** { *; }

-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

-keepclassmembers class * {
    public <init>();
}

-dontwarn org.bouncycastle.**

-keep class org.bouncycastle.jcajce.provider.** { *; }
-keep class org.bouncycastle.pqc.jcajce.provider.** { *; }

-keep class androidx.core.** { *; }
-dontwarn androidx.**

-keepclasseswithmembernames class * {
    native <methods>;
}

-keep class io.github.teilabs.meshnet.client.android.sdk.** { *; }
-keep class io.github.teilabs.meshnet.client.android.daemon.** { *; }