package me.yokkkoso.tiktokkk.feed;

import me.yokkkoso.tiktokkk.FeatureStatus;
import me.yokkkoso.tiktokkk.Prefs;
import me.yokkkoso.tiktokkk.TikToKKK;
import me.yokkkoso.tiktokkk.dex.DexTargets;

import org.luckypray.dexkit.DexKitBridge;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;

public final class SeekBar {
    public static final String FEATURE = "Always show progress bar";

    public static void install(ClassLoader cl, DexKitBridge bridge) {
        Class<?> view = DexTargets.seekBar(bridge, cl);
        if (view == null) {
            FeatureStatus.failed(FEATURE, "seek bar class not found");
            return;
        }
        try {
            XposedHelpers.findAndHookMethod(view, "setSeekBarShowType", int.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam p) {
                    if (!Prefs.is(Prefs.FORCE_PROGRESS_BAR)) return;
                    int t = (int) p.args[0];
                    if (t == 4 || t == 3) p.args[0] = 0;
                }
            });
            FeatureStatus.ok(FEATURE, view.getName() + ".setSeekBarShowType");
            TikToKKK.log("seek bar hook installed on " + view.getName());
        } catch (Throwable t) {
            FeatureStatus.failed(FEATURE, "setSeekBarShowType missing on " + view.getName());
            TikToKKK.log("seek bar hook install failed: " + t);
        }
    }

    private SeekBar() {}
}
