package me.yokkkoso.tiktokkk.feed;

import me.yokkkoso.tiktokkk.FeatureStatus;
import me.yokkkoso.tiktokkk.Prefs;
import me.yokkkoso.tiktokkk.TikToKKK;

import android.os.Bundle;
import android.text.InputFilter;
import android.view.View;
import android.widget.TextView;

import java.lang.reflect.Field;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;

public final class RepostLimit {

    public static final String FEATURE = "Unlimited repost note";

    private static final String FRAGMENT =
            "com.ss.android.ugc.aweme.feed.adapter.widget.repost.RepostAddNoteInputFragment";

    public static void install(ClassLoader cl) {
        try {
            XposedHelpers.findAndHookMethod(FRAGMENT, cl, "onViewCreated",
                    View.class, Bundle.class, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    if (!Prefs.is(Prefs.REPOST_NO_LIMIT)) return;
                    clearFilters(param.thisObject);
                }
            });
            FeatureStatus.ok(FEATURE, "RepostAddNoteInputFragment.onViewCreated");
        } catch (Throwable t) {
            FeatureStatus.failed(FEATURE, "repost note fragment not found");
            TikToKKK.log("repost limit install failed: " + t);
        }
    }

    // The note input is a private field of the fragment whose name is obfuscated per build; the
    // length cap is the only InputFilter TikTok installs here, so drop filters from whichever
    // text view carries them.
    private static void clearFilters(Object fragment) {
        int cleared = 0;
        for (Field f : fragment.getClass().getDeclaredFields()) {
            if (!TextView.class.isAssignableFrom(f.getType())) continue;
            try {
                f.setAccessible(true);
                Object v = f.get(fragment);
                if (!(v instanceof TextView)) continue;
                TextView tv = (TextView) v;
                InputFilter[] filters = tv.getFilters();
                if (filters == null || filters.length == 0) continue;
                tv.setFilters(new InputFilter[0]);
                cleared++;
            } catch (Throwable ignored) {}
        }
        if (cleared > 0) TikToKKK.log("repost limit: cleared filters on " + cleared + " field(s)");
    }

    private RepostLimit() {}
}
