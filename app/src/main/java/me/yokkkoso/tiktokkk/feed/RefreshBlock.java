package me.yokkkoso.tiktokkk.feed;

import me.yokkkoso.tiktokkk.Prefs;
import me.yokkkoso.tiktokkk.TikToKKK;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;

// Suppress the two FYP auto-refresh gestures. Both funnel through
// FeedRecommendFragment.Pc(X.0z80) where X.0z80 is the refresh-trigger enum:
//   PULL_DOWN_REFRESH = over-scroll / pull-to-refresh at the top
//   CLICK_BOTTOM      = tapping the already-selected Home tab
//   CLICK_TOP         = the top "For You" text tab (left intact)
// Returning false skips the refresh; initial load doesn't go through Pc, so it's unaffected.
public final class RefreshBlock {

    private static final String FRAG = "com.ss.android.ugc.aweme.feed.ui.FeedRecommendFragment";
    private static final String ENUM = "X.0z80";

    public static void install(ClassLoader cl) {
        try {
            XposedHelpers.findAndHookMethod(FRAG, cl, "Pc", ENUM, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) {
                    Object arg = param.args[0];
                    if (!(arg instanceof Enum)) return;
                    String n = ((Enum<?>) arg).name();
                    boolean block =
                            ("PULL_DOWN_REFRESH".equals(n) && Prefs.is(Prefs.DISABLE_SCROLL_REFRESH))
                         || ("CLICK_BOTTOM".equals(n) && Prefs.is(Prefs.DISABLE_HOME_REFRESH));
                    if (block) param.setResult(false);
                }
            });
            TikToKKK.log("refresh block installed");
        } catch (Throwable t) {
            TikToKKK.log("refresh block install failed: " + t);
        }
    }

    private RefreshBlock() {}
}
