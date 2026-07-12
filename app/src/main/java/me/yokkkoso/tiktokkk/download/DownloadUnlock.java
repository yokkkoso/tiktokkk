package me.yokkkoso.tiktokkk.download;

import me.yokkkoso.tiktokkk.Prefs;
import me.yokkkoso.tiktokkk.TikToKKK;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;

// The Save gate combines three sources with NO getters on VideoControl, so all three are
// neutralized: Aweme.isPreventDownload, author User.isPreventDownload, and VideoControl fields
// preventDownloadType / allowDownload.
public final class DownloadUnlock {

    public static void install(ClassLoader cl) {
        try {
            Class<?> aweme = XposedHelpers.findClass("com.ss.android.ugc.aweme.feed.model.Aweme", cl);
            Class<?> user = XposedHelpers.findClass("com.ss.android.ugc.aweme.profile.model.User", cl);

            XC_MethodHook allow = new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam p) {
                    if (Prefs.is(Prefs.ALLOW_ALL_DOWNLOADS)) p.setResult(false);
                }
            };
            XposedHelpers.findAndHookMethod(aweme, "isPreventDownload", allow);
            XposedHelpers.findAndHookMethod(user, "isPreventDownload", allow);

            XposedHelpers.findAndHookMethod(aweme, "getVideoControl", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam p) {
                    Object vc = p.getResult();
                    if (vc == null || !Prefs.is(Prefs.ALLOW_ALL_DOWNLOADS)) return;
                    try { XposedHelpers.setIntField(vc, "preventDownloadType", 0); } catch (Throwable ignored) {}
                    try { XposedHelpers.setObjectField(vc, "allowDownload", Boolean.TRUE); } catch (Throwable ignored) {}
                }
            });
            TikToKKK.log("download unlock installed");
        } catch (Throwable t) {
            TikToKKK.log("download unlock install failed: " + t);
        }
    }

    private DownloadUnlock() {}
}
