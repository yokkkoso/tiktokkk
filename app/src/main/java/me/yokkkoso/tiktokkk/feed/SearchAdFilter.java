package me.yokkkoso.tiktokkk.feed;

import me.yokkkoso.tiktokkk.Prefs;
import me.yokkkoso.tiktokkk.Reflect;
import me.yokkkoso.tiktokkk.TikToKKK;

import java.util.ArrayList;
import java.util.List;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;

public final class SearchAdFilter {

    private static final String FRAG =
            "com.ss.android.ugc.aweme.search.pages.result.common.core.ui.fragment.SearchFragment";
    private static final String ITEM =
            "com.ss.android.ugc.aweme.search.pages.result.topsearch.core.model.SearchMixFeed";

    public static void install(ClassLoader cl) {
        try {
            XposedHelpers.findAndHookMethod(FRAG, cl, "LLJJJIL",
                    List.class, boolean.class, new XC_MethodHook() {
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
            TikToKKK.log("search ad filter installed");
        } catch (Throwable t) {
            TikToKKK.log("search ad filter install failed: " + t);
        }
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
