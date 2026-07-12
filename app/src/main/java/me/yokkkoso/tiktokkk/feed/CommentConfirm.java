package me.yokkkoso.tiktokkk.feed;

import me.yokkkoso.tiktokkk.Loc;
import me.yokkkoso.tiktokkk.Prefs;
import me.yokkkoso.tiktokkk.Reflect;
import me.yokkkoso.tiktokkk.TikToKKK;

import android.app.Activity;
import android.app.AlertDialog;

import java.lang.reflect.Method;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

// Comment like/dislike don't go through View.performClick. The thumbs widget LX/0jeh
// ("LikeAndHateView") optimistically toggles itself then calls the ViewModel, so we must
// block at the widget's tap handlers: LJ(String)=like, LIZLLL()=dislike. The Comment is
// field LLILLL; getUserDigged()/getUserBuried() == 0 means this tap will ADD (not undo).
public final class CommentConfirm {

    private static volatile Activity top;
    private static final ThreadLocal<Boolean> BYPASS = new ThreadLocal<>();

    public static void install(ClassLoader cl) {
        try {
            XposedHelpers.findAndHookMethod(Activity.class, "onResume", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    top = (Activity) param.thisObject;
                }
            });
        } catch (Throwable ignored) {}

        Class<?> view = null;
        try {
            view = cl.loadClass("X.0jeh");
        } catch (Throwable t) {
            TikToKKK.log("comment confirm: view class not found (" + t + ")");
            return;
        }
        int n = 0;
        try {
            n += XposedBridge.hookAllMethods(view, "LJ", handler(true)).size();
        } catch (Throwable ignored) {}
        try {
            n += XposedBridge.hookAllMethods(view, "LIZLLL", handler(false)).size();
        } catch (Throwable ignored) {}
        TikToKKK.log("comment confirm installed (0jeh, " + n + " methods)");
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
                    Object comment = XposedHelpers.getObjectField(param.thisObject, "LLILLL");
                    if (comment == null) return;
                    int state = likeBtn ? Reflect.intVal(comment, "getUserDigged")
                            : Reflect.intVal(comment, "getUserBuried");
                    if (state != 0) return;   // already digged/buried -> this tap UNdoes it, no confirm
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
