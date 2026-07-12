package me.yokkkoso.tiktokkk.ui;

import android.content.Context;
import android.content.res.ColorStateList;
import android.util.TypedValue;
import android.widget.Switch;

import me.yokkkoso.tiktokkk.Prefs;

public final class Theme {

    public static int ACCENT = Prefs.DEFAULT_ACCENT;
    public static final int[] ACCENTS = {
            0xFFFC0FC0, 0xFF25F4EE, 0xFF7B2FF7, 0xFF25D366,
            0xFFFF7A00, 0xFFFF3B30, 0xFFFFCC00, 0xFFFFFFFF};
    public static final String[] ACCENT_NAMES = {
            "Pink", "Cyan", "Purple", "Green", "Orange", "Red", "Yellow", "White"};
    public static final int CARD = 0xFF161619;
    public static final int GROUP = 0xFF232329;
    public static final int DIVIDER = 0xFF2E2E34;
    public static final int TEXT = 0xFFFFFFFF;
    public static final int MUTED = 0xFF9A9AA2;
    public static final int TRACK_OFF = 0xFF3A3A40;
    public static final int FAB_BG = 0x40000000;   // black, 25% opacity

    public static void tint(Switch sw) {
        int on = sw.isChecked() ? ACCENT : TRACK_OFF;
        sw.setThumbTintList(ColorStateList.valueOf(sw.isChecked() ? ACCENT : 0xFFCFCFD4));
        sw.setTrackTintList(ColorStateList.valueOf(on));
    }

    public static int dp(Context a, int v) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, v,
                a.getResources().getDisplayMetrics());
    }

    private Theme() {}
}
