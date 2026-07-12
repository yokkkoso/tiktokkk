package me.yokkkoso.tiktokkk.download;

import me.yokkkoso.tiktokkk.Prefs;
import me.yokkkoso.tiktokkk.Reflect;
import me.yokkkoso.tiktokkk.TikToKKK;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;

public final class WatermarkRemover {

    public static void install(ClassLoader cl) {
        try {
            Class<?> video = XposedHelpers.findClass("com.ss.android.ugc.aweme.feed.model.Video", cl);
            XposedHelpers.findAndHookMethod(video, "getDownloadAddr", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam p) {
                    if (!Prefs.is(Prefs.REMOVE_WATERMARK)) return;
                    Object noWm = Reflect.call(p.thisObject, "getDownloadNoWatermarkAddr");
                    if (Reflect.hasUrls(noWm)) p.setResult(noWm);
                }
            });
            TikToKKK.log("watermark remover installed");
        } catch (Throwable t) {
            TikToKKK.log("watermark remover install failed: " + t);
        }
    }

    private WatermarkRemover() {}
}
