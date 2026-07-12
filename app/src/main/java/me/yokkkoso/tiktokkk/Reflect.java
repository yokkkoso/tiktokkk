package me.yokkkoso.tiktokkk;

import java.util.List;

import de.robv.android.xposed.XposedHelpers;

public final class Reflect {

    public static Object call(Object o, String method) {
        try {
            return XposedHelpers.callMethod(o, method);
        } catch (Throwable t) {
            return null;
        }
    }

    public static String str(Object o, String method) {
        Object r = call(o, method);
        return r == null ? "" : r.toString();
    }

    public static int intVal(Object o, String method) {
        Object r = call(o, method);
        return r instanceof Number ? ((Number) r).intValue() : 0;
    }

    public static boolean boolVal(Object o, String method) {
        Object r = call(o, method);
        return r instanceof Boolean && (Boolean) r;
    }

    public static Object field(Object o, String name) {
        try {
            return XposedHelpers.getObjectField(o, name);
        } catch (Throwable t) {
            return null;
        }
    }

    public static boolean boolField(Object o, String name) {
        try {
            return XposedHelpers.getBooleanField(o, name);
        } catch (Throwable t) {
            return false;
        }
    }

    public static String firstUrl(Object urlModel) {
        Object list = call(urlModel, "getUrlList");
        if (list instanceof List && !((List<?>) list).isEmpty()) {
            return String.valueOf(((List<?>) list).get(0));
        }
        return null;
    }

    public static boolean hasUrls(Object urlModel) {
        Object list = call(urlModel, "getUrlList");
        return list instanceof List && !((List<?>) list).isEmpty();
    }

    private Reflect() {}
}
