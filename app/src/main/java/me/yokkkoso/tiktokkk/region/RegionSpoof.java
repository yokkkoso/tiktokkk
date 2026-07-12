package me.yokkkoso.tiktokkk.region;

import me.yokkkoso.tiktokkk.Countries;
import me.yokkkoso.tiktokkk.Prefs;
import me.yokkkoso.tiktokkk.TikToKKK;

import android.telephony.TelephonyManager;

import java.util.Locale;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;

public final class RegionSpoof {

    private static final int SIM_READY = 5;      // TelephonyManager.SIM_STATE_READY
    private static final int NETWORK_LTE = 13;   // TelephonyManager.NETWORK_TYPE_LTE

    public static void install(ClassLoader cl) {
        if (region().isEmpty()) return;
        installSim();
    }

    private static void installSim() {
        final XC_MethodHook iso = valueHook(0);
        final XC_MethodHook numeric = valueHook(1);
        final XC_MethodHook name = valueHook(2);
        final XC_MethodHook state = constHook(SIM_READY);
        final XC_MethodHook netType = constHook(NETWORK_LTE);
        final XC_MethodHook icc = constHook(Boolean.TRUE);
        try {
            Class<?> tm = TelephonyManager.class;
            XposedBridge.hookAllMethods(tm, "getSimCountryIso", iso);
            XposedBridge.hookAllMethods(tm, "getNetworkCountryIso", iso);
            XposedBridge.hookAllMethods(tm, "getSimOperator", numeric);
            XposedBridge.hookAllMethods(tm, "getNetworkOperator", numeric);
            XposedBridge.hookAllMethods(tm, "getSimOperatorName", name);
            XposedBridge.hookAllMethods(tm, "getNetworkOperatorName", name);
            XposedBridge.hookAllMethods(tm, "getSimState", state);
            XposedBridge.hookAllMethods(tm, "getNetworkType", netType);
            XposedBridge.hookAllMethods(tm, "getDataNetworkType", netType);
            XposedBridge.hookAllMethods(tm, "hasIccCard", icc);
            TikToKKK.log("SIM region hooks installed (carrier + SIM-present set)");
        } catch (Throwable t) {
            TikToKKK.log("SIM region hook failed: " + t);
        }
    }

    // type: 0=iso(lower), 1=mcc+mnc, 2=operator name. Short-circuits the real call.
    private static XC_MethodHook valueHook(final int type) {
        return new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                String code = region();
                if (code.isEmpty()) return;
                String v;
                switch (type) {
                    case 0:
                        v = code.toLowerCase(Locale.ROOT);
                        break;
                    case 1:
                        v = Countries.mcc(code) + Countries.mnc(code);
                        break;
                    default:
                        v = Countries.operator(code);
                        break;
                }
                if (v != null && !v.isEmpty()) param.setResult(v);
            }
        };
    }

    private static XC_MethodHook constHook(final Object val) {
        return new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                if (region().isEmpty()) return;
                param.setResult(val);
            }
        };
    }

    private static String region() {
        String r = Prefs.regionCode();
        return Countries.isBlocked(r) ? "" : r;
    }

    private RegionSpoof() {}
}
