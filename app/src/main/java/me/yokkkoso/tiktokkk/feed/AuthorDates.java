package me.yokkkoso.tiktokkk.feed;

import me.yokkkoso.tiktokkk.Prefs;
import me.yokkkoso.tiktokkk.TikToKKK;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;

public final class AuthorDates {

    private static final String AWEME = "com.ss.android.ugc.aweme.feed.model.Aweme";
    private static final Map<String, Long> DATES = Collections.synchronizedMap(new HashMap<>());
    private static final Map<String, String> REGIONS = Collections.synchronizedMap(new HashMap<>());
    private static int STORES = 0;

    public static void install(ClassLoader cl) {
        try {
            Class<?> aweme = XposedHelpers.findClass(AWEME, cl);
            XposedHelpers.findAndHookMethod(aweme, "getVideo", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    try {
                        long t = XposedHelpers.getLongField(param.thisObject, "createTime");
                        if (t <= 0) return;
                        Object author = XposedHelpers.getObjectField(param.thisObject, "author");
                        if (author == null) return;
                        if (DATES.size() > 1500) {
                            DATES.clear();
                            REGIONS.clear();
                        }
                        String uid = strCall(author, "getUniqueId");
                        String nick = strCall(author, "getNickname");
                        put(uid, t);
                        put(nick, t);
                        String region = regionOf(param.thisObject, author);
                        putRegion(uid, region);
                        putRegion(nick, region);
                        if (Prefs.is(Prefs.DEBUG_CLICKS) && STORES++ < 5) {
                            TikToKKK.log("dates store uid=" + uid + " nick=" + nick + " t=" + t
                                    + " size=" + DATES.size());
                        }
                    } catch (Throwable ignored) {}
                }
            });
            TikToKKK.log("author dates hook installed (getVideo)");
        } catch (Throwable t) {
            TikToKKK.log("author dates hook failed: " + t);
        }
    }

    private static String strCall(Object o, String m) {
        try {
            Object r = XposedHelpers.callMethod(o, m);
            return r == null ? null : r.toString();
        } catch (Throwable t) {
            return null;
        }
    }

    private static void put(Object key, long t) {
        if (key == null) return;
        String s = key.toString().trim();
        if (!s.isEmpty()) DATES.put(s, t);
    }

    private static void putRegion(Object key, String region) {
        if (key == null || region == null) return;
        String k = key.toString().trim();
        String r = region.trim().toUpperCase(java.util.Locale.ROOT);
        if (!k.isEmpty() && r.length() == 2) REGIONS.put(k, r);
    }

    static String regionForHandle(String handle) {
        if (handle == null) return null;
        return REGIONS.get(handle.trim());
    }

    private static String regionOf(Object aweme, Object author) {
        String r = strCall(aweme, "getRegion");
        if (r == null || r.trim().isEmpty()) {
            try {
                Object f = XposedHelpers.getObjectField(aweme, "region");
                if (f != null) r = f.toString();
            } catch (Throwable ignored) {}
        }
        if (r == null || r.trim().isEmpty()) {
            r = strCall(author, "getRegion");
        }
        return r;
    }

    static long forHandle(String handle) {
        if (handle == null) return 0L;
        Long t = DATES.get(handle.trim());
        return t == null ? 0L : t;
    }

    private AuthorDates() {}
}
