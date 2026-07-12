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

    public static void install(ClassLoader cl) {
        hookSetItems(cl, FEED_LIST);
        hookSetItems(cl, FOLLOW_FEED_LIST);
    }

    private static void hookSetItems(ClassLoader cl, String className) {
        try {
            Class<?> feedList = XposedHelpers.findClass(className, cl);
            XposedHelpers.findAndHookMethod(feedList, "setItems", List.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) {
                    param.args[0] = filter((List<?>) param.args[0]);
                }
            });
            TikToKKK.log("feed filter installed: " + className);
        } catch (Throwable t) {
            TikToKKK.log("feed filter install failed (" + className + "): " + t);
        }
    }

    private static List<?> filter(List<?> items) {
        if (items == null || items.isEmpty() || !anyFilterOn()) return items;
        List<Object> kept = new ArrayList<>(items.size());
        for (Object aweme : items) {
            if (aweme == null || !shouldDrop(aweme)) kept.add(aweme);
        }
        // safety: never blank the whole page (a too-broad predicate would kill the feed)
        return kept.isEmpty() ? items : kept;
    }

    private static boolean anyFilterOn() {
        return Prefs.is(Prefs.HIDE_FEED_ADS) || Prefs.is(Prefs.HIDE_LIVE)
                || Prefs.is(Prefs.HIDE_SLIDESHOW) || Prefs.is(Prefs.HIDE_SHOP)
                || Prefs.is(Prefs.HIDE_AI_POSTS) || Prefs.is(Prefs.HIDE_REWARDS_ADS)
                || Prefs.is(Prefs.HIDE_LOCATION_ADS) || Prefs.is(Prefs.HIDE_COMMISSION)
                || Prefs.is(Prefs.HIDE_FRIEND_SUGGEST);
    }

    private static boolean shouldDrop(Object aweme) {
        if (Prefs.is(Prefs.HIDE_FEED_ADS) && Reflect.boolVal(aweme, "isAd")) return true;
        if (Prefs.is(Prefs.HIDE_LIVE) && isLiveCard(aweme)) return true;
        if (Prefs.is(Prefs.HIDE_SLIDESHOW)
                && (Reflect.boolVal(aweme, "isPhotoMode") || Reflect.boolVal(aweme, "isImage"))) return true;
        if (Prefs.is(Prefs.HIDE_SHOP) && Reflect.boolVal(aweme, "isCommerce")) return true;
        if (Prefs.is(Prefs.HIDE_AI_POSTS) && isAigc(aweme)) return true;
        if (Prefs.is(Prefs.HIDE_REWARDS_ADS) && isRewardAd(aweme)) return true;
        // getPoiDataStruct is present on ANY location-tagged video (not just ads) and over-filters
        // the whole FYP, so only the local-business signal is used here.
        if (Prefs.is(Prefs.HIDE_LOCATION_ADS)
                && Reflect.call(aweme, "getLocalServiceInfo") != null) return true;
        if (Prefs.is(Prefs.HIDE_COMMISSION)
                && (Reflect.intVal(aweme, "getProductsCount") > 0
                || Reflect.call(aweme, "getCommerceVideoAuthInfo") != null)) return true;
        if (Prefs.is(Prefs.HIDE_FRIEND_SUGGEST)
                && Reflect.call(aweme, "getRelationRecommendInfo") != null) return true;
        return false;
    }

    // A live card in the feed (not the "author is live" badge on a normal video, which would
    // over-filter): it carries a live room / feed cell / stream, or a non-zero live id.
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
