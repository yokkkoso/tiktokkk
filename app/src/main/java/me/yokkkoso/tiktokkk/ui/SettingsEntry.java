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

// Inject a "tiktokkk" row into TikTok's profile side-drawer (list id s2b) that opens the settings
// dialog - a native-looking alternative to the floating gear button.
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

    private static void inject(View v) {
        if (!(v instanceof ViewGroup) || !Ids.DRAWER_LIST.equals(Ids.nameOf(v))) return;
        final ViewGroup list = (ViewGroup) v;
        // add once per drawer instance (native rows land shortly after the list itself)
        MAIN.postDelayed(() -> {
            try {
                if (list.findViewWithTag(TAG) != null) return;
                list.addView(row(list.getContext()));   // append -> under the last item (Settings & privacy)
            } catch (Throwable ignored) {}
        }, 400);
    }

    private static TextView row(Context c) {
        TextView row = new TextView(c);
        row.setTag(TAG);
        row.setText("⚙️  tiktokkk - Mod settings");
        row.setTextColor(0xFFFFFFFF);
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

    private static int dp(Context c, int v) {
        return Math.round(v * c.getResources().getDisplayMetrics().density);
    }

    private SettingsEntry() {}
}
