package me.yokkkoso.tiktokkk.feed;

import me.yokkkoso.tiktokkk.Loc;
import me.yokkkoso.tiktokkk.Prefs;
import me.yokkkoso.tiktokkk.TikToKKK;

import android.app.Activity;
import android.app.AlertDialog;

import java.lang.reflect.Method;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

// Favoriting a video routes through AwemeCollectionAgent.collect(String,Map,Function2,Function2).
// Adding and removing are separate methods (collect vs unCollect), so hooking collect alone
// confirms only the ADD, never the undo.
public final class FavoriteConfirm {

    private static final String AGENT =
            "com.ss.android.ugc.aweme.favorites.business.aweme.AwemeCollectionAgent";

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

        Class<?> agent;
        try {
            agent = cl.loadClass(AGENT);
        } catch (Throwable t) {
            TikToKKK.log("favorite confirm: agent class not found (" + t + ")");
            return;
        }
        int n = 0;
        try {
            n = XposedBridge.hookAllMethods(agent, "collect", handler()).size();
        } catch (Throwable ignored) {}
        TikToKKK.log("favorite confirm installed (" + n + " methods)");
    }

    private static XC_MethodHook handler() {
        return new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                if (Boolean.TRUE.equals(BYPASS.get())) return;
                try {
                    if (!Prefs.is(Prefs.CONFIRM_FAVORITE)) return;
                    final Activity a = top;
                    if (a == null || a.isFinishing()) return;

                    param.setResult(null);
                    final Object agent = param.thisObject;
                    final Object[] args = param.args.clone();
                    final Method m = (Method) param.method;
                    final String msg = Loc.t("Add to favorites?");
                    a.runOnUiThread(() -> {
                        try {
                            new AlertDialog.Builder(a, android.R.style.Theme_Material_Dialog_Alert)
                                    .setMessage(msg)
                                    .setNegativeButton(android.R.string.cancel, null)
                                    .setPositiveButton(android.R.string.ok,
                                            (d, w) -> replay(m, agent, args))
                                    .show();
                        } catch (Throwable ignored) {}
                    });
                } catch (Throwable ignored) {}
            }
        };
    }

    private static void replay(Method m, Object agent, Object[] args) {
        BYPASS.set(Boolean.TRUE);
        try {
            m.setAccessible(true);
            m.invoke(agent, args);
        } catch (Throwable ignored) {
        } finally {
            BYPASS.set(Boolean.FALSE);
        }
    }

    private FavoriteConfirm() {}
}
