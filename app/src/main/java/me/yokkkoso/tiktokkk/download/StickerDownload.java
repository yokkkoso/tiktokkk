package me.yokkkoso.tiktokkk.download;

import me.yokkkoso.tiktokkk.Ids;
import me.yokkkoso.tiktokkk.Loc;
import me.yokkkoso.tiktokkk.Prefs;
import me.yokkkoso.tiktokkk.TikToKKK;

import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.OutputStream;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;

public final class StickerDownload {

    public static void install(ClassLoader cl) {
        XC_MethodHook cb = new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam p) {
                try {
                    if (Prefs.is(Prefs.STICKER_DOWNLOAD)) attach((View) p.args[0]);
                } catch (Throwable ignored) {}
            }
        };
        try {
            XposedHelpers.findAndHookMethod(ViewGroup.class, "addView",
                    View.class, int.class, ViewGroup.LayoutParams.class, cb);
        } catch (Throwable ignored) {}
        try {
            XposedHelpers.findAndHookMethod(ViewGroup.class, "addViewInLayout",
                    View.class, int.class, ViewGroup.LayoutParams.class, boolean.class, cb);
        } catch (Throwable ignored) {}
        TikToKKK.log("sticker download installed");
    }

    private static void attach(View v) {
        if (v == null) return;
        if (v instanceof ImageView && v.getClass().getSimpleName().contains("SmartImage")
                && (inComment(v) || inStickerViewer(v))) {
            v.setOnLongClickListener(view -> {
                save((ImageView) view);
                return true;
            });
        }
        if (v instanceof ViewGroup) {
            ViewGroup g = (ViewGroup) v;
            for (int i = 0; i < g.getChildCount(); i++) attach(g.getChildAt(i));
        }
    }

    private static boolean inComment(View v) {
        for (int i = 0; i < 16 && v != null; i++) {
            if (Ids.COMMENT_SHEET.contains(Ids.nameOf(v))) return true;
            ViewParent p = v.getParent();
            v = (p instanceof View) ? (View) p : null;
        }
        return false;
    }

    private static boolean inStickerViewer(View v) {
        View node = v;
        for (int i = 0; i < 8 && node != null; i++) {
            if (node instanceof ViewGroup && hasReport((ViewGroup) node, 3)) return true;
            ViewParent p = node.getParent();
            node = (p instanceof View) ? (View) p : null;
        }
        return false;
    }

    // 46.0.3: the enlarged-sticker viewer carries a report (fri) and close (fqe) button.
    private static boolean hasReport(View v, int depth) {
        if (v == null || depth < 0) return false;
        String id = Ids.nameOf(v);
        if (Ids.STICKER_REPORT.equals(id) || Ids.STICKER_CLOSE.equals(id)) return true;
        if (v instanceof ViewGroup) {
            ViewGroup g = (ViewGroup) v;
            for (int i = 0; i < g.getChildCount(); i++) {
                if (hasReport(g.getChildAt(i), depth - 1)) return true;
            }
        }
        return false;
    }

    private static void save(ImageView iv) {
        Context c = iv.getContext();
        try {
            Bitmap bmp = toBitmap(iv.getDrawable(), iv.getWidth(), iv.getHeight());
            if (bmp == null) {
                toast(c, Loc.t("No image"));
                return;
            }
            ContentValues cv = new ContentValues();
            cv.put(MediaStore.MediaColumns.DISPLAY_NAME, "tiktok_sticker_" + System.nanoTime() + ".png");
            cv.put(MediaStore.MediaColumns.MIME_TYPE, "image/png");
            if (Build.VERSION.SDK_INT >= 29) {
                cv.put(MediaStore.MediaColumns.RELATIVE_PATH,
                        Environment.DIRECTORY_PICTURES + "/tiktokkk");
            }
            Uri uri = c.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, cv);
            if (uri == null) {
                toast(c, Loc.t("Save failed"));
                return;
            }
            try (OutputStream os = c.getContentResolver().openOutputStream(uri)) {
                bmp.compress(Bitmap.CompressFormat.PNG, 100, os);
            } catch (Throwable t) {
                try { c.getContentResolver().delete(uri, null, null); } catch (Throwable ignored) {}
                toast(c, Loc.t("Save failed"));
                return;
            }
            toast(c, Loc.t("Saved to gallery"));
        } catch (Throwable t) {
            toast(c, Loc.t("Save failed"));
        }
    }

    // Fresco SmartImageViews use a custom drawable with intrinsic size -1 and render to a hardware
    // layer (so View.draw yields blank). Draw the drawable itself at the view's laid-out size.
    private static Bitmap toBitmap(Drawable d, int fallbackW, int fallbackH) {
        if (d == null) return null;
        if (d instanceof BitmapDrawable && ((BitmapDrawable) d).getBitmap() != null) {
            return ((BitmapDrawable) d).getBitmap();
        }
        int w = d.getIntrinsicWidth() > 0 ? d.getIntrinsicWidth() : fallbackW;
        int h = d.getIntrinsicHeight() > 0 ? d.getIntrinsicHeight() : fallbackH;
        if (w <= 0 || h <= 0) return null;
        Bitmap b = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(b);
        d.setBounds(0, 0, w, h);
        d.draw(canvas);
        return b;
    }

    private static void toast(Context c, String m) {
        Toast.makeText(c, m, Toast.LENGTH_SHORT).show();
    }

    private StickerDownload() {}
}
