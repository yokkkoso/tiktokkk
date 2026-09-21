package me.yokkkoso.tiktokkk.dex;

import me.yokkkoso.tiktokkk.TikToKKK;

import org.luckypray.dexkit.DexKitBridge;

public final class DexKitHost {
    private static boolean libLoaded;
    private static boolean libFailed;
    private static DexKitBridge bridge;

    private static synchronized boolean loadLib() {
        if (libLoaded) return true;
        if (libFailed) return false;
        try {
            System.loadLibrary("dexkit");
            libLoaded = true;
        } catch (Throwable t) {
            libFailed = true;
            TikToKKK.log("dexkit: native lib unavailable (" + t + ")");
        }
        return libLoaded;
    }

    public static synchronized DexKitBridge open(ClassLoader cl) {
        if (bridge != null) return bridge;
        if (!loadLib()) return null;
        try {
            long t0 = System.currentTimeMillis();
            bridge = DexKitBridge.create(cl, true);
            TikToKKK.log("dexkit: bridge open in " + (System.currentTimeMillis() - t0) + "ms");
        } catch (Throwable t) {
            TikToKKK.log("dexkit: bridge create failed (" + t + ")");
        }
        return bridge;
    }

    public static synchronized void close() {
        if (bridge == null) return;
        try {
            bridge.close();
        } catch (Throwable ignored) {
        } finally {
            bridge = null;
        }
    }

    private DexKitHost() {}
}
