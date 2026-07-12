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

import me.yokkkoso.tiktokkk.Ids;
import me.yokkkoso.tiktokkk.Prefs;
import me.yokkkoso.tiktokkk.TikToKKK;
import me.yokkkoso.tiktokkk.download.VideoDownloader;

// The ⤓ download button. Settings now live in the profile-drawer entry (see SettingsEntry).
public final class OverlayFab {

    private static final String DL_TAG = "kkk_dl";
    private static final android.graphics.Rect RC = new android.graphics.Rect();

    public static void installTrigger() {
        try {
            XposedHelpers.findAndHookMethod(Activity.class, "onResume", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    try {
                        Activity a = (Activity) param.thisObject;
                        TikToKKK.logHostVersion(a);
                        addDownloadButton(a);
                    } catch (Throwable ignored) {}
                }
            });
        } catch (Throwable t) {
            TikToKKK.log("fab trigger failed: " + t);
        }
    }

    private static void addDownloadButton(Activity a) {
        ViewGroup decor = (ViewGroup) a.getWindow().getDecorView();
        if (decor.findViewWithTag(DL_TAG) != null) return;

        TextView dl = new TextView(a);
        dl.setTag(DL_TAG);
        dl.setText("⤓");
        dl.setTextColor(Theme.TEXT);
        dl.setTextSize(TypedValue.COMPLEX_UNIT_SP, 22);
        dl.setGravity(Gravity.CENTER);
        GradientDrawable bg = new GradientDrawable();
        bg.setShape(GradientDrawable.OVAL);
        bg.setColor(fabColor());
        dl.setBackground(bg);
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(Theme.dp(a, 48), Theme.dp(a, 48));
        lp.gravity = Gravity.TOP | Gravity.END;
        lp.topMargin = Theme.dp(a, 150);
        lp.rightMargin = Theme.dp(a, 10);
        dl.setLayoutParams(lp);
        dl.setVisibility(View.GONE);
        dl.setOnClickListener(v -> VideoDownloader.download(a));
        decor.addView(dl);

        final ViewGroup d = decor;
        final Runnable upd = () -> updateFab(d);
        d.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            d.removeCallbacks(upd);
            d.postDelayed(upd, 350);
        });
        d.post(upd);
    }

    private static void updateFab(ViewGroup decor) {
        try {
            View down = decor.findViewWithTag(DL_TAG);
            if (down == null) return;
            if (down.getBackground() instanceof GradientDrawable) {
                ((GradientDrawable) down.getBackground()).setColor(fabColor());
            }
            boolean post = onPost(decor, 0);
            down.setVisibility(post && Prefs.is(Prefs.SHOW_DL_BUTTON) ? View.VISIBLE : View.GONE);
        } catch (Throwable ignored) {}
    }

    // A real post player shows the interaction-rail LIKE button (obfuscated ids, language-independent).
    private static boolean onPost(View v, int depth) {
        if (v == null || depth > 32 || v.getVisibility() != View.VISIBLE) return false;
        if (v.getGlobalVisibleRect(RC) && Ids.LIKE_BTN.contains(Ids.nameOf(v))) return true;
        if (v instanceof ViewGroup) {
            ViewGroup g = (ViewGroup) v;
            for (int i = 0; i < g.getChildCount(); i++) {
                if (onPost(g.getChildAt(i), depth + 1)) return true;
            }
        }
        return false;
    }

    // Black with user-selected alpha (opacity %). Default 25% (the old FAB_BG).
    private static int fabColor() {
        int pct = Math.max(0, Math.min(100, Prefs.getInt(Prefs.FAB_OPACITY, 25)));
        return (pct * 255 / 100) << 24;
    }

    private OverlayFab() {}
}
