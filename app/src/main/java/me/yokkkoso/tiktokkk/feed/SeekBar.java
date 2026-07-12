package me.yokkkoso.tiktokkk.feed;

import me.yokkkoso.tiktokkk.Prefs;
import me.yokkkoso.tiktokkk.TikToKKK;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;

// Short videos hide the seek bar via a duration gate: X.0zo1.LJIIL(boolean) returns a
// "show type" (0=visible draggable, 4=alpha 0, 3=GONE) fed to 0zo3.setSeekBarShowType.
// Remap the hidden types to 0 so the bar shows on every video.
public final class SeekBar {

    public static void install(ClassLoader cl) {
        try {
            Class<?> view = cl.loadClass("X.0zo3");
            XposedHelpers.findAndHookMethod(view, "setSeekBarShowType", int.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam p) {
                    if (!Prefs.is(Prefs.FORCE_PROGRESS_BAR)) return;
                    int t = (int) p.args[0];
                    if (t == 4 || t == 3) p.args[0] = 0;
                }
            });
            TikToKKK.log("seek bar hook installed");
        } catch (Throwable t) {
            TikToKKK.log("seek bar hook install failed: " + t);
        }
    }

    private SeekBar() {}
}
