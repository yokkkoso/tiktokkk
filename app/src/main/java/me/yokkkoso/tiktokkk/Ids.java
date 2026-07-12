package me.yokkkoso.tiktokkk;

import android.view.View;

import java.util.Arrays;
import java.util.List;

public final class Ids {
    public static final List<String> COMMENT_LIKE = Arrays.asList(
            "comment_digg", "comment_like", "comment_praise");
    // 46.0.3 right-rail interaction buttons (obfuscated, per-version). Unique ids only —
    // fud is shared between like/share so it is intentionally excluded.
    public static final List<String> LIKE_BTN = Arrays.asList("ftu", "fu0", "fu1", "fu_", "fu2");
    public static final List<String> FOLLOW_BTN = Arrays.asList("idu", "iek", "ieu");
    // 46.0.3 bottom-nav tabs: Home nw8, Friends nw7, Create nw4, Inbox nw9, Profile nw_.
    public static final List<String> CREATE_TAB = Arrays.asList("nw4");
    public static final List<String> FRIENDS_TAB = Arrays.asList("nw7");
    public static final List<String> INBOX_TAB = Arrays.asList("nw9");
    public static final List<String> AI_ASSISTANT = Arrays.asList(
            "tako", "vs_tako_entrance", "aigc_entrance", "view_stub_ep_aigc_entrance", "x_n", "xvh");

    // 46.0.3 single-view ids (obfuscated, change per TikTok version) for language-independent detection.
    public static final String TITLE = "title";                       // FYP author/title button
    public static final String POST_TIME = "tv_post_time";            // native upload-date label
    public static final String PROFILE_COUNT = "s5x";                 // profile follower/following count
    public static final String SEARCH_BAR_SUGGEST = "ubg";            // inline search suggestion text
    public static final String SEARCH_BAR_ROOT = "ht2";               // inline search bar container
    public static final String FIND_SIMILAR_LABEL = "fb";             // "Find similar" tag label
    public static final String SEARCH_AI_LABEL = "tv_tab_tako_entrance"; // search "Ask"/Tako entrance
    public static final String SEARCH_AI_BOX = "iaf";                 // search AI button container
    public static final String VIEWER_CLOSE = "e2o";                  // fullscreen viewer close (X)
    public static final String STICKER_REPORT = "fri";               // sticker viewer report
    public static final String STICKER_CLOSE = "fqe";                 // sticker viewer close
    public static final List<String> FIND_SIMILAR_BOX = Arrays.asList("bq", "br");
    public static final List<String> AVATAR_IMG = Arrays.asList("user_avatar", "vnh");
    public static final List<String> COMMENT_SHEET = Arrays.asList("ecj", "ec6", "i4h");
    public static final String DRAWER_LIST = "s2b";                  // profile side-drawer menu list
    public static final List<String> STORY_MARKERS = Arrays.asList(
            "vp_story_collection", "vp_story_immersive_feed");        // story player container
    public static final String LIKE_ICON_ACTIVE = "video_like_icon_active";     // liked state
    public static final String LIKE_ICON_INACTIVE = "video_like_icon_inactive"; // not-liked state
    public static final List<String> QUICK_SHARE = Arrays.asList("xz0");        // "Share with <friend>" pill label
    public static final List<String> QUICK_REPOST = Arrays.asList("tv_upvote"); // "Repost to followers" label

    public static String nameOf(View v) {
        try {
            int id = v.getId();
            if (id == View.NO_ID) return null;
            return v.getResources().getResourceEntryName(id);
        } catch (Throwable t) {
            return null;
        }
    }

    public static boolean inSubtreeExact(View v, List<String> names, int depth) {
        if (v == null || depth < 0) return false;
        String n = nameOf(v);
        if (n != null && names.contains(n)) return true;
        if (v instanceof android.view.ViewGroup) {
            android.view.ViewGroup g = (android.view.ViewGroup) v;
            for (int i = 0; i < g.getChildCount(); i++) {
                if (inSubtreeExact(g.getChildAt(i), names, depth - 1)) return true;
            }
        }
        return false;
    }

    public static boolean inAncestryExact(View v, List<String> names, int depth) {
        for (int i = 0; i <= depth && v != null; i++) {
            String n = nameOf(v);
            if (n != null && names.contains(n)) return true;
            android.view.ViewParent p = v.getParent();
            v = (p instanceof View) ? (View) p : null;
        }
        return false;
    }

    public static boolean matches(View v, List<String> names) {
        String n = nameOf(v);
        if (n == null) return false;
        for (String s : names) {
            if (n.equals(s) || n.contains(s)) return true;
        }
        return false;
    }

    private Ids() {}
}
