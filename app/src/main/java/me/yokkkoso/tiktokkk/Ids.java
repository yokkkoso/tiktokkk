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
    public static final List<String> CREATE_TAB = Arrays.asList(
            "video_shoot_container", "create_aweme_button", "creation_entrance");
    public static final List<String> AI_ASSISTANT = Arrays.asList(
            "tako", "vs_tako_entrance", "aigc_entrance", "view_stub_ep_aigc_entrance", "x_n", "xvh");

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
