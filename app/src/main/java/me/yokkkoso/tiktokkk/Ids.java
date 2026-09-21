package me.yokkkoso.tiktokkk;

import android.content.Context;
import android.view.View;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class Ids {
    public static final String TITLE = "title";
    public static final String POST_TIME = "tv_post_time";
    public static final String SEARCH_AI_LABEL = "tv_tab_tako_entrance";
    public static final String LIKE_ICON_ACTIVE = "video_like_icon_active";
    public static final String LIKE_ICON_INACTIVE = "video_like_icon_inactive";
    public static final List<String> STORY_MARKERS = Arrays.asList(
            "vp_story_collection", "vp_story_immersive_feed");
    public static final List<String> QUICK_REPOST = Arrays.asList("tv_upvote");
    public static final String FIND_SIMILAR_LABEL = "fb";
    public static final List<String> FIND_SIMILAR_BOX = Arrays.asList("bq", "br");

    public static List<String> LIKE_BTN;
    public static List<String> FOLLOW_BTN;
    public static List<String> CREATE_TAB;
    public static List<String> FRIENDS_TAB;
    public static List<String> INBOX_TAB;
    public static List<String> AI_ASSISTANT;
    public static List<String> AVATAR_IMG;
    public static List<String> COMMENT_SHEET;
    public static List<String> QUICK_SHARE;
    public static String PROFILE_COUNT;
    public static String SEARCH_BAR_SUGGEST;
    public static String SEARCH_BAR_ROOT;
    public static String SEARCH_AI_BOX;
    public static String VIEWER_CLOSE;
    public static String STICKER_REPORT;
    public static String STICKER_CLOSE;
    public static String DRAWER_LIST;

    private static final int[] COLUMN_MIN_VERSION = {
            HostVersion.V46_0_3, HostVersion.V46_6_3, HostVersion.V46_9_3,
    };

    private static int column = COLUMN_MIN_VERSION.length - 1;
    private static int appliedVc = -1;

    static {
        apply(HostVersion.code());
    }

    public static synchronized void init(Context ctx) {
        HostVersion.init(ctx);
        int vc = HostVersion.code();
        if (vc == appliedVc) return;
        apply(vc);
        TikToKKK.log("ids: host versionCode " + vc + " -> column " + column);
    }

    private static synchronized void apply(int versionCode) {
        appliedVc = versionCode;
        column = 0;
        for (int i = COLUMN_MIN_VERSION.length - 1; i >= 0; i--) {
            if (versionCode >= COLUMN_MIN_VERSION[i]) {
                column = i;
                break;
            }
        }

        LIKE_BTN = l("ftu,fu0,fu1,fu_,fu2", "g2c,g2i,g2j,g2s,g2k", "g65,g6a,g6b,g6k,g6c");
        FOLLOW_BTN = l("idu,iek,ieu", "ip3,ipt,iqa", "ium,ivc,ivu");
        CREATE_TAB = l("nw4", "of9", "olt");
        FRIENDS_TAB = l("nw7", "ofb", "olw");
        INBOX_TAB = l("nw9", "ofd", "oly");
        COMMENT_SHEET = l("ecj,ec6,i4h", "ejd,ei8,ieb", "em3,eky,ijv");
        QUICK_SHARE = l("xz0", "ysf", "z6f");
        AI_ASSISTANT = merge(
                Arrays.asList("tako", "vs_tako_entrance", "aigc_entrance",
                        "view_stub_ep_aigc_entrance"),
                l("x_n,xvh", "y3n,yov", "yg7,z2v"));
        AVATAR_IMG = merge(Arrays.asList("user_avatar"), l("vnh", "wfq", "ws6"));

        PROFILE_COUNT = s("s5x", "svt", "t56");
        SEARCH_BAR_SUGGEST = s("ubg", "v4e", "vf9");
        SEARCH_BAR_ROOT = s("ht2", "i3n", "i97");
        SEARCH_AI_BOX = s("iaf", "iln", "ir9");
        VIEWER_CLOSE = s("e2o", "e8m", "ea7");
        STICKER_REPORT = s("fri", "fzo", "g3g");
        STICKER_CLOSE = s("fqe", "fyk", "g2a");
        DRAWER_LIST = s("s2b", "su3", "t3e");
    }

    private static String s(String... perVersion) {
        return perVersion[column];
    }

    private static List<String> l(String... perVersion) {
        return Arrays.asList(perVersion[column].split(","));
    }

    private static List<String> merge(List<String> stable, List<String> obfuscated) {
        List<String> all = new ArrayList<>(stable);
        all.addAll(obfuscated);
        return all;
    }

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
