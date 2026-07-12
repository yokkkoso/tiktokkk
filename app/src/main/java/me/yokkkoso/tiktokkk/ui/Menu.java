package me.yokkkoso.tiktokkk.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;

import me.yokkkoso.tiktokkk.Countries;
import me.yokkkoso.tiktokkk.Loc;
import me.yokkkoso.tiktokkk.Prefs;

public final class Menu {

    public static final boolean DEV = false;
    static final String VERSION = "1.1.1";

    private static Dialog CURRENT;
    private static FrameLayout HOST;

    public static void show(Activity a) {
        Theme.ACCENT = Prefs.accentColor();
        Dialog dlg = new Dialog(a);
        dlg.requestWindowFeature(Window.FEATURE_NO_TITLE);
        Window w = dlg.getWindow();
        if (w != null) {
            w.setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(Color.TRANSPARENT));
        }

        FrameLayout host = new FrameLayout(a);
        host.setTag("kkk_menu");
        GradientDrawable card = new GradientDrawable();
        card.setColor(Theme.CARD);
        card.setCornerRadius(Theme.dp(a, 22));
        host.setBackground(card);

        FrameLayout wrap = new FrameLayout(a);
        int m = Theme.dp(a, 14);
        FrameLayout.LayoutParams wlp = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        wlp.setMargins(m, m, m, m);
        wlp.gravity = Gravity.CENTER;
        wrap.addView(host, wlp);
        dlg.setContentView(wrap);

