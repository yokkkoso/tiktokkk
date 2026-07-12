package me.yokkkoso.tiktokkk.profile;

import me.yokkkoso.tiktokkk.Ids;
import me.yokkkoso.tiktokkk.Loc;
import me.yokkkoso.tiktokkk.Prefs;
import me.yokkkoso.tiktokkk.Reflect;
import me.yokkkoso.tiktokkk.TikToKKK;

import android.content.ClipData;
import android.content.ClipboardManager;
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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.OutputStream;
import java.util.Locale;

import android.os.Looper;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

public final class ProfileExtras {

    private static final android.os.Handler MAIN =
            new android.os.Handler(android.os.Looper.getMainLooper());
    private static final java.util.Set<Integer> WIRED = new java.util.HashSet<>();
    private static volatile Object avatarUrlModel;

    public static void install(ClassLoader cl) {
        XC_MethodHook cb = new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) {
                try {
                    if (Prefs.is(Prefs.PROFILE_PIC_SAVE) || Prefs.is(Prefs.COPY_BIO)) {
                        attach((View) param.args[0]);
                    }
                } catch (Throwable ignored) {}
            }
        };
        int n = 0;
        try {
            XposedHelpers.findAndHookMethod(ViewGroup.class, "addView",
                    View.class, int.class, ViewGroup.LayoutParams.class, cb);
            n++;
        } catch (Throwable ignored) {}
        try {
            // RecyclerView / custom layouts add children via addViewInLayout, bypassing addView
            XposedHelpers.findAndHookMethod(ViewGroup.class, "addViewInLayout",
                    View.class, int.class, ViewGroup.LayoutParams.class, boolean.class, cb);
            n++;
        } catch (Throwable ignored) {}
        try {
            // the "Подписчики/followers" label text is set AFTER addView, so detect it on setText
            XposedHelpers.findAndHookMethod(TextView.class, "setText",
                    CharSequence.class, TextView.BufferType.class, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    try {
                        if (!Prefs.is(Prefs.COPY_BIO) && !Prefs.is(Prefs.PROFILE_PIC_SAVE)) return;
                        TextView tv = (TextView) param.thisObject;
                        if (isFollowersLabel(tv) && WIRED.add(System.identityHashCode(tv))) {
                            wireProfileHeader(tv);
                        }
                    } catch (Throwable ignored) {}
                }
            });
            n++;
        } catch (Throwable ignored) {}
        try {
            Class<?> user = XposedHelpers.findClass("com.ss.android.ugc.aweme.profile.model.User", cl);
            XposedBridge.hookAllMethods(user, "getAvatarLarger", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam p) {
                    if (Looper.myLooper() == Looper.getMainLooper()
                            && Reflect.firstUrl(p.getResult()) != null) avatarUrlModel = p.getResult();
                }
            });
            n++;
        } catch (Throwable ignored) {}
        TikToKKK.log("profile extras installed (" + n + " hooks)");
    }

    private static void attach(View v) {
        if (v == null) return;
        if (Prefs.is(Prefs.PROFILE_PIC_SAVE)) {
            // feed avatar (desc "профиль X"); skip the comment sheet (owned by StickerDownload).
            if (isAvatar(v) && !inComment(v)) {
                wireSave(v, 800);
            } else if (v instanceof ImageView) {
                final View img = v;
                MAIN.postDelayed(() -> {
                    int w = img.getWidth(), h = img.getHeight();
                    int screenW = img.getResources().getDisplayMetrics().widthPixels;
                    boolean big = w >= screenW * 0.6f;
                    boolean square = h > 0 && w <= h * 1.4f && h <= w * 1.4f;
                    if (big && square && inAvatarViewer(img)) wireSave(img, 0);
                }, 500);
            }
        }
        if (v instanceof ViewGroup) {
            ViewGroup g = (ViewGroup) v;
            for (int i = 0; i < g.getChildCount(); i++) attach(g.getChildAt(i));
        }
    }

    private static void wireSave(final View v, long delayMs) {
        Runnable r = () -> {
            v.setLongClickable(true);
            v.setOnLongClickListener(view -> {
                savePic(view);
                return true;
            });
        };
        if (delayMs > 0) MAIN.postDelayed(r, delayMs); else r.run();
    }

    private static boolean inAvatarViewer(View v) {
        for (int i = 0; i < 6 && v != null; i++) {
            if (v instanceof ViewGroup && hasCloseButton((ViewGroup) v, 3)) return true;
            android.view.ViewParent p = v.getParent();
            v = (p instanceof View) ? (View) p : null;
        }
        return false;
    }

    private static boolean hasCloseButton(View v, int depth) {
        if (v == null || depth < 0) return false;
        CharSequence d = v.getContentDescription();
        if (d != null) {
            String s = d.toString().trim().toLowerCase(Locale.ROOT);
            if (s.equals("закрыть") || s.equals("close")) return true;
        }
        if (v instanceof ViewGroup) {
            ViewGroup g = (ViewGroup) v;
            for (int i = 0; i < g.getChildCount(); i++) {
                if (hasCloseButton(g.getChildAt(i), depth - 1)) return true;
            }
        }
        return false;
    }

    private static boolean isAvatar(View v) {
        String cls = v.getClass().getSimpleName();
        if (cls.contains("Avatar")) return true;
        CharSequence d = v.getContentDescription();
        if (d != null) {
            String s = d.toString().toLowerCase(Locale.ROOT);
            if (s.startsWith("профиль ") || s.startsWith("profile ")) return true;
        }
        String id = Ids.nameOf(v);
        return "user_avatar".equals(id) || "vnh".equals(id);
    }

    private static boolean inComment(View v) {
        for (int i = 0; i < 16 && v != null; i++) {
            String id = Ids.nameOf(v);
            if ("ecj".equals(id) || "ec6".equals(id) || "i4h".equals(id)) return true;
            android.view.ViewParent p = v.getParent();
            v = (p instanceof View) ? (View) p : null;
        }
        return false;
    }

    private static boolean isFollowersLabel(View v) {
        if (!(v instanceof TextView)) return false;
        CharSequence t = ((TextView) v).getText();
        if (t == null) return false;
        String s = t.toString().toLowerCase(Locale.ROOT);
        return s.contains("подписчик") || s.contains("follower");
    }

    private static void wireProfileHeader(View label) {
        Runnable r = () -> {
            View header = label;
            for (int i = 0; i < 7 && header.getParent() instanceof View; i++) {
                header = (View) header.getParent();
            }
            if (Prefs.is(Prefs.COPY_BIO)) copyTexts(header, 0);
        };
        MAIN.postDelayed(r, 700);
        MAIN.postDelayed(r, 1600);
    }

    private static void copyTexts(View v, int depth) {
        if (v == null || depth > 14) return;
        if (v instanceof TextView) {
            final TextView t = (TextView) v;
            // attach unconditionally (bio text loads async — reading it now would skip it);
            // the text is read at click time instead
            MAIN.postDelayed(() -> {
                t.setTextIsSelectable(false);   // stop native text-selection / translate
                t.setLongClickable(true);
                t.setOnLongClickListener(view -> {
                    CharSequence txt = ((TextView) view).getText();
                    if (txt == null || txt.length() == 0) return false;
                    Context c = view.getContext();
                    ClipboardManager cm = (ClipboardManager)
                            c.getSystemService(Context.CLIPBOARD_SERVICE);
                    if (cm != null) {
                        cm.setPrimaryClip(ClipData.newPlainText("tiktokkk", txt));
                        toast(c, Loc.t("Copied"));
                    }
                    return true;
                });
            }, 800);
        } else if (v instanceof ViewGroup) {
            ViewGroup g = (ViewGroup) v;
            for (int i = 0; i < g.getChildCount(); i++) copyTexts(g.getChildAt(i), depth + 1);
        }
    }

    private static void savePic(View anchor) {
        Context c = anchor.getContext();
        // prefer the full-res square original from the avatar URL (the view bitmap is round-cropped)
        String url = Reflect.firstUrl(avatarUrlModel);
        if (url != null) {
            downloadImage(c, url);
            return;
        }
        try {
            ImageView iv = anchor instanceof ImageView ? (ImageView) anchor : findImage(anchor, 8);
            View src = iv != null ? iv : anchor;
            Bitmap bmp = iv != null ? toBitmap(iv.getDrawable()) : null;
            if (bmp == null || bmp.getWidth() < 40 || bmp.getHeight() < 40) {
                bmp = renderView(src);
            }
            if (bmp == null) {
                toast(c, Loc.t("No image"));
                return;
            }
            ContentValues cv = new ContentValues();
            cv.put(MediaStore.Images.Media.DISPLAY_NAME, "tiktok_avatar_" + Math.abs(anchor.hashCode()) + ".jpg");
            cv.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
            if (Build.VERSION.SDK_INT >= 29) {
                cv.put(MediaStore.Images.Media.RELATIVE_PATH,
                        Environment.DIRECTORY_PICTURES + "/tiktokkk");
            }
            Uri uri = c.getContentResolver().insert(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI, cv);
            if (uri == null) {
                toast(c, Loc.t("Save failed"));
                return;
            }
            try (OutputStream os = c.getContentResolver().openOutputStream(uri)) {
                bmp.compress(Bitmap.CompressFormat.JPEG, 100, os);
            } catch (Throwable t) {
                try { c.getContentResolver().delete(uri, null, null); } catch (Throwable ignored) {}
                toast(c, Loc.t("Save failed"));
                return;
            }
            toast(c, Loc.t("Picture saved"));
        } catch (Throwable t) {
            toast(c, Loc.t("Save failed"));
        }
    }

    private static void downloadImage(final Context c, final String url) {
        toast(c, Loc.t("Downloading…"));
        new Thread(() -> {
            boolean ok = false;
            ContentValues cv = new ContentValues();
            cv.put(MediaStore.Images.Media.DISPLAY_NAME, "tiktok_avatar_" + System.nanoTime() + ".jpg");
            cv.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
            if (Build.VERSION.SDK_INT >= 29) {
                cv.put(MediaStore.Images.Media.RELATIVE_PATH,
                        Environment.DIRECTORY_PICTURES + "/tiktokkk");
            }
            Uri uri = c.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, cv);
            if (uri != null) {
                HttpURLConnection conn = null;
                try {
                    conn = (HttpURLConnection) new URL(url).openConnection();
                    conn.setConnectTimeout(15000);
                    conn.setReadTimeout(30000);
                    try (InputStream in = conn.getInputStream();
                         OutputStream os = c.getContentResolver().openOutputStream(uri)) {
                        byte[] buf = new byte[65536];
                        int r;
                        while ((r = in.read(buf)) > 0) os.write(buf, 0, r);
                    }
                    ok = true;
                } catch (Throwable ignored) {
                } finally {
                    if (conn != null) conn.disconnect();
                }
                if (!ok) {
                    try { c.getContentResolver().delete(uri, null, null); } catch (Throwable ignored) {}
                }
            }
            final boolean done = ok;
            MAIN.post(() -> toast(c, Loc.t(done ? "Picture saved" : "Save failed")));
        }).start();
    }

    private static ImageView findImage(View v, int depth) {
        ImageView[] best = {null};
        int[] bestArea = {0};
        collectImage(v, depth, best, bestArea);
        return best[0];
    }

    private static void collectImage(View v, int depth, ImageView[] best, int[] bestArea) {
        if (v == null || depth < 0) return;
        if (v instanceof ImageView) {
            Drawable d = ((ImageView) v).getDrawable();
            if (d != null) {
                int area = Math.max(0, d.getIntrinsicWidth()) * Math.max(0, d.getIntrinsicHeight());
                if (d.getIntrinsicWidth() >= 40 && d.getIntrinsicHeight() >= 40 && area > bestArea[0]) {
                    bestArea[0] = area;
                    best[0] = (ImageView) v;
                }
            }
        }
        if (v instanceof ViewGroup) {
            ViewGroup g = (ViewGroup) v;
            for (int i = 0; i < g.getChildCount(); i++) collectImage(g.getChildAt(i), depth - 1, best, bestArea);
        }
    }

    private static Bitmap renderView(View v) {
        if (v == null) return null;
        int w = v.getWidth(), h = v.getHeight();
        if (w <= 0 || h <= 0) return null;
        Bitmap b = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        v.draw(new Canvas(b));
        return b;
    }

    private static Bitmap toBitmap(Drawable d) {
        if (d == null) return null;
        if (d instanceof BitmapDrawable && ((BitmapDrawable) d).getBitmap() != null) {
            return ((BitmapDrawable) d).getBitmap();
        }
        int w = Math.max(1, d.getIntrinsicWidth());
        int h = Math.max(1, d.getIntrinsicHeight());
        Bitmap b = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(b);
        d.setBounds(0, 0, w, h);
        d.draw(canvas);
        return b;
    }

    private static void toast(Context c, String m) {
        Toast.makeText(c, m, Toast.LENGTH_SHORT).show();
    }

    private ProfileExtras() {}
}
