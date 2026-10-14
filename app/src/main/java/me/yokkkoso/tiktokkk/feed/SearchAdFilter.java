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
import de.robv.android.xposed.XposedHelpers;

public final class SearchAdFilter {
    public static final String FEATURE = "Hide search ads";

    private static final String ITEM =
            "com.ss.android.ugc.aweme.search.pages.result.topsearch.core.model.SearchMixFeed";

    public static final String INFLOW = "Hide search feed ads";

    private static final String SEARCH_MIX =
            "com.ss.android.ugc.aweme.search.pages.result.topsearch.core.model.SearchMix";
    private static final String DETAIL_PANEL =
            "com.ss.android.ugc.aweme.detail.panel.DetailFragmentPanel";

    public static void installInflow(ClassLoader cl) {
        int n = 0;
        try {
            XposedHelpers.findAndHookMethod(SEARCH_MIX, cl, "getAwemeList", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    Object r = param.getResult();
                    if (r instanceof List) FeedFilter.filterAwemes((List<?>) r);
                }
            });
            n++;
        } catch (Throwable t) {
            TikToKKK.log("search inflow: SearchMix miss: " + t);
        }
        try {
            Class<?> panel = XposedHelpers.findClass(DETAIL_PANEL, cl);
            for (Method m : panel.getDeclaredMethods()) {
                Class<?>[] p = m.getParameterTypes();
                if (m.getReturnType() != void.class) continue;
                if (p.length != 2 || p[0] != List.class || p[1] != boolean.class) continue;
                XposedBridge.hookMethod(m, new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) {
                        if (param.args[0] instanceof List) {
                            FeedFilter.filterAwemes((List<?>) param.args[0]);
                        }
                    }
                });
                n++;
            }
        } catch (Throwable t) {
            TikToKKK.log("search inflow: DetailFragmentPanel miss: " + t);
        }
        if (n == 0) {
            FeatureStatus.failed(INFLOW, "no search inflow hook");
        } else {
            FeatureStatus.ok(INFLOW, n + " hook(s)");
        }
        TikToKKK.log("search inflow filter: " + n + " hooks");
    }

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