        CURRENT = dlg;
        HOST = host;
        navMain(a);
        dlg.show();
    }

    public static void navMain(Activity a) {
        HOST.removeAllViews();
        LinearLayout col = Widgets.column(a);

        TextView title = new TextView(a);
        title.setText("tiktokkk  v" + VERSION);
        title.setTextColor(Theme.ACCENT);
        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24);
        title.setTypeface(title.getTypeface(), Typeface.BOLD);
        col.addView(title);

        TextView sub = new TextView(a);
        sub.setText("yokkkoso.me");
        sub.setTextColor(Theme.MUTED);
        sub.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        sub.setPadding(0, Theme.dp(a, 2), 0, Theme.dp(a, 2));
        sub.setOnClickListener(v -> openUrl(a, "https://yokkkoso.me"));
        col.addView(sub);

        TextView tg = new TextView(a);
        tg.setText("t.me/tiktokkkmod");
        tg.setTextColor(Theme.MUTED);
        tg.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        tg.setPadding(0, 0, 0, Theme.dp(a, 14));
        tg.setOnClickListener(v -> openUrl(a, "https://t.me/tiktokkkmod"));
        col.addView(tg);

        TextView langRow = new TextView(a);
        langRow.setText((Loc.isRu() ? "Язык:  Русский" : "Language:  English") + "   ▾");
        langRow.setTextColor(Theme.ACCENT);
        langRow.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        langRow.setPadding(0, 0, 0, Theme.dp(a, 12));
        langRow.setOnClickListener(v -> pickLanguage(a));
        col.addView(langRow);

        LinearLayout group = Widgets.groupCard(a);
        boolean first = true;
        for (String[] c : PrefCatalog.CATEGORIES) {
            if (!DEV && "Advanced".equals(c[0])) continue;
            if (!first) group.addView(Widgets.divider(a));
            first = false;
            final String cat = c[0];
            group.addView(Widgets.categoryRow(a, c[1], Loc.t(cat), "", () -> navCategory(a, cat)));
        }
        col.addView(group);

        col.addView(footer(a));
        HOST.addView(Widgets.scroll(a, col));
    }

    private static void navCategory(Activity a, String cat) {
        HOST.removeAllViews();
        LinearLayout col = Widgets.column(a);
        col.addView(Widgets.backHeader(a, Loc.t(cat)));

        if (PrefCatalog.REGION_CAT.equals(cat)) {
            LinearLayout g = Widgets.groupCard(a);
            g.addView(regionRow(a));
            col.addView(g);
            col.addView(Widgets.restartNote(a));
        } else if (PrefCatalog.MISC_CAT.equals(cat)) {
            col.addView(miscGroup(a));
        } else {
            String[][] groups = PrefCatalog.GROUPS.get(cat);
            if (groups != null) {
                for (String[] sub : groups) {
                    if (sub[0] != null && !sub[0].isEmpty()) col.addView(Widgets.subHeader(a, Loc.t(sub[0])));
                    LinearLayout g = Widgets.groupCard(a);
                    for (int i = 1; i < sub.length; i++) {
                        if (i > 1) g.addView(Widgets.divider(a));
                        g.addView(row(a, sub[i]));
                    }
                    col.addView(g);
                }
            }
        }
        HOST.addView(Widgets.scroll(a, col));
    }

    private static void pickLanguage(Activity a) {
        String[] items = {"English", "Русский"};
        int cur = Loc.isRu() ? 1 : 0;
        dlg(a)
                .setTitle(Loc.t("Language"))
                .setSingleChoiceItems(items, cur, (d, which) -> {
                    Prefs.setString(Prefs.LOCALE, which == 1 ? "ru" : "en");
                    d.dismiss();
                    navMain(a);
                })
                .show();
    }

    private static LinearLayout miscGroup(Activity a) {
        LinearLayout g = Widgets.groupCard(a);
        g.addView(row(a, Prefs.SANITIZE_LINKS));
        g.addView(Widgets.divider(a));
        g.addView(Widgets.accentRow(a));
        g.addView(Widgets.divider(a));
        g.addView(Widgets.actionRow(a, "Export settings", () -> exportSettings(a)));
        g.addView(Widgets.divider(a));
        g.addView(Widgets.actionRow(a, "Import settings", () -> importSettings(a)));
        g.addView(Widgets.divider(a));
        g.addView(Widgets.actionRow(a, "Reset settings", () -> resetSettings(a)));
        return g;
    }

    public static void pickAccent(Activity a) {
        int cur = 0;
        for (int i = 0; i < Theme.ACCENTS.length; i++) if (Theme.ACCENTS[i] == Theme.ACCENT) cur = i;
        String[] items = new String[Theme.ACCENT_NAMES.length];
        for (int i = 0; i < items.length; i++) items[i] = Loc.t(Theme.ACCENT_NAMES[i]);
        dlg(a)
                .setTitle(Loc.t("Accent color"))
                .setSingleChoiceItems(items, cur, (d, which) -> {
                    Theme.ACCENT = Theme.ACCENTS[which];
                    Prefs.setInt(Prefs.ACCENT_COLOR, Theme.ACCENT);
                    OverlayFab.recolorFab(a);
                    d.dismiss();
                    navCategory(a, PrefCatalog.MISC_CAT);
                })
                .show();
    }

    private static void exportSettings(Activity a) {
        final String text = Prefs.exportAll();
        TextView tv = new TextView(a);
        tv.setText(text);
        tv.setTextColor(Theme.TEXT);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        tv.setTextIsSelectable(true);
        int p = Theme.dp(a, 14);
        tv.setPadding(p, p, p, p);
        ScrollView sv = new ScrollView(a);
        sv.addView(tv);
        dlg(a)
                .setTitle(Loc.t("Export settings"))
                .setView(sv)
                .setPositiveButton(Loc.t("Share"), (d, w) -> {
                    Intent i = new Intent(Intent.ACTION_SEND);
                    i.setType("text/plain");
                    i.putExtra(Intent.EXTRA_TEXT, text);
                    a.startActivity(Intent.createChooser(i, Loc.t("Export settings")));
                })
                .setNeutralButton(Loc.t("Copy"), (d, w) -> {
                    android.content.ClipboardManager cm = (android.content.ClipboardManager)
                            a.getSystemService(Context.CLIPBOARD_SERVICE);
                    if (cm != null) cm.setPrimaryClip(
                            android.content.ClipData.newPlainText("tiktokkk", text));
                    toast(a, Loc.t("Copied"));
                })
                .setNegativeButton(Loc.t("Close"), null)
                .show();
    }

    private static void importSettings(Activity a) {
        final EditText et = new EditText(a);
        et.setHint(Loc.t("Paste config here"));
        et.setTextColor(Theme.TEXT);
        et.setHintTextColor(Theme.MUTED);
        et.setMinLines(4);
        et.setGravity(Gravity.TOP);
        int p = Theme.dp(a, 12);
        et.setPadding(p, p, p, p);
        dlg(a)
                .setTitle(Loc.t("Import settings"))
                .setView(et)
                .setPositiveButton(android.R.string.ok, (d, w) -> {
                    boolean ok = Prefs.importAll(et.getText().toString());
                    Theme.ACCENT = Prefs.accentColor();
                    OverlayFab.recolorFab(a);
                    toast(a, Loc.t(ok ? "Imported — restart to apply" : "Nothing to import"));
                    navMain(a);
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private static void resetSettings(Activity a) {
        dlg(a)
                .setTitle(Loc.t("Reset settings"))
                .setMessage(Loc.t("Reset all settings to defaults?"))
                .setPositiveButton(android.R.string.ok, (d, w) -> {
                    Prefs.resetAll();
                    Theme.ACCENT = Prefs.accentColor();
                    OverlayFab.recolorFab(a);
                    toast(a, Loc.t("Settings reset"));
                    navMain(a);
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    public static AlertDialog.Builder dlg(Activity a) {
        return new AlertDialog.Builder(a, android.R.style.Theme_Material_Dialog_Alert);
    }

    private static void toast(Activity a, String m) {
        android.widget.Toast.makeText(a, m, android.widget.Toast.LENGTH_SHORT).show();
    }

    private static View footer(Activity a) {
        LinearLayout wrap = new LinearLayout(a);
        wrap.setOrientation(LinearLayout.VERTICAL);
        wrap.setPadding(0, Theme.dp(a, 16), 0, 0);

        if (DEV) {
            LinearLayout logs = new LinearLayout(a);
            logs.setOrientation(LinearLayout.HORIZONTAL);
            logs.addView(Widgets.button(a, "Dump screen", false, v -> {
                if (CURRENT != null) CURRENT.dismiss();
                DebugTools.dumpViews(a);
            }));
            logs.addView(Widgets.gap(a));
            logs.addView(Widgets.button(a, "View logs", false, v -> DebugTools.showLogs(a)));
            wrap.addView(logs);
        }

        LinearLayout act = new LinearLayout(a);
        act.setOrientation(LinearLayout.HORIZONTAL);
        act.setPadding(0, Theme.dp(a, 10), 0, 0);
        act.addView(Widgets.button(a, "Restart", true, v -> restart(a)));
        act.addView(Widgets.gap(a));
        act.addView(Widgets.button(a, "Close", false, v -> { if (CURRENT != null) CURRENT.dismiss(); }));
        wrap.addView(act);
        return wrap;
    }

    private static View row(Activity a, String key) {
        LinearLayout row = new LinearLayout(a);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        int pad = Theme.dp(a, 14);
        row.setPadding(pad, Theme.dp(a, 11), pad, Theme.dp(a, 11));

        LinearLayout texts = new LinearLayout(a);
        texts.setOrientation(LinearLayout.VERTICAL);
        TextView title = new TextView(a);
        title.setText(Loc.t(PrefCatalog.LABELS.get(key)));
        title.setTextColor(Theme.TEXT);
        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        texts.addView(title);
        String desc = PrefCatalog.DESCS.get(key);
        if (desc != null) {
            TextView d = new TextView(a);
            d.setText(Loc.t(desc));
            d.setTextColor(Theme.MUTED);
            d.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
            d.setPadding(0, Theme.dp(a, 2), 0, 0);
            texts.addView(d);
        }
        row.addView(texts, new LinearLayout.LayoutParams(0,
                ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        Switch sw = new Switch(a);
        sw.setChecked(Prefs.is(key));
        Theme.tint(sw);
        sw.setOnCheckedChangeListener((b, checked) -> {
            Prefs.set(key, checked);
            Theme.tint(sw);
        });
        row.addView(sw);
        return row;
    }

    private static View regionRow(Activity a) {
        TextView row = new TextView(a);
        row.setTextColor(Theme.TEXT);
        row.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        int pad = Theme.dp(a, 14);
        row.setPadding(pad, Theme.dp(a, 14), pad, Theme.dp(a, 14));
        row.setText(regionText());
        row.setOnClickListener(v -> pickRegion(a, row));
        return row;
    }

    private static String regionText() {
        String code = Prefs.getString(Prefs.REGION, "US");
        return Loc.t("Region:") + "  " + (code.isEmpty() ? Loc.t("Off (real region)") : Countries.label(code))
                + "     ▾";
    }

    private static void pickRegion(Activity a, TextView row) {
        String[] items = new String[Countries.LIST.length];
        int current = 0;
        String cur = Prefs.getString(Prefs.REGION, "US");
        for (int i = 0; i < items.length; i++) {
            String[] c = Countries.LIST[i];
            String base = c[0].isEmpty() ? Loc.t(c[1]) : Countries.flag(c[0]) + "   " + c[1] + "  (" + c[0] + ")";
            items[i] = Countries.isBlocked(c[0]) ? base + "   " + Loc.t("— banned") : base;
            if (c[0].equalsIgnoreCase(cur)) current = i;
        }
        android.widget.ArrayAdapter<String> adapter = new android.widget.ArrayAdapter<String>(
                a, android.R.layout.simple_list_item_single_choice, items) {
            @Override
            public boolean areAllItemsEnabled() {
                return false;
            }
            @Override
            public boolean isEnabled(int position) {
                return !Countries.isBlocked(Countries.LIST[position][0]);
            }
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View v = super.getView(position, convertView, parent);
                if (Countries.isBlocked(Countries.LIST[position][0]) && v instanceof TextView) {
                    v.setEnabled(false);
                    ((TextView) v).setTextColor(Theme.MUTED);
                }
                return v;
            }
        };
        dlg(a)
                .setTitle(Loc.t("Select region"))
                .setSingleChoiceItems(adapter, current, (d, which) -> {
                    if (Countries.isBlocked(Countries.LIST[which][0])) return;
                    Prefs.setString(Prefs.REGION, Countries.LIST[which][0]);
                    row.setText(regionText());
                    d.dismiss();
                })
                .show();
    }

    private static void openUrl(Activity a, String url) {
        try {
            a.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        } catch (Throwable ignored) {}
    }

    private static void restart(Context ctx) {
        try {
            Intent i = ctx.getPackageManager().getLaunchIntentForPackage(ctx.getPackageName());
            if (i != null) {
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                ctx.startActivity(i);
            }
        } catch (Throwable ignored) {}
        Runtime.getRuntime().exit(0);
    }

    private Menu() {}
}
