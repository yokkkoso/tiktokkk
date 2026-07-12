package me.yokkkoso.tiktokkk.feed;

import me.yokkkoso.tiktokkk.Prefs;
import me.yokkkoso.tiktokkk.Reflect;
import me.yokkkoso.tiktokkk.TikToKKK;

import java.util.ArrayList;
import java.util.List;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;

public final class FeedFilter {

    private static final String FEED_LIST = "com.ss.android.ugc.aweme.feed.model.FeedItemList";
    private static final String FOLLOW_FEED_LIST = "com.ss.android.ugc.aweme.follow.presenter.FollowFeedList";
    private static final String FOLLOWING_INTEREST =
            "com.ss.android.ugc.aweme.feed.module.FollowingInterestFeedResponse";
    private static final String AWEME_EXT = "com.ss.android.ugc.aweme.feed.model.AwemeExtKt";

    private static Class<?> awemeExt;

    public static void install(ClassLoader cl) {
        try {
            awemeExt = XposedHelpers.findClass(AWEME_EXT, cl);
        } catch (Throwable ignored) {}
        // The network path deserializes items straight into the list field, so setItems is often
        // never called — filter the READ side (getItems/getAwemeList) which every consumer uses.
        hookGetter(cl, FEED_LIST, "getItems");
        hookGetter(cl, FEED_LIST, "getAwemeList");
        hookSetItems(cl, FEED_LIST);
        hookGetter(cl, FOLLOW_FEED_LIST, "getItems");
        hookGetter(cl, FOLLOW_FEED_LIST, "getAwemeList");
        hookGetter(cl, FOLLOW_FEED_LIST, "getInsertedResults");
        hookGetter(cl, FOLLOW_FEED_LIST, "getInsertResults");
        hookSetItems(cl, FOLLOW_FEED_LIST);
        hookGetter(cl, FOLLOWING_INTEREST, "getAwemeList");
    }

