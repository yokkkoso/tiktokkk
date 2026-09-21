-repackageclasses ''
-allowaccessmodification
-overloadaggressively

-keep class me.yokkkoso.tiktokkk.TikToKKK {
    <init>();
    public void handleLoadPackage(...);
    public void initZygote(...);
}

-keepattributes RuntimeVisibleAnnotations

-dontwarn de.robv.android.xposed.**
-dontwarn okhttp3.**
-dontwarn okio.**

# DexKit talks to libdexkit.so over JNI: the native side resolves these types and their
# fields by name, so R8 must not rename, strip or repackage anything under it.
-keep class org.luckypray.dexkit.** { *; }
-keepclassmembers class org.luckypray.dexkit.** { *; }
-dontwarn org.luckypray.dexkit.**
