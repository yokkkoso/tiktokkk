package me.yokkkoso.tiktokkk.ui;

import android.app.Activity;
import android.graphics.drawable.GradientDrawable;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;

import me.yokkkoso.tiktokkk.Prefs;
import me.yokkkoso.tiktokkk.TikToKKK;
import me.yokkkoso.tiktokkk.download.VideoDownloader;

public final class OverlayFab {

    public static void installTrigger() {
        try {
            XposedHelpers.findAndHookMethod(Activity.class, "onResume", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    try {
                        Activity a = (Activity) param.thisObject;
                        TikToKKK.logHostVersion(a);
                        addFloatingButton(a);
                    } catch (Throwable ignored) {}
                }
            });
        } catch (Throwable t) {
            TikToKKK.log("menu trigger failed: " + t);
        }
    }

    private static final String BTN_TAG = "kkk_fab";

    private static void addFloatingButton(Activity a) {
        ViewGroup decor = (ViewGroup) a.getWindow().getDecorView();
        if (decor.findViewWithTag(BTN_TAG) != null) return;
        Theme.ACCENT = Prefs.accentColor();

        TextView btn = new TextView(a);
        btn.setTag(BTN_TAG);
        btn.setText("⚙️");
        btn.setTextColor(Theme.TEXT);
        btn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        btn.setGravity(Gravity.CENTER);
        GradientDrawable bg = new GradientDrawable();
        bg.setShape(GradientDrawable.OVAL);
        bg.setColor(Theme.FAB_BG);
        btn.setBackground(bg);

        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(Theme.dp(a, 48), Theme.dp(a, 48));
        lp.gravity = Gravity.TOP | Gravity.END;
        lp.topMargin = Theme.dp(a, 100);
        lp.rightMargin = Theme.dp(a, 10);
        btn.setLayoutParams(lp);
        btn.setVisibility(View.GONE);
        btn.setOnClickListener(v -> Menu.show(a));
        decor.addView(btn);

        TextView dl = new TextView(a);
        dl.setTag("kkk_dl");
        dl.setText("⤓");
        dl.setTextColor(Theme.TEXT);
        dl.setTextSize(TypedValue.COMPLEX_UNIT_SP, 22);
        dl.setGravity(Gravity.CENTER);
        GradientDrawable dbg = new GradientDrawable();
        dbg.setShape(GradientDrawable.OVAL);
        dbg.setColor(Theme.FAB_BG);
        dl.setBackground(dbg);
        FrameLayout.LayoutParams dlp = new FrameLayout.LayoutParams(Theme.dp(a, 48), Theme.dp(a, 48));
        dlp.gravity = Gravity.TOP | Gravity.END;
        dlp.topMargin = Theme.dp(a, 150);
        dlp.rightMargin = Theme.dp(a, 10);
        dl.setLayoutParams(dlp);
        dl.setVisibility(View.GONE);
        dl.setOnClickListener(v -> VideoDownloader.download(a));
        decor.addView(dl);

        final ViewGroup d = decor;
        final Runnable upd = () -> updateFabs(d);
        d.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            d.removeCallbacks(upd);
            d.postDelayed(upd, 350);
        });
        d.post(upd);
    }

    private static void updateFabs(ViewGroup decor) {
        try {
            View kkk = decor.findViewWithTag(BTN_TAG);
            View down = decor.findViewWithTag("kkk_dl");
            if (kkk == null && down == null) return;
            boolean[] r = {false, false, false};   // {ownProfileOnScreen, videoPostOnScreen, anyProfileOnScreen}
            scanScreen(decor, r, 0);
            if (kkk != null) kkk.setVisibility((Menu.DEV || r[0]) ? View.VISIBLE : View.GONE);
            if (down != null) down.setVisibility(
                    r[1] && Prefs.is(Prefs.SHOW_DL_BUTTON) ? View.VISIBLE : View.GONE);
        } catch (Throwable ignored) {}
    }

    private static final android.graphics.Rect RC = new android.graphics.Rect();

    private static void scanScreen(View v, boolean[] r, int depth) {
        if (v == null || depth > 32 || (r[0] && r[1] && r[2])) return;
        if (v.getVisibility() != View.VISIBLE) return;
        if (!r[1]) {
            CharSequence cd = v.getContentDescription();
            if (cd != null) {
                String s = cd.toString().toLowerCase(java.util.Locale.ROOT);
                if ((s.contains("поставить лайк") || s.contains("вам понравилось")
                        || s.startsWith("like")) && v.getGlobalVisibleRect(RC)) r[1] = true;
            }
        }
        if (v instanceof TextView) {
            CharSequence t = ((TextView) v).getText();
            if (t != null) {
                String s = t.toString().trim().toLowerCase(java.util.Locale.ROOT);
                // own-profile-only affordances (other users' profiles show Follow/Message).
                // The logged-out profile tab shows a login CTA instead, so match that too — else
                // the settings button is unreachable before sign-in (e.g. to set the region first).
                if (!r[0] && (s.equals("tiktok studio") || s.equals("добавить описание")
                        || s.equals("есть мысли?") || s.equals("изменить профиль")
                        || s.equals("edit profile") || s.contains("студия")
                        || s.contains("войти") || s.contains("зарегистр")
                        || s.contains("log in") || s.contains("sign up"))
                        && v.getGlobalVisibleRect(RC)) r[0] = true;
                // any profile page (own or other) shows a "Подписчики"/"followers" count label;
                // the FYP itself only shows the "Подписки"/Following tab, so match followers only
                if (!r[2] && (s.contains("подписчик") || s.contains("follower"))
                        && v.getGlobalVisibleRect(RC)) r[2] = true;
            }
        }
        if (v instanceof ViewGroup) {
            ViewGroup g = (ViewGroup) v;
            for (int i = 0; i < g.getChildCount(); i++) scanScreen(g.getChildAt(i), r, depth + 1);
        }
    }

    public static void recolorFab(Activity a) {
        try {
            View fab = a.getWindow().getDecorView().findViewWithTag(BTN_TAG);
            if (fab != null && fab.getBackground() instanceof GradientDrawable) {
                ((GradientDrawable) fab.getBackground()).setColor(Theme.ACCENT);
            }
        } catch (Throwable ignored) {}
    }

    private OverlayFab() {}
}
