package me.yokkkoso.tiktokkk.dex;

import me.yokkkoso.tiktokkk.TikToKKK;
import me.yokkkoso.tiktokkk.feed.CommentConfirm;
import me.yokkkoso.tiktokkk.feed.RefreshBlock;
import me.yokkkoso.tiktokkk.feed.SearchAdFilter;
import me.yokkkoso.tiktokkk.feed.SeekBar;
import me.yokkkoso.tiktokkk.feed.SplashAdBlock;

import android.content.Context;
import android.content.pm.PackageInfo;

import org.luckypray.dexkit.DexKitBridge;

public final class DexBootstrap {
    private static boolean started;

    public static synchronized void run(Context ctx, ClassLoader cl) {
        if (started) return;
        started = true;

        DexCache.init(ctx, hostVersion(ctx));
        if (DexCache.isValid()) {
            installAll(cl, null);
            return;
        }
        Thread t = new Thread(() -> {
            try {
                DexKitBridge bridge = DexKitHost.open(cl);
                installAll(cl, bridge);
            } finally {
                DexKitHost.close();
            }
        }, "tiktokkk-dexkit");
        t.setPriority(Thread.MIN_PRIORITY);
        t.start();
    }

    private static void installAll(ClassLoader cl, DexKitBridge bridge) {
        safe("commentConfirm", () -> CommentConfirm.install(cl, bridge));
        safe("seekBar", () -> SeekBar.install(cl, bridge));
        safe("refreshBlock", () -> RefreshBlock.install(cl, bridge));
        safe("searchAdFilter", () -> SearchAdFilter.install(cl, bridge));
        safe("splashAdBlock", () -> SplashAdBlock.install(cl, bridge));
        safe("feedConverters", () -> me.yokkkoso.tiktokkk.feed.FeedFilter.installConverters(cl, bridge));
    }

    private static void safe(String name, Runnable r) {
        try {
            r.run();
        } catch (Throwable t) {
            TikToKKK.log("dex hook '" + name + "' failed: " + t);
        }
    }

    private static String hostVersion(Context c) {
        try {
            PackageInfo pi = c.getPackageManager().getPackageInfo(c.getPackageName(), 0);
            return String.valueOf(pi.getLongVersionCode());
        } catch (Throwable t) {
            return "unknown";
        }
    }

    private DexBootstrap() {}
}
