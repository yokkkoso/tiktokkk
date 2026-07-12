package me.yokkkoso.tiktokkk.ui;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import me.yokkkoso.tiktokkk.Loc;

public final class Widgets {

    public static LinearLayout column(Activity a) {
        LinearLayout col = new LinearLayout(a);
        col.setOrientation(LinearLayout.VERTICAL);
        int pad = Theme.dp(a, 18);
        col.setPadding(pad, Theme.dp(a, 18), pad, Theme.dp(a, 16));
        return col;
    }

    public static ScrollView scroll(Activity a, View child) {
        ScrollView sv = new ScrollView(a);
        sv.setVerticalScrollBarEnabled(false);
        sv.addView(child);
        return sv;
    }

    public static LinearLayout groupCard(Activity a) {
        LinearLayout g = new LinearLayout(a);
        g.setOrientation(LinearLayout.VERTICAL);
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Theme.GROUP);
        bg.setCornerRadius(Theme.dp(a, 16));
        g.setBackground(bg);
        return g;
    }

    public static View divider(Activity a) {
        View v = new View(a);
        v.setBackgroundColor(Theme.DIVIDER);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, Math.max(1, Theme.dp(a, 1) / 2));
        int m = Theme.dp(a, 14);
        lp.setMargins(m, 0, 0, 0);
        v.setLayoutParams(lp);
        return v;
    }

    public static View categoryRow(Activity a, String emoji, String title, String subtitle, Runnable onClick) {
        LinearLayout row = new LinearLayout(a);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        int pad = Theme.dp(a, 14);
        row.setPadding(pad, Theme.dp(a, 13), pad, Theme.dp(a, 13));
        row.setOnClickListener(v -> onClick.run());

        TextView ic = new TextView(a);
        ic.setText(emoji);
        ic.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
        ic.setPadding(0, 0, Theme.dp(a, 12), 0);
        row.addView(ic);

        LinearLayout texts = new LinearLayout(a);
        texts.setOrientation(LinearLayout.VERTICAL);
        TextView t = new TextView(a);
        t.setText(title);
        t.setTextColor(Theme.TEXT);
        t.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        texts.addView(t);
        if (subtitle != null && !subtitle.isEmpty()) {
            TextView s = new TextView(a);
            s.setText(subtitle);
            s.setTextColor(Theme.MUTED);
            s.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
            texts.addView(s);
        }
        row.addView(texts, new LinearLayout.LayoutParams(0,
                ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        TextView chev = new TextView(a);
        chev.setText("›");
        chev.setTextColor(Theme.MUTED);
        chev.setTextSize(TypedValue.COMPLEX_UNIT_SP, 22);
        row.addView(chev);
        return row;
    }

    public static View accentRow(Activity a) {
        LinearLayout row = new LinearLayout(a);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        int pad = Theme.dp(a, 14);
        row.setPadding(pad, Theme.dp(a, 13), pad, Theme.dp(a, 13));
        row.setOnClickListener(v -> Menu.pickAccent(a));

        TextView t = new TextView(a);
        t.setText(Loc.t("Accent color"));
        t.setTextColor(Theme.TEXT);
        t.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        row.addView(t, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        View dot = new View(a);
        GradientDrawable d = new GradientDrawable();
        d.setShape(GradientDrawable.OVAL);
        d.setColor(Theme.ACCENT);
        dot.setBackground(d);
        dot.setLayoutParams(new LinearLayout.LayoutParams(Theme.dp(a, 22), Theme.dp(a, 22)));
        row.addView(dot);
        return row;
    }

    public static View actionRow(Activity a, String title, Runnable onClick) {
        LinearLayout row = new LinearLayout(a);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        int pad = Theme.dp(a, 14);
        row.setPadding(pad, Theme.dp(a, 14), pad, Theme.dp(a, 14));
        row.setOnClickListener(v -> onClick.run());
        TextView t = new TextView(a);
        t.setText(Loc.t(title));
        t.setTextColor(Theme.TEXT);
        t.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        row.addView(t, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        TextView chev = new TextView(a);
        chev.setText("›");
        chev.setTextColor(Theme.MUTED);
        chev.setTextSize(TypedValue.COMPLEX_UNIT_SP, 22);
        row.addView(chev);
        return row;
    }

    public static View subHeader(Activity a, String text) {
        TextView t = new TextView(a);
        t.setText(text.toUpperCase(java.util.Locale.ROOT));
        t.setTextColor(Theme.MUTED);
        t.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
        t.setTypeface(t.getTypeface(), Typeface.BOLD);
        t.setLetterSpacing(0.06f);
        t.setPadding(Theme.dp(a, 4), Theme.dp(a, 16), 0, Theme.dp(a, 8));
        return t;
    }

    public static View backHeader(Activity a, String title) {
        LinearLayout bar = new LinearLayout(a);
        bar.setOrientation(LinearLayout.HORIZONTAL);
        bar.setGravity(Gravity.CENTER_VERTICAL);
        bar.setPadding(0, 0, 0, Theme.dp(a, 14));

        TextView back = new TextView(a);
        back.setText("‹");
        back.setTextColor(Theme.ACCENT);
        back.setTextSize(TypedValue.COMPLEX_UNIT_SP, 28);
        back.setPadding(0, 0, Theme.dp(a, 12), 0);
        back.setOnClickListener(v -> Menu.navMain(a));
        bar.addView(back);

        TextView t = new TextView(a);
        t.setText(title);
        t.setTextColor(Theme.TEXT);
        t.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
        t.setTypeface(t.getTypeface(), Typeface.BOLD);
        bar.addView(t);
        return bar;
    }

    public static View gap(Activity a) {
        View g = new View(a);
        g.setLayoutParams(new LinearLayout.LayoutParams(Theme.dp(a, 10), 1));
        return g;
    }

    public static Button button(Activity a, String text, boolean filled, View.OnClickListener onClick) {
        Button b = new Button(a);
        b.setText(Loc.t(text));
        b.setAllCaps(false);
        b.setTextColor(filled ? Color.WHITE : Theme.ACCENT);
        GradientDrawable bg = new GradientDrawable();
        bg.setCornerRadius(Theme.dp(a, 12));
        if (filled) bg.setColor(Theme.ACCENT);
        else { bg.setColor(0x00000000); bg.setStroke(Theme.dp(a, 1), Theme.ACCENT); }
        b.setBackground(bg);
        b.setMaxLines(1);
        b.setMinHeight(0);
        b.setMinimumHeight(0);
        b.setOnClickListener(onClick);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, Theme.dp(a, 46), 1f);
        b.setLayoutParams(lp);
        return b;
    }

    public static View restartNote(Activity a) {
        TextView t = new TextView(a);
        t.setText("↻  " + Loc.t("Applies after restart"));
        t.setTextColor(Theme.ACCENT);
        t.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        t.setPadding(Theme.dp(a, 6), Theme.dp(a, 10), 0, 0);
        return t;
    }

    private Widgets() {}
}