    private static void hookGetter(ClassLoader cl, String className, String method) {
        try {
            XposedHelpers.findAndHookMethod(XposedHelpers.findClass(className, cl), method,
                    new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    Object r = param.getResult();
                    if (r instanceof List) param.setResult(filter((List<?>) r));
                }
            });
            TikToKKK.log("feed filter installed: " + className + "." + method);
        } catch (Throwable t) {
            TikToKKK.log("feed filter miss (" + className + "." + method + "): " + t);
        }
    }

    private static void hookSetItems(ClassLoader cl, String className) {
        try {
            XposedHelpers.findAndHookMethod(XposedHelpers.findClass(className, cl), "setItems",
                    List.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) {
                    param.args[0] = filter((List<?>) param.args[0]);
                }
            });
        } catch (Throwable ignored) {}
    }

    // Mutate the list in place and return the SAME object — returning a new list on every getItems
    // call churns the RecyclerView (it diffs a fresh object each frame) and blanks the feed.
    private static List<?> filter(List<?> items) {
        if (items == null || items.isEmpty() || !anyFilterOn()) return items;
        List<Object> drop = null;
        for (Object item : items) {
            if (item != null && shouldDrop(awemeOf(item))) {
                if (drop == null) drop = new ArrayList<>();
                drop.add(item);
            }
        }
        if (drop != null && drop.size() < items.size()) {
            try { items.removeAll(drop); } catch (Throwable ignored) {}
        }
        return items;
    }

    // Feed lists hold either Aweme directly or wrapper cells (FollowFeed etc.) exposing getAweme().
    private static Object awemeOf(Object item) {
        Object aw = Reflect.call(item, "getAweme");
        return aw != null ? aw : item;
    }

    private static boolean anyFilterOn() {
        return Prefs.is(Prefs.HIDE_FEED_ADS) || Prefs.is(Prefs.HIDE_LIVE)
                || Prefs.is(Prefs.HIDE_SLIDESHOW) || Prefs.is(Prefs.HIDE_SHOP)
                || Prefs.is(Prefs.HIDE_AI_POSTS) || Prefs.is(Prefs.HIDE_REWARDS_ADS)
                || Prefs.is(Prefs.HIDE_LOCATION_ADS) || Prefs.is(Prefs.HIDE_COMMISSION)
                || Prefs.is(Prefs.HIDE_FRIEND_SUGGEST);
    }

    private static boolean shouldDrop(Object aweme) {
        if (aweme == null) return false;
        if (Prefs.is(Prefs.HIDE_FEED_ADS) && isAdvert(aweme)) return true;
        if (Prefs.is(Prefs.HIDE_LIVE) && isLiveCard(aweme)) return true;
        if (Prefs.is(Prefs.HIDE_SLIDESHOW)
                && (Reflect.boolVal(aweme, "isPhotoMode") || Reflect.boolVal(aweme, "isImage"))) return true;
        if (Prefs.is(Prefs.HIDE_SHOP) && Reflect.boolVal(aweme, "isCommerce")) return true;
        if (Prefs.is(Prefs.HIDE_AI_POSTS) && isAigc(aweme)) return true;
        if (Prefs.is(Prefs.HIDE_REWARDS_ADS) && isRewardAd(aweme)) return true;
        if (Prefs.is(Prefs.HIDE_LOCATION_ADS)
                && Reflect.call(aweme, "getLocalServiceInfo") != null) return true;
        if (Prefs.is(Prefs.HIDE_COMMISSION)
                && (Reflect.intVal(aweme, "getProductsCount") > 0
                || Reflect.call(aweme, "getCommerceVideoAuthInfo") != null)) return true;
        if (Prefs.is(Prefs.HIDE_FRIEND_SUGGEST)
                && Reflect.call(aweme, "getRelationRecommendInfo") != null) return true;
        return false;
    }

    // Broad, language-independent ad signal — Aweme.isAd() alone misses monetization/pseudo/filed
    // ads that leak into Following and profile feeds. AwemeExtKt statics + rawAd + injected cell
    // types (104/105) cover them.
    private static boolean isAdvert(Object aweme) {
        if (Reflect.boolVal(aweme, "isAd")) return true;
        if (Reflect.call(aweme, "getAwemeRawAd") != null) return true;
        int ty = Reflect.intVal(aweme, "getAwemeType");
        if (ty == 104 || ty == 105) return true;
        if (extBool(aweme, "isAdFiled") || extBool(aweme, "isAdTraffic")
                || extBool(aweme, "isPseudoAd") || extBool(aweme, "isMonetizationTraffic")) return true;
        return extObj(aweme, "getMonetizationData") != null;
    }

    private static boolean extBool(Object aweme, String method) {
        if (awemeExt == null) return false;
        try {
            Object r = XposedHelpers.callStaticMethod(awemeExt, method, aweme);
            return r instanceof Boolean && (Boolean) r;
        } catch (Throwable t) {
            return false;
        }
    }

    private static Object extObj(Object aweme, String method) {
        if (awemeExt == null) return null;
        try {
            return XposedHelpers.callStaticMethod(awemeExt, method, aweme);
        } catch (Throwable t) {
            return null;
        }
    }

    private static boolean isLiveCard(Object aweme) {
        if (Reflect.boolVal(aweme, "isLive")) return true;
        if (Reflect.call(aweme, "getRoom") != null) return true;
        if (Reflect.call(aweme, "getRoomFeedCellStruct") != null) return true;
        if (Reflect.call(aweme, "getStreamUrlModel") != null) return true;
        Object liveId = Reflect.call(aweme, "getLiveId");
        return liveId instanceof Number && ((Number) liveId).longValue() != 0;
    }

    private static boolean isAigc(Object aweme) {
        Object aigc = Reflect.call(aweme, "getAigcInfo");
        if (aigc == null) return false;
        return Reflect.boolField(aigc, "createByAI") || Reflect.intVal(aigc, "getAIGCLabelType") != 0;
    }

    private static boolean isRewardAd(Object aweme) {
        Object raw = Reflect.call(aweme, "getAwemeRawAd");
        if (raw == null) return false;
        if (Reflect.call(raw, "getAdRewardData") != null) return true;
        Object inc = Reflect.call(raw, "getIncentiveInfo");
        return inc instanceof String && !((String) inc).isEmpty();
    }

    private FeedFilter() {}
}
