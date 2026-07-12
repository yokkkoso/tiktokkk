package me.yokkkoso.tiktokkk;

import android.app.AndroidAppHelper;
import android.content.Context;
import android.content.SharedPreferences;

import java.util.LinkedHashMap;
import java.util.Map;

public final class Prefs {
    public static final String STORE = "tiktokkk";
    private static final String LEGACY_STORE = "tiktokkkk";

    public static final String CONFIRM_LIKE = "confirm_like";
    public static final String CONFIRM_FOLLOW = "confirm_follow";
    public static final String CONFIRM_COMMENT_LIKE = "confirm_comment_like";
    public static final String CONFIRM_UNLIKE = "confirm_unlike";
    public static final String CONFIRM_DISLIKE_COMMENT = "confirm_dislike_comment";
    public static final String CONFIRM_STORY_LIKE = "confirm_story_like";
    public static final String CONFIRM_QUICK_SHARE = "confirm_quick_share";
    public static final String CONFIRM_QUICK_REPOST = "confirm_quick_repost";
    public static final String CONFIRM_FAVORITE = "confirm_favorite";
    public static final String SHOW_FYP_TIMESTAMP = "fyp_timestamp";
    public static final String SHOW_POST_REGION = "show_post_region";
    public static final String FORCE_PROGRESS_BAR = "force_progress_bar";
    public static final String HIDE_PLUS_BUTTON = "hide_plus_button";
    public static final String HIDE_TAB_LABELS = "hide_tab_labels";
    public static final String HIDE_FRIENDS_BADGE = "hide_friends_badge";
    public static final String HIDE_INBOX_BADGE = "hide_inbox_badge";
    public static final String HIDE_AI_ASSISTANT = "hide_ai_assistant";
    public static final String ANONYMOUS_PROFILE_VIEW = "anon_profile_view";
    public static final String PROFILE_PIC_SAVE = "profile_pic_save";
    public static final String COPY_BIO = "copy_bio";

    public static final String HIDE_FEED_ADS = "hide_feed_ads";
    public static final String HIDE_LIVE = "hide_live";
    public static final String HIDE_SLIDESHOW = "hide_slideshow";
    public static final String HIDE_SHOP = "hide_shop";
    public static final String HIDE_STORY = "hide_story";
    public static final String HIDE_FIND_SIMILAR = "hide_find_similar";
    public static final String HIDE_SEARCH_BAR = "hide_search_bar";
    public static final String DISABLE_SCROLL_REFRESH = "disable_scroll_refresh";
    public static final String DISABLE_HOME_REFRESH = "disable_home_refresh";
    public static final String HIDE_AI_POSTS = "hide_ai_posts";
    public static final String HIDE_REWARDS_ADS = "hide_rewards_ads";
    public static final String HIDE_LOCATION_ADS = "hide_location_ads";
    public static final String HIDE_COMMISSION = "hide_commission";

    public static final String SANITIZE_LINKS = "sanitize_links";
    public static final String REMOVE_WATERMARK = "remove_watermark";
    public static final String ALLOW_ALL_DOWNLOADS = "allow_all_downloads";
    public static final String STICKER_DOWNLOAD = "sticker_download";
    public static final String SHOW_DL_BUTTON = "show_dl_button";
    public static final String DEBUG_CLICKS = "debug_clicks";

    public static final String HIDE_FRIEND_SUGGEST = "hide_friend_suggest";
    public static final String REGION = "region_code";
    public static final String LOCALE = "mod_locale";
    public static final String ACCENT_COLOR = "accent_color";
    public static final int DEFAULT_ACCENT = 0xFFFC0FC0;
    public static final String FAB_OPACITY = "fab_opacity";   // download button opacity, 0-100 %

    private static final Map<String, Boolean> BOOL_DEFAULTS = new LinkedHashMap<>();
    static {
        BOOL_DEFAULTS.put(CONFIRM_LIKE, false);
        BOOL_DEFAULTS.put(CONFIRM_FOLLOW, true);
        BOOL_DEFAULTS.put(CONFIRM_COMMENT_LIKE, false);
        BOOL_DEFAULTS.put(CONFIRM_UNLIKE, false);
        BOOL_DEFAULTS.put(CONFIRM_DISLIKE_COMMENT, false);
        BOOL_DEFAULTS.put(CONFIRM_STORY_LIKE, false);
        BOOL_DEFAULTS.put(CONFIRM_QUICK_SHARE, false);
        BOOL_DEFAULTS.put(CONFIRM_QUICK_REPOST, false);
        BOOL_DEFAULTS.put(CONFIRM_FAVORITE, false);
        BOOL_DEFAULTS.put(SHOW_FYP_TIMESTAMP, true);
        BOOL_DEFAULTS.put(SHOW_POST_REGION, true);
        BOOL_DEFAULTS.put(FORCE_PROGRESS_BAR, true);
        BOOL_DEFAULTS.put(HIDE_PLUS_BUTTON, false);
        BOOL_DEFAULTS.put(HIDE_TAB_LABELS, false);
        BOOL_DEFAULTS.put(HIDE_FRIENDS_BADGE, false);
        BOOL_DEFAULTS.put(HIDE_INBOX_BADGE, false);
        BOOL_DEFAULTS.put(HIDE_AI_ASSISTANT, true);
        BOOL_DEFAULTS.put(ANONYMOUS_PROFILE_VIEW, false);
        BOOL_DEFAULTS.put(PROFILE_PIC_SAVE, true);
        BOOL_DEFAULTS.put(COPY_BIO, true);
        BOOL_DEFAULTS.put(HIDE_FEED_ADS, true);
        BOOL_DEFAULTS.put(HIDE_LIVE, false);
        BOOL_DEFAULTS.put(HIDE_SLIDESHOW, false);
        BOOL_DEFAULTS.put(HIDE_SHOP, true);
        BOOL_DEFAULTS.put(HIDE_STORY, false);
        BOOL_DEFAULTS.put(HIDE_FIND_SIMILAR, true);
        BOOL_DEFAULTS.put(HIDE_SEARCH_BAR, true);
        BOOL_DEFAULTS.put(DISABLE_SCROLL_REFRESH, true);
        BOOL_DEFAULTS.put(DISABLE_HOME_REFRESH, true);
        BOOL_DEFAULTS.put(HIDE_AI_POSTS, false);
        BOOL_DEFAULTS.put(HIDE_REWARDS_ADS, true);
        BOOL_DEFAULTS.put(HIDE_LOCATION_ADS, false);
        BOOL_DEFAULTS.put(HIDE_COMMISSION, false);
        BOOL_DEFAULTS.put(SANITIZE_LINKS, true);
        BOOL_DEFAULTS.put(REMOVE_WATERMARK, true);
        BOOL_DEFAULTS.put(ALLOW_ALL_DOWNLOADS, true);
        BOOL_DEFAULTS.put(STICKER_DOWNLOAD, true);
        BOOL_DEFAULTS.put(SHOW_DL_BUTTON, true);
        BOOL_DEFAULTS.put(DEBUG_CLICKS, false);
        BOOL_DEFAULTS.put(HIDE_FRIEND_SUGGEST, true);
    }

