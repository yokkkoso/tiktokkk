package me.yokkkoso.tiktokkk.feed;

import me.yokkkoso.tiktokkk.FeatureStatus;
import me.yokkkoso.tiktokkk.Prefs;
import me.yokkkoso.tiktokkk.TikToKKK;
import me.yokkkoso.tiktokkk.dex.DexTargets;

import org.luckypray.dexkit.DexKitBridge;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;

public final class RefreshBlock {
    public static final String FEATURE = "Disable feed auto-refresh";

    private static final String FRAG = "com.ss.android.ugc.aweme.feed.ui.FeedRecommendFragment";
    private static final String METHOD_FALLBACK = "Pc";

    public static void install(ClassLoader cl, DexKitBridge bridge) {
        Class<?> frag;
        try {
            frag = cl.loadClass(FRAG);
        } catch (Throwable t) {
            FeatureStatus.failed(FEATURE, "FeedRecommendFragment not found");
            return;
        }
        Class<?> trigger = DexTargets.refreshEnum(bridge, cl);
        if (trigger == null) {
            FeatureStatus.failed(FEATURE, "refresh-trigger enum not found");
            return;
        }

        Method target = bySignature(frag, trigger);
        if (target == null) target = byName(frag, trigger);
        if (target == null) {
            FeatureStatus.failed(FEATURE, "no refresh method taking " + trigger.getSimpleName());
            return;
        }

        XposedBridge.hookMethod(target, new XC_MethodHook() {
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
        FeatureStatus.ok(FEATURE, frag.getSimpleName() + "." + target.getName());
        TikToKKK.log("refresh block installed on " + target.getName() + "(" + trigger.getName() + ")");
    }

    private static Method bySignature(Class<?> frag, Class<?> trigger) {
        List<Method> hits = new ArrayList<>();
        for (Method m : frag.getDeclaredMethods()) {
            Class<?>[] p = m.getParameterTypes();
            if (p.length == 1 && p[0] == trigger) hits.add(m);
        }
        if (hits.size() == 1) return hits.get(0);
        TikToKKK.log("refresh block: " + hits.size() + " candidate methods, trying name fallback");
        return null;
    }

    private static Method byName(Class<?> frag, Class<?> trigger) {
        try {
            return frag.getDeclaredMethod(METHOD_FALLBACK, trigger);
        } catch (Throwable t) {
            return null;
        }
    }

    private RefreshBlock() {}
}
