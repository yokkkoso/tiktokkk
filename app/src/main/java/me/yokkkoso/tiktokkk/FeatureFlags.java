package me.yokkkoso.tiktokkk;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

public final class FeatureFlags {
    public static final String FEATURE = "AB flag overrides";

    private static final String SETTINGS_MANAGER = "com.bytedance.ies.abmock.SettingsManager";

    private static volatile Map<String, String> overrides = Collections.emptyMap();
    private static final Map<String, String[]> forced = new java.util.concurrent.ConcurrentHashMap<>();
    private static final Set<String> seen =
            Collections.synchronizedSet(new LinkedHashSet<String>());

    public static void install(ClassLoader cl) {
        Class<?> manager;
        try {
            manager = XposedHelpers.findClass(SETTINGS_MANAGER, cl);
        } catch (Throwable t) {
            FeatureStatus.failed(FEATURE, "SettingsManager not found");
            return;
        }
        reload();

        int n = 0;
        n += hook(manager, boolean.class);
        n += hook(manager, int.class);
        if (n == 0) {
            FeatureStatus.failed(FEATURE, "no (String, primitive) getter on SettingsManager");
            return;
        }
        FeatureStatus.ok(FEATURE, n + " getter(s)");
        TikToKKK.log("ab flags: hooked " + n + " getters on " + manager.getName());
    }

    public static void reload() {
        Map<String, String> map = new HashMap<>();
        for (String line : Prefs.getString(Prefs.AB_OVERRIDES, "").split("\n")) {
            int eq = line.indexOf('=');
            if (eq <= 0) continue;
            String key = line.substring(0, eq).trim();
            String value = line.substring(eq + 1).trim();
            if (!key.isEmpty() && !value.isEmpty()) map.put(key, value);
        }
        overrides = map;
    }

    public static void force(String key, String value, String gatePref) {
        forced.put(key, new String[]{value, gatePref});
    }

    public static Set<String> queriedKeys() {
        synchronized (seen) {
            return new LinkedHashSet<>(seen);
        }
    }

    private static int hook(Class<?> manager, final Class<?> type) {
        Method target = null;
        for (Method m : manager.getDeclaredMethods()) {
            if (!Modifier.isStatic(m.getModifiers())) continue;
            if (m.getReturnType() != type) continue;
            Class<?>[] p = m.getParameterTypes();
            if (p.length == 2 && p[0] == String.class && p[1] == type) {
                if (target != null) return 0;
                target = m;
            }
        }
        if (target == null) return 0;
        XposedBridge.hookMethod(target, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) {
                Object key = param.args[0];
                if (!(key instanceof String)) return;
                String name = (String) key;
                if (Prefs.is(Prefs.AB_LOG) && seen.add(name)) {
                    TikToKKK.log("ab flag: " + name + " = " + param.getResult());
                }
                String override = overrides.get(name);
                if (override == null) {
                    String[] f = forced.get(name);
                    if (f == null || !Prefs.is(f[1])) return;
                    override = f[0];
                    if (seen.add("forced:" + name)) {
                        TikToKKK.log("ab flag forced: " + name + " " + param.getResult() + " -> " + override);
                    }
                }
                try {
                    param.setResult(type == boolean.class
                            ? (Object) Boolean.parseBoolean(override)
                            : (Object) Integer.parseInt(override));
                } catch (Throwable ignored) {}
            }
        });
        return 1;
    }

    private FeatureFlags() {}
}
