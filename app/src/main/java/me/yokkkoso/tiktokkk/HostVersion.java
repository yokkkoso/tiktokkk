package me.yokkkoso.tiktokkk;

import android.content.Context;
import android.content.pm.PackageInfo;

public final class HostVersion {
    public static final int V46_0_3 = 2024600030;
    public static final int V46_6_3 = 2024606030;
    public static final int V46_9_3 = 2024609030;

    private static volatile int code = Integer.MAX_VALUE;
    private static volatile String name = "";

    public static void init(Context ctx) {
        if (ctx == null) return;
        try {
            PackageInfo pi = ctx.getPackageManager().getPackageInfo(ctx.getPackageName(), 0);
            code = pi.versionCode;
            name = pi.versionName == null ? "" : pi.versionName;
        } catch (Throwable ignored) {}
    }

    public static int code() {
        if (code == Integer.MAX_VALUE) detect();
        return code;
    }

    public static String name() {
        return name;
    }

    public static boolean atLeast(int versionCode) {
        return code() >= versionCode;
    }

    private static void detect() {
        try {
            Object app = Class.forName("android.app.ActivityThread")
                    .getMethod("currentApplication").invoke(null);
            if (app instanceof Context) init((Context) app);
        } catch (Throwable ignored) {}
    }

    private HostVersion() {}
}
