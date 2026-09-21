package me.yokkkoso.tiktokkk.feed;

import me.yokkkoso.tiktokkk.FeatureStatus;
import me.yokkkoso.tiktokkk.Prefs;
import me.yokkkoso.tiktokkk.TikToKKK;
import me.yokkkoso.tiktokkk.dex.DexTargets;

import java.lang.reflect.Method;

import org.luckypray.dexkit.DexKitBridge;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

public final class SplashAdBlock {
    public static final String FEATURE = "Hide startup ads";
    public static final String TOPVIEW = "Hide launch feed ad";

    private static final String SPLASH_SERVICE =
            "com.bytedance.ies.ugc.aweme.commercialize.splash.service.ISplashAdService";
    private static final String SPLASH_IMPL =
            "com.bytedance.ies.ugc.aweme.commercialize.splash.core.SplashAdServiceImpl";
    private static final String AWEME = "com.ss.android.ugc.aweme.feed.model.Aweme";

    // The TopView ad never arrives inside the feed response: it is built from preloaded JSON and
    // inserted into the pager afterwards, so no amount of list filtering removes it. Starving that
    // insert of its Aweme is the one cut that stops it, and null is a case TikTok already handles.
    // The method name is obfuscated but it is the only (String) -> Aweme on the service.
    public static void installTopView(ClassLoader cl) {
        try {
            Class<?> api = XposedHelpers.findClass(SPLASH_SERVICE, cl);
            Class<?> aweme = XposedHelpers.findClass(AWEME, cl);
            String name = null;
            for (Method m : api.getDeclaredMethods()) {
                Class<?>[] p = m.getParameterTypes();
                if (m.getReturnType() == aweme && p.length == 1 && p[0] == String.class) {
                    if (name != null) {
                        FeatureStatus.failed(TOPVIEW, "ambiguous (String)->Aweme on ISplashAdService");
                        return;
                    }
                    name = m.getName();
                }
            }
            if (name == null) {
                FeatureStatus.failed(TOPVIEW, "no (String)->Aweme on ISplashAdService");
                return;
            }
            XposedHelpers.findAndHookMethod(SPLASH_IMPL, cl, name, String.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) {
                    if (!Prefs.is(Prefs.HIDE_SPLASH_ADS)) return;
                    param.setResult(null);
                    TikToKKK.log("topview ad suppressed");
                }
            });
            FeatureStatus.ok(TOPVIEW, SPLASH_IMPL + "." + name);
            TikToKKK.log("topview block installed on " + name);
        } catch (Throwable t) {
            FeatureStatus.failed(TOPVIEW, "splash service not found");
            TikToKKK.log("topview block install failed: " + t);
        }
    }

    public static void install(ClassLoader cl, DexKitBridge bridge) {

        Method m = DexTargets.splashAdColdStart(bridge, cl);
        if (m == null) {
            FeatureStatus.failed(FEATURE, "splash ad entry not found");
            return;
        }
        XposedBridge.hookMethod(m, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                if (Prefs.is(Prefs.HIDE_SPLASH_ADS)) param.setResult(null);
            }
        });
        FeatureStatus.ok(FEATURE, m.getDeclaringClass().getName() + "." + m.getName());
        TikToKKK.log("splash ad block installed on " + m.getDeclaringClass().getName());
    }

    private SplashAdBlock() {}
}
