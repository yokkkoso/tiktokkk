package me.yokkkoso.tiktokkk.feed;

import me.yokkkoso.tiktokkk.Countries;
import me.yokkkoso.tiktokkk.Ids;
import me.yokkkoso.tiktokkk.Loc;
import me.yokkkoso.tiktokkk.Prefs;
import me.yokkkoso.tiktokkk.TikToKKK;

import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;

public final class FeedDecorator {

    private static final SimpleDateFormat FMT = new SimpleDateFormat("dd.MM.yyyy", Locale.US);
    private static final int DIM = 0x99FFFFFF;
    private static final long WEEK_MS = 7L * 24 * 3600 * 1000;
    private static final ThreadLocal<Boolean> REENTRY = new ThreadLocal<>();

    public static void install(ClassLoader cl) {
        try {
            XposedHelpers.findAndHookMethod(TextView.class, "setText",
                    CharSequence.class, TextView.BufferType.class, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    if (Boolean.TRUE.equals(REENTRY.get())) return;
                    TextView tv = (TextView) param.thisObject;
                    if (Prefs.is(Prefs.DEBUG_CLICKS)) debugLog(tv);
                    hideFindSimilar(tv);
                    hideSearchBar(tv);
                    hideSearchAiButton(tv);
                    String id = Ids.nameOf(tv);
                    if (id == null) return;
                    // Defer: the native tv_post_time date is populated shortly AFTER the title, so
                    // stamping immediately can't see it and would produce a double date. Let it land.
                    if (Ids.TITLE.equals(id)) {
                        final TextView t = tv;
                        t.postDelayed(() -> stampTitle(t), 350);
                    }
                }
            });
        } catch (Throwable t) {
            TikToKKK.log("feed decorator install failed: " + t);
        }
        try {
            // TikTok re-shows the fast-search bar after we hide it, so a one-shot GONE loses. Force
            // the bar's root (ht2) to stay hidden by intercepting every attempt to make it visible.
            XposedHelpers.findAndHookMethod(View.class, "setVisibility", int.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) {
                    if ((int) param.args[0] == View.VISIBLE
                            && Prefs.is(Prefs.HIDE_SEARCH_BAR)
                            && Ids.SEARCH_BAR_ROOT.equals(Ids.nameOf((View) param.thisObject))) {
                        param.args[0] = View.GONE;
                    }
                }
            });
        } catch (Throwable ignored) {}
    }

    private static void stampTitle(TextView tv) {
        // Flag shows everywhere; the custom DATE only where TikTok has no native date (FYP) — else
        // it would double the date on Following/detail posts (which carry a native tv_post_time).
        boolean wantDate = Prefs.is(Prefs.SHOW_FYP_TIMESTAMP) && !nativeDateNearby(tv);
        boolean wantFlag = Prefs.is(Prefs.SHOW_POST_REGION);
        if (!wantDate && !wantFlag) return;
        try {
            CharSequence t = tv.getText();
            if (t == null) return;
            String s = t.toString();
            if (s.isEmpty() || s.length() > 80 || s.contains("  ·  ")) return;   // last: already stamped

            String region = wantFlag ? AuthorDates.regionForHandle(s) : null;
            String flag = region != null && region.length() == 2 ? Countries.flag(region) : null;
            long ct = wantDate ? AuthorDates.forHandle(s) : 0;
            boolean hasDate = ct > 0;
            if (flag == null && !hasDate) return;

            String display = s.length() > 18 ? s.substring(0, 18).trim() + "…" : s;
            SpannableStringBuilder sb = new SpannableStringBuilder();
            if (flag != null) sb.append(flag).append("  ·  ");
            sb.append(display);
            if (hasDate) {
                int dateStart = sb.length();
                sb.append("  ·  ").append(formatDate(ct));
                sb.setSpan(new ForegroundColorSpan(DIM), dateStart, sb.length(),
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            REENTRY.set(Boolean.TRUE);
            try {
                tv.setText(sb);
            } finally {
                REENTRY.set(Boolean.FALSE);
            }
        } catch (Throwable ignored) {}
    }

    private static boolean nativeDateNearby(View title) {
        try {
            View c = title;
            for (int i = 0; i < 2 && c.getParent() instanceof View; i++) c = (View) c.getParent();
            return findPostTime(c, 0);
        } catch (Throwable t) {
            return false;
        }
    }

    private static boolean findPostTime(View v, int depth) {
        if (v == null || depth > 5) return false;
        // Only a VISIBLE, non-empty native date counts — the FYP layout keeps a hidden/empty
        // tv_post_time in the tree, which must not suppress our stamp.
        if (Ids.POST_TIME.equals(Ids.nameOf(v)) && v.getVisibility() == View.VISIBLE
                && v instanceof TextView) {
            CharSequence t = ((TextView) v).getText();
            if (t != null && t.length() > 0) return true;
        }
        if (v instanceof ViewGroup) {
            ViewGroup g = (ViewGroup) v;
            for (int i = 0; i < g.getChildCount(); i++) {
                if (findPostTime(g.getChildAt(i), depth + 1)) return true;
            }
        }
        return false;
    }

    private static String formatDate(long ctSeconds) {
        long postMs = ctSeconds * 1000L;
        long diff = System.currentTimeMillis() - postMs;
        if (diff < 0) diff = 0;
        return diff < WEEK_MS ? relative(diff) : FMT.format(new Date(postMs));
    }

    private static String relative(long diffMs) {
        long min = diffMs / 60000L;
        if (min < 1) return Loc.isRu() ? "только что" : "just now";
        if (min < 60) return min + (Loc.isRu() ? "м назад" : "m ago");
        long h = min / 60L;
        if (h < 24) return h + (Loc.isRu() ? "ч назад" : "h ago");
        long d = h / 24L;
        return d + (Loc.isRu() ? "д назад" : "d ago");
    }

    // 46.0.3: the "Find similar" tag = label id fb + icon fa inside container bq/br.
    private static void hideFindSimilar(TextView tv) {
        if (!Prefs.is(Prefs.HIDE_FIND_SIMILAR)) return;
        try {
            if (!Ids.FIND_SIMILAR_LABEL.equals(Ids.nameOf(tv))) return;
            View target = tv;
            for (int i = 0; i < 3 && target.getParent() instanceof View; i++) {
                target = (View) target.getParent();
                String id = Ids.nameOf(target);
                if (Ids.FIND_SIMILAR_BOX.contains(id)) {
                    target.setVisibility(View.GONE);
                    return;
                }
            }
        } catch (Throwable ignored) {}
    }

    // Match the search-suggestion TextView by id (ubg) and hide the bar's root container (ht2) —
    // resource-ids are language-independent, unlike the "Поиск ·"/"Search ·" text.
    private static void hideSearchBar(final TextView tv) {
        if (!Prefs.is(Prefs.HIDE_SEARCH_BAR)) return;
        try {
            if (!Ids.SEARCH_BAR_SUGGEST.equals(Ids.nameOf(tv))) return;
            Runnable hide = () -> {
                View cur = tv;
                for (int i = 0; i < 6 && cur.getParent() instanceof View; i++) {
                    cur = (View) cur.getParent();
                    if (Ids.SEARCH_BAR_ROOT.equals(Ids.nameOf(cur))
                            || (cur.isClickable() && cur.getHeight() < 400)) {
                        cur.setVisibility(View.GONE);
                        return;
                    }
                }
            };
            hide.run();
            tv.postDelayed(hide, 400);
        } catch (Throwable ignored) {}
    }

    // 46.0.3: the search "Ask"/Tako AI entrance = label id tv_tab_tako_entrance inside container iaf.
    private static void hideSearchAiButton(TextView tv) {
        if (!Prefs.is(Prefs.HIDE_AI_ASSISTANT)) return;
        try {
            if (!Ids.SEARCH_AI_LABEL.equals(Ids.nameOf(tv))) return;
            View cur = tv;
            for (int i = 0; i < 6 && cur.getParent() instanceof View; i++) {
                cur = (View) cur.getParent();
                if (Ids.SEARCH_AI_BOX.equals(Ids.nameOf(cur))) {
                    cur.setVisibility(View.GONE);
                    return;
                }
            }
        } catch (Throwable ignored) {}
    }

    private static void debugLog(TextView tv) {
        try {
            String id = Ids.nameOf(tv);
            if (id == null) return;
            CharSequence t = tv.getText();
            String txt = t == null ? "" : t.toString();
            if (txt.length() > 24) txt = txt.substring(0, 24);
            TikToKKK.log("TV id=" + id + " cls=" + tv.getClass().getSimpleName()
                    + " text=[" + txt + "]");
        } catch (Throwable ignored) {}
    }

    private FeedDecorator() {}
}
