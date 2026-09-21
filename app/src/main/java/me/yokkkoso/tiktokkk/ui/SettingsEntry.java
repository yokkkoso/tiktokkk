package me.yokkkoso.tiktokkk.ui;

import me.yokkkoso.tiktokkk.Ids;
import me.yokkkoso.tiktokkk.TikToKKK;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;

public final class SettingsEntry {
    private static final String TAG = "kkk_settings_entry";
    private static final Handler MAIN = new Handler(Looper.getMainLooper());

    public static void install(ClassLoader cl) {
        XC_MethodHook cb = new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam p) {
                try {
                    inject((View) p.args[0]);
                } catch (Throwable ignored) {}
            }
        };
        try {
            XposedHelpers.findAndHookMethod(ViewGroup.class, "addView",
                    View.class, int.class, ViewGroup.LayoutParams.class, cb);
        } catch (Throwable ignored) {}
        try {
            XposedHelpers.findAndHookMethod(ViewGroup.class, "addViewInLayout",
                    View.class, int.class, ViewGroup.LayoutParams.class, boolean.class, cb);
        } catch (Throwable ignored) {}
        TikToKKK.log("settings entry installed");
    }

    private static void inject(View added) {
        final ViewGroup list = drawerList(added);
        if (list == null) return;
        MAIN.postDelayed(() -> {
            try {
                if (list.findViewWithTag(TAG) != null) return;

                list.addView(row(list.getContext(), rowTextColor(list)));
            } catch (Throwable ignored) {}
        }, 400);
    }

    private static ViewGroup drawerList(View v) {
        for (int i = 0; i < 4 && v != null; i++) {
            if (v instanceof ViewGroup && Ids.DRAWER_LIST.equals(Ids.nameOf(v))) return (ViewGroup) v;
            android.view.ViewParent p = v.getParent();
            v = (p instanceof View) ? (View) p : null;
        }
        return null;
    }

    private static int rowTextColor(View v) {
        if (v instanceof TextView) {
            CharSequence t = ((TextView) v).getText();
            if (t != null && t.length() > 0) return ((TextView) v).getCurrentTextColor();
        }
        if (v instanceof ViewGroup) {
            ViewGroup g = (ViewGroup) v;
            for (int i = 0; i < g.getChildCount(); i++) {
                Integer c = boxed(rowTextColor(g.getChildAt(i)));
                if (c != null) return c;
            }
        }
        return Integer.MIN_VALUE;
    }

    private static Integer boxed(int c) {
        return c == Integer.MIN_VALUE ? null : c;
    }

    private static TextView row(Context c, int color) {
        TextView row = new TextView(c);
        row.setTag(TAG);
        row.setText("⚙️  tiktokkk - Mod settings");
        row.setTextColor(color == Integer.MIN_VALUE ? resolvePrimaryText(c) : color);
        row.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        int h = dp(c, 14), s = dp(c, 24);
        row.setPadding(s, h, s, h);
        row.setClickable(true);
        row.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        row.setOnClickListener(view -> {
            Activity a = TikToKKK.activityOf(view.getContext());
            if (a != null) Menu.show(a);
        });
        return row;
    }

    private static int resolvePrimaryText(Context c) {
        try {
            android.util.TypedValue tv = new android.util.TypedValue();
            if (c.getTheme().resolveAttribute(android.R.attr.textColorPrimary, tv, true)) {
                int color = tv.data;
                if (tv.type == android.util.TypedValue.TYPE_STRING || color == 0) {
                    color = c.getResources().getColor(tv.resourceId, c.getTheme());
                }
                return color;
            }
        } catch (Throwable ignored) {}
        return 0xFF161823;
    }

    private static int dp(Context c, int v) {
        return Math.round(v * c.getResources().getDisplayMetrics().density);
    }

    private SettingsEntry() {}
}
