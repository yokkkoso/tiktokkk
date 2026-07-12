package me.yokkkoso.tiktokkk.tabbar;

import me.yokkkoso.tiktokkk.Prefs;
import me.yokkkoso.tiktokkk.TikToKKK;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayDeque;
import java.util.Locale;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;

public final class TabBar {

    public static void install(ClassLoader cl) {
        try {
            XposedHelpers.findAndHookMethod(Activity.class, "onResume", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    try {
                        Activity a = (Activity) param.thisObject;
                        if (!a.getClass().getName().contains("MainActivity")) return;
                        View decor = a.getWindow().getDecorView();
                        decor.post(() -> apply(a));
                        decor.postDelayed(() -> apply(a), 600);
                    } catch (Throwable ignored) {}
                }
            });
            TikToKKK.log("tab bar hook installed");
        } catch (Throwable t) {
            TikToKKK.log("tab bar install failed: " + t);
        }
    }

    static void apply(Activity a) {
        boolean labels = Prefs.is(Prefs.HIDE_TAB_LABELS);
        boolean friendsB = Prefs.is(Prefs.HIDE_FRIENDS_BADGE);
        boolean inboxB = Prefs.is(Prefs.HIDE_INBOX_BADGE);
        if (!labels && !friendsB && !inboxB) return;
        try {
            View decor = a.getWindow().getDecorView();
            if (!(decor instanceof ViewGroup)) return;
            ViewGroup bar = findTabBar((ViewGroup) decor);
            if (bar == null) return;
            for (int i = 0; i < bar.getChildCount(); i++) {
                View tab = bar.getChildAt(i);
                CharSequence dsc = tab.getContentDescription();
                if (dsc == null || !(tab instanceof ViewGroup)) continue;
                String name = dsc.toString();
                String d = name.toLowerCase(Locale.ROOT);
                boolean friends = d.contains("друз") || d.contains("friend");
                boolean inbox = d.contains("вход") || d.contains("inbox") || d.contains("сообщ")
                        || d.contains("уведомл") || d.contains("notif");
                processTab((ViewGroup) tab, name, labels,
                        (friends && friendsB) || (inbox && inboxB));
            }
        } catch (Throwable ignored) {}
    }

    // The tab bar = a ViewGroup with 4-6 children, most carrying a contentDescription (the tabs).
    private static ViewGroup findTabBar(ViewGroup root) {
        ArrayDeque<ViewGroup> q = new ArrayDeque<>();
        q.add(root);
        int scanned = 0;
        while (!q.isEmpty() && scanned < 5000) {
            ViewGroup g = q.poll();
            scanned++;
            int kids = g.getChildCount();
            int desc = 0;
            for (int i = 0; i < kids; i++) {
                View c = g.getChildAt(i);
                if (c.getContentDescription() != null) desc++;
                if (c instanceof ViewGroup) q.add((ViewGroup) c);
            }
            if (kids >= 4 && kids <= 6 && desc >= 4) return g;
        }
        return null;
    }

    private static void processTab(ViewGroup tab, String name, boolean hideLabel, boolean hideBadge) {
        for (int i = 0; i < tab.getChildCount(); i++) {
            View c = tab.getChildAt(i);
            if (c instanceof TextView) {
                CharSequence t = ((TextView) c).getText();
                String s = t == null ? "" : t.toString();
                if (s.equals(name)) {
                    c.setVisibility(hideLabel ? View.GONE : View.VISIBLE);
                } else if (!s.isEmpty() && hideBadge) {
                    c.setVisibility(View.GONE);
                }
            } else if (c instanceof ViewGroup) {
                processTab((ViewGroup) c, name, hideLabel, hideBadge);
            }
        }
    }

    private TabBar() {}
}
