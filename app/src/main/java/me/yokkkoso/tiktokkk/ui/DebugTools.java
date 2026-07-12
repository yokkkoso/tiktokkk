package me.yokkkoso.tiktokkk.ui;

import android.app.Activity;
import android.content.Intent;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.List;

import me.yokkkoso.tiktokkk.Ids;
import me.yokkkoso.tiktokkk.Loc;
import me.yokkkoso.tiktokkk.Logs;
import me.yokkkoso.tiktokkk.TikToKKK;

public final class DebugTools {

    public static void dumpViews(Activity a) {
        Logs.clear();
        TikToKKK.log("=== SCREEN DUMP: " + a.getClass().getSimpleName() + " ===");
        try {
            View decor = a.getWindow().getDecorView();
            decor.postDelayed(() -> {
                dumpWalk(decor, 0);
                TikToKKK.log("=== END DUMP ===");
            }, 400);
            android.widget.Toast.makeText(a,
                    Loc.t("Dumped. Wait 1s, reopen menu -> View logs -> Share"),
                    android.widget.Toast.LENGTH_LONG).show();
        } catch (Throwable t) {
            TikToKKK.log("dump failed: " + t);
        }
    }

    private static void dumpWalk(View v, int depth) {
        if (v == null || depth > 40) return;
        String id = Ids.nameOf(v);
        String txt = v instanceof TextView ? String.valueOf(((TextView) v).getText()) : "";
        if (txt.length() > 24) txt = txt.substring(0, 24);
        boolean shown = v.getVisibility() == View.VISIBLE;
        StringBuilder pad = new StringBuilder();
        for (int i = 0; i < depth; i++) pad.append(' ');
        TikToKKK.log(pad + (shown ? "" : "[H]") + v.getClass().getSimpleName()
                + " id=" + id + " desc=" + v.getContentDescription()
                + (txt.isEmpty() ? "" : " text=[" + txt + "]"));
        if (v instanceof ViewGroup) {
            ViewGroup g = (ViewGroup) v;
            for (int i = 0; i < g.getChildCount(); i++) dumpWalk(g.getChildAt(i), depth + 1);
        }
    }

    public static void showLogs(Activity a) {
        List<String> lines = Logs.snapshot();
        StringBuilder sb = new StringBuilder();
        for (String l : lines) sb.append(l).append('\n');
        final String text = sb.length() == 0
                ? Loc.t("(empty — enable 'Debug: log click targets', then use the app)") : sb.toString();

        TextView tv = new TextView(a);
        tv.setText(text);
        tv.setTextColor(Theme.TEXT);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
        tv.setTextIsSelectable(true);
        int p = Theme.dp(a, 14);
        tv.setPadding(p, p, p, p);
        ScrollView sv = new ScrollView(a);
        sv.addView(tv);

        Menu.dlg(a)
                .setTitle(Loc.t("Logs") + " (" + lines.size() + ")")
                .setView(sv)
                .setPositiveButton(Loc.t("Share"), (d, w) -> {
                    Intent i = new Intent(Intent.ACTION_SEND);
                    i.setType("text/plain");
                    i.putExtra(Intent.EXTRA_TEXT, text);
                    a.startActivity(Intent.createChooser(i, "Share logs"));
                })
                .setNeutralButton(Loc.t("Clear"), (d, w) -> Logs.clear())
                .setNegativeButton(Loc.t("Close"), null)
                .show();
    }

    private DebugTools() {}
}
