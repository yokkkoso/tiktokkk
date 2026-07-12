package me.yokkkoso.tiktokkk.links;

import me.yokkkoso.tiktokkk.Prefs;
import me.yokkkoso.tiktokkk.TikToKKK;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.net.Uri;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;

public final class UrlSanitizer {

    private static final Pattern URL = Pattern.compile("https?://\\S+");

    public static void install(ClassLoader cl) {
        try {
            XposedHelpers.findAndHookMethod(ClipboardManager.class, "setPrimaryClip",
                    ClipData.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) {
                    if (!Prefs.is(Prefs.SANITIZE_LINKS)) return;
                    try {
                        ClipData in = (ClipData) param.args[0];
                        if (in == null || in.getItemCount() == 0) return;
                        CharSequence text = in.getItemAt(0).getText();
                        if (text == null) return;
                        String cleaned = clean(text.toString());
                        if (!cleaned.equals(text.toString())) {
                            param.args[0] = ClipData.newPlainText(in.getDescription().getLabel(), cleaned);
                        }
                    } catch (Throwable ignored) {}
                }
            });
        } catch (Throwable t) {
            TikToKKK.log("url sanitizer failed: " + t);
        }
    }

    static String clean(String s) {
        Matcher m = URL.matcher(s);
        StringBuffer out = new StringBuffer();
        while (m.find()) {
            m.appendReplacement(out, Matcher.quoteReplacement(stripQuery(m.group())));
        }
        m.appendTail(out);
        return out.toString();
    }

    private static String stripQuery(String url) {
        try {
            Uri u = Uri.parse(url);
            String host = u.getHost();
            if (host == null || !host.contains("tiktok")) return url;
            if (u.getQuery() == null) return url;
            return new Uri.Builder()
                    .scheme(u.getScheme())
                    .authority(u.getAuthority())
                    .path(u.getPath())
                    .build()
                    .toString();
        } catch (Throwable t) {
            return url;
        }
    }

    private UrlSanitizer() {}
}
