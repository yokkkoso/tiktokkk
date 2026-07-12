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