    private static SharedPreferences sp;

    private static SharedPreferences sp() {
        if (sp == null) {
            Context c = AndroidAppHelper.currentApplication();
            if (c != null) {
                sp = c.getSharedPreferences(STORE, Context.MODE_PRIVATE);
                migrateLegacy(c);
            }
        }
        return sp;
    }

    // One-time carry-over from the old "tiktokkkk" (4-k) store to the renamed one.
    private static void migrateLegacy(Context c) {
        try {
            if (!sp.getAll().isEmpty()) return;
            SharedPreferences old = c.getSharedPreferences(LEGACY_STORE, Context.MODE_PRIVATE);
            Map<String, ?> all = old.getAll();
            if (all.isEmpty()) return;
            SharedPreferences.Editor e = sp.edit();
            for (Map.Entry<String, ?> en : all.entrySet()) {
                Object v = en.getValue();
                if (v instanceof Boolean) e.putBoolean(en.getKey(), (Boolean) v);
                else if (v instanceof String) e.putString(en.getKey(), (String) v);
                else if (v instanceof Integer) e.putInt(en.getKey(), (Integer) v);
                else if (v instanceof Long) e.putLong(en.getKey(), (Long) v);
                else if (v instanceof Float) e.putFloat(en.getKey(), (Float) v);
            }
            e.apply();
        } catch (Throwable ignored) {}
    }

    public static Map<String, Boolean> boolDefaults() {
        return BOOL_DEFAULTS;
    }

    public static boolean is(String key) {
        SharedPreferences p = sp();
        boolean def = Boolean.TRUE.equals(BOOL_DEFAULTS.get(key));
        return p == null ? def : p.getBoolean(key, def);
    }

    public static void set(String key, boolean value) {
        SharedPreferences p = sp();
        if (p != null) p.edit().putBoolean(key, value).apply();
    }

    public static int getInt(String key, int def) {
        SharedPreferences p = sp();
        if (p == null) return def;
        try {
            return Integer.parseInt(p.getString(key, String.valueOf(def)).trim());
        } catch (Throwable t) {
            return def;
        }
    }

    public static String getString(String key, String def) {
        SharedPreferences p = sp();
        return p == null ? def : p.getString(key, def);
    }

    public static void setString(String key, String value) {
        SharedPreferences p = sp();
        if (p != null) p.edit().putString(key, value).apply();
    }

    public static void setInt(String key, int value) {
        setString(key, String.valueOf(value));
    }

    public static int accentColor() {
        return getInt(ACCENT_COLOR, DEFAULT_ACCENT);
    }

    public static String exportAll() {
        StringBuilder sb = new StringBuilder("tiktokkk.v1\n");
        for (String k : BOOL_DEFAULTS.keySet()) sb.append(k).append('=').append(is(k)).append('\n');
        sb.append(REGION).append('=').append(getString(REGION, "US")).append('\n');
        sb.append(LOCALE).append('=').append(getString(LOCALE, "")).append('\n');
        sb.append(ACCENT_COLOR).append('=').append(accentColor()).append('\n');
        sb.append(FAB_OPACITY).append('=').append(getInt(FAB_OPACITY, 25)).append('\n');
        return sb.toString();
    }

    public static boolean importAll(String data) {
        SharedPreferences p = sp();
        if (data == null || p == null) return false;
        SharedPreferences.Editor e = p.edit();
        int n = 0;
        for (String line : data.split("\\r?\\n")) {
            int i = line.indexOf('=');
            if (i <= 0) continue;
            String k = line.substring(0, i).trim();
            String v = line.substring(i + 1).trim();
            if (BOOL_DEFAULTS.containsKey(k)) {
                e.putBoolean(k, "true".equalsIgnoreCase(v));
                n++;
            } else if (REGION.equals(k) || LOCALE.equals(k) || ACCENT_COLOR.equals(k)
                    || FAB_OPACITY.equals(k)) {
                e.putString(k, v);
                n++;
            }
        }
        e.apply();
        return n > 0;
    }

    public static void resetAll() {
        SharedPreferences p = sp();
        if (p != null) p.edit().clear().apply();
    }

    public static String regionCode() {
        return getString(REGION, "US").trim().toUpperCase(java.util.Locale.ROOT);
    }

    private Prefs() {}
}
