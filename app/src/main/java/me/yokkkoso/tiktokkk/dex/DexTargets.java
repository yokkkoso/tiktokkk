package me.yokkkoso.tiktokkk.dex;

import me.yokkkoso.tiktokkk.TikToKKK;

import org.luckypray.dexkit.DexKitBridge;
import org.luckypray.dexkit.query.FindClass;
import org.luckypray.dexkit.query.FindMethod;
import org.luckypray.dexkit.query.matchers.ClassMatcher;
import org.luckypray.dexkit.query.matchers.MethodMatcher;
import org.luckypray.dexkit.result.ClassData;
import org.luckypray.dexkit.result.MethodData;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public final class DexTargets {
    public static final String COMMENT_WIDGET_FALLBACK = "X.0jeh";

    public static final String SEEKBAR_FALLBACK = "X.0zo3";

    public static final String REFRESH_ENUM_FALLBACK = "X.0z80";

    public static final String SEARCH_FRAGMENT =
            "com.ss.android.ugc.aweme.search.pages.result.common.core.ui.fragment.SearchFragment";

    public static List<Class<?>> commentWidgets(DexKitBridge b, ClassLoader cl) {
        List<Class<?>> cached = new ArrayList<>();
        for (int i = 0; ; i++) {
            Class<?> c = DexCache.loadClass("commentWidget" + i, cl);
            if (c == null) break;
            cached.add(c);
        }
        if (!cached.isEmpty()) return cached;

        List<Class<?>> found = new ArrayList<>();
        if (b != null) {
            try {
                List<ClassData> hits = b.findClass(FindClass.create().matcher(ClassMatcher.create()
                        .usingStrings("LikeAndHateView", "onLikeClicked: comment id ")));
                for (ClassData d : hits) {
                    Class<?> c = d.getInstance(cl);
                    if (c != null) found.add(c);
                }
            } catch (Throwable t) {
                TikToKKK.log("dexkit: commentWidgets search failed (" + t + ")");
            }
        }
        if (found.isEmpty()) {
            try {
                found.add(cl.loadClass(COMMENT_WIDGET_FALLBACK));
            } catch (Throwable t) {
                TikToKKK.log("dexkit: commentWidget fallback " + COMMENT_WIDGET_FALLBACK + " missing too");
                return found;
            }
        } else {
            for (int i = 0; i < found.size(); i++) DexCache.saveClass("commentWidget" + i, found.get(i));
        }
        for (Class<?> c : found) TikToKKK.log("dexkit: commentWidget -> " + c.getName());
        return found;
    }

    public static Class<?> seekBar(DexKitBridge b, ClassLoader cl) {
        return resolveClass(b, cl, "seekBar", SEEKBAR_FALLBACK,
                ClassMatcher.create().usingStrings("seekbar show type change, change to:"));
    }

    public static Class<?> refreshEnum(DexKitBridge b, ClassLoader cl) {
        return resolveClass(b, cl, "refreshEnum", REFRESH_ENUM_FALLBACK,
                ClassMatcher.create().usingStrings(
                        "CLICK_TOP", "CLICK_BOTTOM", "PULL_DOWN_REFRESH", "pull_down"));
    }

    public static Method commentLike(DexKitBridge b, ClassLoader cl, Class<?> widget) {
        if (widget == null) return null;
        return resolveMethod(b, cl, "commentLike@" + widget.getName(),
                MethodMatcher.create()
                        .declaredClass(widget.getName())
                        .usingStrings("onLikeClicked: comment id ")
                        .paramTypes("java.lang.String")
                        .returnType("void"));
    }

    public static Method commentDislike(DexKitBridge b, ClassLoader cl, Class<?> widget) {
        if (widget == null) return null;
        return resolveMethod(b, cl, "commentDislike@" + widget.getName(),
                MethodMatcher.create()
                        .declaredClass(widget.getName())
                        .usingStrings("like_comment")
                        .paramCount(0)
                        .returnType("void"));
    }

    public static Method searchPush(DexKitBridge b, ClassLoader cl) {
        return searchPush(b, cl, "searchPush", "setData");
    }

    public static Method searchPushMore(DexKitBridge b, ClassLoader cl) {
        return searchPush(b, cl, "searchPushMore", "setDataAfterLoadMore");
    }

    // ConvertHelper turns the protobuf feed response into a FeedItemList and is the path a warm
    // disk cache takes. Its methods cannot be enumerated reflectively (getDeclaredMethods resolves
    // parameter types the module's classloader cannot see), so resolve only the ones we want.
    public static List<Method> feedConverters(DexKitBridge b, ClassLoader cl) {
        List<Method> out = new ArrayList<>();
        for (int i = 0; ; i++) {
            Method m = DexCache.loadMethod("feedConverter" + i, cl);
            if (m == null) break;
            out.add(m);
        }
        if (!out.isEmpty()) return out;
        if (b == null) return out;
        try {
            for (MethodData d : b.findMethod(FindMethod.create().matcher(MethodMatcher.create()
                    .declaredClass("com.ss.android.ugc.tiktok.ConvertHelper")
                    .returnType("com.ss.android.ugc.aweme.feed.model.FeedItemList")))) {
                try {
                    Method m = d.getMethodInstance(cl);
                    m.setAccessible(true);
                    DexCache.saveMethod("feedConverter" + out.size(), m);
                    out.add(m);
                } catch (Throwable ignored) {}
            }
        } catch (Throwable t) {
            TikToKKK.log("dexkit: feedConverters search failed (" + t + ")");
        }
        return out;
    }

    public static Method splashAdColdStart(DexKitBridge b, ClassLoader cl) {
        return resolveConcrete(b, cl, "splashAdColdStart",
                MethodMatcher.create()
                        .name("checkSplashAdsForColdStart")
                        .paramTypes("android.content.Intent")
                        .returnType("void"));
    }

    private static Method resolveConcrete(DexKitBridge b, ClassLoader cl, String key,
                                          MethodMatcher matcher) {
        Method cached = DexCache.loadMethod(key, cl);
        if (cached != null) return cached;
        if (b == null) return null;
        try {
            List<Method> concrete = new ArrayList<>();
            for (MethodData d : b.findMethod(FindMethod.create().matcher(matcher))) {
                Method m = d.getMethodInstance(cl);
                if (!java.lang.reflect.Modifier.isAbstract(m.getModifiers())) concrete.add(m);
            }
            if (concrete.size() != 1) {
                TikToKKK.log("dexkit: " + key + " matched " + concrete.size() + " methods, skipped");
                return null;
            }
            Method m = concrete.get(0);
            m.setAccessible(true);
            DexCache.saveMethod(key, m);
            TikToKKK.log("dexkit: " + key + " -> " + m.getDeclaringClass().getName() + "." + m.getName());
            return m;
        } catch (Throwable t) {
            TikToKKK.log("dexkit: " + key + " search failed (" + t + ")");
            return null;
        }
    }

    private static Method searchPush(DexKitBridge b, ClassLoader cl, String key, String adapterCall) {
        return resolveMethod(b, cl, key,
                MethodMatcher.create()
                        .declaredClass(SEARCH_FRAGMENT)
                        .paramTypes("java.util.List", "boolean")
                        .returnType("void")
                        .addInvoke(MethodMatcher.create().name(adapterCall)));
    }

    private static Class<?> resolveClass(DexKitBridge b, ClassLoader cl, String key,
                                         String fallback, ClassMatcher matcher) {
        Class<?> cached = DexCache.loadClass(key, cl);
        if (cached != null) return cached;

        Class<?> found = null;
        if (b != null) {
            try {
                List<ClassData> hits = b.findClass(FindClass.create().matcher(matcher));
                if (hits.size() == 1) {
                    found = hits.get(0).getInstance(cl);
                } else {
                    TikToKKK.log("dexkit: " + key + " matched " + hits.size() + " classes, using fallback");
                }
            } catch (Throwable t) {
                TikToKKK.log("dexkit: " + key + " search failed (" + t + ")");
            }
        }
        if (found != null) {
            DexCache.saveClass(key, found);
            TikToKKK.log("dexkit: " + key + " -> " + found.getName());
            return found;
        }
        try {
            return cl.loadClass(fallback);
        } catch (Throwable t) {
            TikToKKK.log("dexkit: " + key + " fallback " + fallback + " missing too");
            return null;
        }
    }

    private static Method resolveMethod(DexKitBridge b, ClassLoader cl, String key,
                                        MethodMatcher matcher) {
        Method cached = DexCache.loadMethod(key, cl);
        if (cached != null) return cached;
        if (b == null) return null;
        try {
            List<MethodData> hits = b.findMethod(FindMethod.create().matcher(matcher));
            if (hits.size() != 1) {
                TikToKKK.log("dexkit: " + key + " matched " + hits.size() + " methods, skipped");
                return null;
            }
            Method m = hits.get(0).getMethodInstance(cl);
            m.setAccessible(true);
            DexCache.saveMethod(key, m);
            TikToKKK.log("dexkit: " + key + " -> " + m.getDeclaringClass().getName() + "." + m.getName());
            return m;
        } catch (Throwable t) {
            TikToKKK.log("dexkit: " + key + " search failed (" + t + ")");
            return null;
        }
    }

    private DexTargets() {}
}
