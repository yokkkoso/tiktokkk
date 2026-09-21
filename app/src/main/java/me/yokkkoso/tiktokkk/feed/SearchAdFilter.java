package me.yokkkoso.tiktokkk.feed;

import me.yokkkoso.tiktokkk.FeatureStatus;
import me.yokkkoso.tiktokkk.Prefs;
import me.yokkkoso.tiktokkk.Reflect;
import me.yokkkoso.tiktokkk.TikToKKK;
import me.yokkkoso.tiktokkk.dex.DexTargets;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.luckypray.dexkit.DexKitBridge;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;

public final class SearchAdFilter {
    public static final String FEATURE = "Hide search ads";

    private static final String ITEM =
            "com.ss.android.ugc.aweme.search.pages.result.topsearch.core.model.SearchMixFeed";

    public static void install(ClassLoader cl, DexKitBridge bridge) {
        int n = 0;
        if (hook(DexTargets.searchPush(bridge, cl))) n++;
        if (hook(DexTargets.searchPushMore(bridge, cl))) n++;
        if (n == 0) {
            FeatureStatus.failed(FEATURE, "no search result push method found");
        } else {
            FeatureStatus.ok(FEATURE, n + " push method(s)");
        }
    }

    private static boolean hook(Method m) {
        if (m == null) return false;
        XposedBridge.hookMethod(m, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                if (!Prefs.is(Prefs.HIDE_FEED_ADS)) return;
                List<?> in = (List<?>) param.args[0];
                if (in == null || in.isEmpty()) return;
                List<Object> out = new ArrayList<>(in.size());
                for (Object it : in) {
                    if (it == null || !ITEM.equals(it.getClass().getName()) || !isAd(it)) {
                        out.add(it);
                    }
                }
                if (!out.isEmpty() && out.size() < in.size()) param.args[0] = out;
            }
        });
        TikToKKK.log("search ad filter installed on " + m.getName());
        return true;
    }

    private static boolean isAd(Object item) {
        Object aweme = Reflect.call(item, "getAweme");
        if (aweme != null && Reflect.boolVal(aweme, "isAd")) return true;
        if (Reflect.call(item, "getAiAdCard") != null) return true;
        if (Reflect.call(item, "getBrandZoneCard") != null) return true;
        if (Reflect.call(item, "getPreciseAd") != null) return true;
        if (Reflect.field(item, "multiAdCard") != null) return true;
        Object dt = Reflect.field(item, "docType");
        return dt instanceof Integer && (Integer) dt == 10003;
    }

    private SearchAdFilter() {}
}
