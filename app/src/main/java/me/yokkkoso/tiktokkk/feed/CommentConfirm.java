package me.yokkkoso.tiktokkk.feed;

import me.yokkkoso.tiktokkk.FeatureStatus;
import me.yokkkoso.tiktokkk.Loc;
import me.yokkkoso.tiktokkk.Prefs;
import me.yokkkoso.tiktokkk.Reflect;
import me.yokkkoso.tiktokkk.TikToKKK;
import me.yokkkoso.tiktokkk.dex.DexTargets;

import org.luckypray.dexkit.DexKitBridge;

import android.app.Activity;
import android.app.AlertDialog;

import java.util.ArrayList;
import java.util.List;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

public final class CommentConfirm {
    public static final String FEATURE = "Confirm comment like / dislike";

    private static final String COMMENT_MODEL = "com.ss.android.ugc.aweme.comment.model.Comment";
    private static final String LIKE_FALLBACK = "LJ";
    private static final String DISLIKE_FALLBACK = "LIZLLL";
    private static final String FIELD_FALLBACK = "LLILLL";

    private static volatile Activity top;
    private static final java.util.Map<Class<?>, Field> COMMENT_FIELDS =
            new java.util.concurrent.ConcurrentHashMap<>();
    private static final ThreadLocal<Boolean> BYPASS = new ThreadLocal<>();

    public static void install(ClassLoader cl, DexKitBridge bridge) {
        try {
            XposedHelpers.findAndHookMethod(Activity.class, "onResume", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    top = (Activity) param.thisObject;
                }
            });
        } catch (Throwable ignored) {}

        List<Class<?>> widgets = DexTargets.commentWidgets(bridge, cl);
        if (widgets.isEmpty()) {
            FeatureStatus.failed(FEATURE, "LikeAndHateView not found");
            return;
        }

        int handlers = 0;
        List<String> wired = new ArrayList<>();
        for (Class<?> widget : widgets) {
            Field field = commentField(widget, cl);
            if (field == null) {
                TikToKKK.log("comment confirm: no Comment field on " + widget.getName() + ", skipped");
                continue;
            }
            COMMENT_FIELDS.put(widget, field);
            int n = 0;
            n += hook(DexTargets.commentLike(bridge, cl, widget), widget, LIKE_FALLBACK, true);
            n += hook(DexTargets.commentDislike(bridge, cl, widget), widget, DISLIKE_FALLBACK, false);
            if (n > 0) wired.add(widget.getName() + "(" + n + ")");
            handlers += n;
        }
        if (handlers == 0) {
            FeatureStatus.failed(FEATURE, "no tap handler hooked");
        } else {
            FeatureStatus.ok(FEATURE, handlers + " handler(s) on " + wired.size() + " widget(s)");
        }
        TikToKKK.log("comment confirm: " + wired);
    }

    private static int hook(Method resolved, Class<?> widget, String fallbackName, boolean likeBtn) {
        if (resolved != null) {
            try {
                XposedBridge.hookMethod(resolved, handler(likeBtn));
                return 1;
            } catch (Throwable t) {
                TikToKKK.log("comment confirm: hookMethod failed for " + resolved.getName());
            }
        }
        try {
            return XposedBridge.hookAllMethods(widget, fallbackName, handler(likeBtn)).size();
        } catch (Throwable t) {
            return 0;
        }
    }

    private static Field commentField(Class<?> widget, ClassLoader cl) {
        Class<?> model;
        try {
            model = cl.loadClass(COMMENT_MODEL);
        } catch (Throwable t) {
            model = null;
        }
        if (model != null) {
            Field hit = null;
            int n = 0;
            for (Field f : widget.getDeclaredFields()) {
                if (f.getType() == model) {
                    hit = f;
                    n++;
                }
            }
            if (n == 1) {
                hit.setAccessible(true);
                return hit;
            }
            TikToKKK.log("comment confirm: " + n + " Comment fields, using name fallback");
        }
        try {
            Field f = widget.getDeclaredField(FIELD_FALLBACK);
            f.setAccessible(true);
            return f;
        } catch (Throwable t) {
            return null;
        }
    }

    private static XC_MethodHook handler(final boolean likeBtn) {
        return new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                if (Boolean.TRUE.equals(BYPASS.get())) return;
                try {
                    boolean pref = likeBtn ? Prefs.is(Prefs.CONFIRM_COMMENT_LIKE)
                            : Prefs.is(Prefs.CONFIRM_DISLIKE_COMMENT);
                    if (!pref) return;
                    Field field = COMMENT_FIELDS.get(param.thisObject.getClass());
                    if (field == null) return;
                    Object comment = field.get(param.thisObject);
                    if (comment == null) return;
                    int state = likeBtn ? Reflect.intVal(comment, "getUserDigged")
                            : Reflect.intVal(comment, "getUserBuried");
                    if (state != 0) return;
                    final Activity a = top;
                    if (a == null || a.isFinishing()) return;

                    param.setResult(null);
                    final Object viewObj = param.thisObject;
                    final Object[] args = param.args.clone();
                    final Method m = (Method) param.method;
                    final String msg = Loc.t(likeBtn ? "Like this comment?" : "Dislike this comment?");
                    a.runOnUiThread(() -> {
                        try {
                            new AlertDialog.Builder(a, android.R.style.Theme_Material_Dialog_Alert)
                                    .setMessage(msg)
                                    .setNegativeButton(android.R.string.cancel, null)
                                    .setPositiveButton(android.R.string.ok,
                                            (d, w) -> replay(m, viewObj, args))
                                    .show();
                        } catch (Throwable ignored) {}
                    });
                } catch (Throwable ignored) {}
            }
        };
    }

    private static void replay(Method m, Object viewObj, Object[] args) {
        BYPASS.set(Boolean.TRUE);
        try {
            m.setAccessible(true);
            m.invoke(viewObj, args);
        } catch (Throwable ignored) {
        } finally {
            BYPASS.set(Boolean.FALSE);
        }
    }

    private CommentConfirm() {}
}
