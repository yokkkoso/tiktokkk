package me.yokkkoso.tiktokkk.feed;

import me.yokkkoso.tiktokkk.FeatureStatus;
import me.yokkkoso.tiktokkk.Prefs;
import me.yokkkoso.tiktokkk.TikToKKK;

import android.content.ClipData;
import android.content.ClipboardManager;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;

public final class CommentCopy {

    public static final String FEATURE = "Copy comment without username";

    private static final Pattern AUTHOR_PREFIX = Pattern.compile("^@([^:\\n]{1,60}):[ \\t]*");

    public static void install(ClassLoader cl) {
        XposedHelpers.findAndHookMethod(ClipboardManager.class, "setPrimaryClip",
                ClipData.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                boolean debug = Prefs.is(Prefs.DEBUG_CLICKS);
                if (!Prefs.is(Prefs.COPY_COMMENT_NO_AUTHOR) && !debug) return;
                try {
                    ClipData in = (ClipData) param.args[0];
                    if (in == null || in.getItemCount() == 0) return;
                    CharSequence text = in.getItemAt(0).getText();
                    if (text == null) return;
                    if (debug) TikToKKK.log("comment copy: clip=[" + text + "]");
                    String stripped = strip(text.toString());
                    if (!stripped.equals(text.toString())) {
                        param.args[0] =
                                ClipData.newPlainText(in.getDescription().getLabel(), stripped);
                    }
                } catch (Throwable ignored) {}
            }
        });
        FeatureStatus.ok(FEATURE, "clipboard");
    }

    // TikTok copies a comment as "@author:text"; anything else is left untouched.
    static String strip(String clip) {
        if (!Prefs.is(Prefs.COPY_COMMENT_NO_AUTHOR)) return clip;
        Matcher m = AUTHOR_PREFIX.matcher(clip);
        if (!m.find()) return clip;
        String rest = clip.substring(m.end());
        if (rest.trim().isEmpty()) return clip;
        TikToKKK.log("comment copy: dropped author '" + m.group(1) + "'");
        return rest;
    }

    private CommentCopy() {}
}
