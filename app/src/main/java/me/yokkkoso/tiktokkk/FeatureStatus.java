package me.yokkkoso.tiktokkk;

import java.util.LinkedHashMap;
import java.util.Map;

public final class FeatureStatus {
    public enum State { OK, FAILED, SKIPPED }

    private static final Map<String, State> STATE = new LinkedHashMap<>();
    private static final Map<String, String> DETAIL = new LinkedHashMap<>();

    public static synchronized void ok(String feature, String detail) {
        STATE.put(feature, State.OK);
        DETAIL.put(feature, detail);
    }

    public static synchronized void failed(String feature, String detail) {
        STATE.put(feature, State.FAILED);
        DETAIL.put(feature, detail);
        TikToKKK.log("feature '" + feature + "' FAILED: " + detail);
    }

    public static synchronized void okIfUnset(String feature) {
        if (!STATE.containsKey(feature)) STATE.put(feature, State.OK);
    }

    public static synchronized void skipped(String feature, String detail) {
        STATE.put(feature, State.SKIPPED);
        DETAIL.put(feature, detail);
    }

    public static synchronized Map<String, State> snapshot() {
        return new LinkedHashMap<>(STATE);
    }

    public static synchronized String detail(String feature) {
        String d = DETAIL.get(feature);
        return d == null ? "" : d;
    }

    public static synchronized int count(State s) {
        int n = 0;
        for (State v : STATE.values()) if (v == s) n++;
        return n;
    }

    private FeatureStatus() {}
}
