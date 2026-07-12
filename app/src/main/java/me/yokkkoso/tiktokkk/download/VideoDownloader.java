package me.yokkkoso.tiktokkk.download;

import me.yokkkoso.tiktokkk.Loc;
import me.yokkkoso.tiktokkk.Reflect;
import me.yokkkoso.tiktokkk.TikToKKK;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Looper;
import android.provider.MediaStore;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.json.JSONObject;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

public final class VideoDownloader {

    private static volatile Object currentAweme;

    public static void install(ClassLoader cl) {
        try {
            Class<?> aweme = XposedHelpers.findClass("com.ss.android.ugc.aweme.feed.model.Aweme", cl);
            XposedBridge.hookAllMethods(aweme, "getVideo", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam p) {
                    if (Looper.myLooper() == Looper.getMainLooper()) currentAweme = p.thisObject;
                }
            });
            TikToKKK.log("video downloader installed");
        } catch (Throwable t) {
            TikToKKK.log("video downloader install failed: " + t);
        }
    }

    private static final class Cand {
        String label;
        Object urlModel;
        long score;
        boolean image;
    }

    public static void download(Activity a) {
        Object aweme = currentAweme;
        if (aweme == null) {
            toast(a, Loc.t("No video"));
            return;
        }
        showPicker(a, aweme);
    }

    private static void showPicker(final Activity a, final Object aweme) {
        try {
            Object video = Reflect.call(aweme, "getVideo");
            final List<Cand> cands = video != null ? candidates(video) : new ArrayList<>();
            if (cands.isEmpty()) cands.addAll(imageCandidates(aweme));
            if (cands.isEmpty()) {
                toast(a, Loc.t("No download URL"));
                return;
            }
            final boolean photo = cands.get(0).image;
            final boolean many = photo && cands.size() > 1;
            final String id = photo ? "" : awemeId(aweme);
            final boolean original = !photo && !id.isEmpty();
            List<String> labels = new ArrayList<>();
            if (original) labels.add(Loc.t("Original (HQ)"));
            if (many) labels.add(Loc.t("All"));
            for (Cand c : cands) labels.add(c.label);
            new AlertDialog.Builder(a, android.R.style.Theme_Material_Dialog_Alert)
                    .setTitle(Loc.t(photo ? "Download photo" : "Download quality"))
                    .setItems(labels.toArray(new String[0]), (d, which) -> {
                        int idx = which;
                        if (original) {
                            if (idx == 0) { hqDownload(a, aweme, id); return; }
                            idx--;
                        }
                        if (many && idx == 0) {
                            for (Cand c : cands) save(a, Reflect.firstUrl(c.urlModel), true);
                        } else {
                            Cand c = cands.get(many ? idx - 1 : idx);
                            save(a, Reflect.firstUrl(c.urlModel), c.image);
                        }
                    })
                    .show();
        } catch (Throwable t) {
            TikToKKK.log("download picker failed: " + t);
            toast(a, Loc.t("Save failed"));
        }
    }

    private static List<Cand> imageCandidates(Object aweme) {
        List<Cand> out = new ArrayList<>();
        Object info = Reflect.call(aweme, "getPhotoModeImageInfo");
        if (info == null) return out;
        Object list = Reflect.call(info, "getImageList");
        if (list instanceof List && !((List<?>) list).isEmpty()) {
            int i = 1;
            for (Object el : (List<?>) list) {
                Object url = Reflect.field(el, "displayImageNoWatermark");
                if (!Reflect.hasUrls(url)) url = Reflect.field(el, "ownerWatermarkImage");
                if (!Reflect.hasUrls(url)) url = Reflect.field(el, "thumbnail");
                addImage(out, url, i++);
            }
        }
        if (out.isEmpty()) {
            Object list2 = Reflect.call(info, "getPhotoModeImageList");
            if (list2 instanceof List) {
                int i = 1;
                for (Object el : (List<?>) list2) addImage(out, Reflect.field(el, "urlModel"), i++);
            }
        }
        return out;
    }

    private static void addImage(List<Cand> out, Object url, int i) {
        if (Reflect.hasUrls(url)) {
            Cand c = new Cand();
            c.urlModel = url;
            c.image = true;
            c.label = Loc.t("Photo") + " " + i;
            c.score = 1000 - i;
            out.add(c);
        }
    }

    static boolean isAudioOnly(Object br, Object addr) {
        if (Reflect.str(br, "getGearName").toLowerCase(Locale.ROOT).contains("audio")) return true;
        if (Reflect.intVal(br, "getMediaType") == 4) return true;   // 4 = audio in TikTok's media types
        String u = Reflect.str(addr, "getUri").toLowerCase(Locale.ROOT);
        if (u.contains("-audio") || u.contains("_audio") || u.contains("/audio")) return true;
        String url = Reflect.firstUrl(addr);
        if (url != null) {
            String lu = url.toLowerCase(Locale.ROOT);
            if (lu.contains("mime_type=audio") || lu.contains("is_audio=1") || lu.endsWith(".mp3")) return true;
        }
        return false;
    }

    private static List<Cand> candidates(Object video) {
        List<Cand> out = new ArrayList<>();
        Object list = Reflect.field(video, "bitRate");
        if (list instanceof List) {
            for (Object br : (List<?>) list) {
                Object addr = Reflect.field(br, "playAddr");
                if (!Reflect.hasUrls(addr)) continue;
                if (isAudioOnly(br, addr)) continue;
                int res = Math.max(Reflect.intVal(addr, "getWidth"), Reflect.intVal(addr, "getHeight"));
                if (res == 0) res = Math.max(Reflect.intVal(br, "getWidth"), Reflect.intVal(br, "getHeight"));
                if (res == 0) continue;
                String gear = Reflect.str(br, "getGearName");
                Cand c = new Cand();
                c.urlModel = addr;
                c.label = res + "p" + (gear.isEmpty() ? "" : "  ·  " + gear);
                c.score = (long) res * 1000L + Reflect.intVal(br, "getBitRate") / 1000L;
                out.add(c);
            }
        }
        for (String f : new String[]{"downloadNoWatermarkAddr", "newDownloadAddr", "downloadAddr"}) {
            Object addr = Reflect.field(video, f);
            if (Reflect.hasUrls(addr)) {
                Cand c = new Cand();
                c.urlModel = addr;
                c.label = f.equals("downloadAddr")
                        ? Loc.t("Standard") : Loc.t("Standard (No watermark)");
                c.score = f.equals("downloadNoWatermarkAddr") ? 500 : 1;
                out.add(c);
                break;
            }
        }
        Collections.sort(out, (x, y) -> Long.compare(y.score, x.score));
        return out;
    }

    private static void hqDownload(final Activity a, final Object aweme, final String id) {
        progress(a, Loc.t("Resolving HQ…"));
        new Thread(() -> {
            String url = tikwmHd(aweme, id, a);
            boolean ok = url != null && writeToGallery(a, url, false, a);
            if (ok) {
                a.runOnUiThread(() -> toast(a, Loc.t("Saved to gallery")));
            } else {
                a.runOnUiThread(() -> {
                    toast(a, Loc.t("HQ unavailable - pick a quality"));
                    showPicker(a, aweme);
                });
            }
        }).start();
    }

    private static String awemeId(Object aweme) {
        for (String m : new String[]{"getAwemeId", "getAid", "getGroupId"}) {
            String v = Reflect.str(aweme, m);
            if (!v.isEmpty() && !"0".equals(v) && !"null".equals(v)) return v;
        }
        return "";
    }

    private static String tikwmHd(Object aweme, String id, Activity a) {
        String handle = Reflect.str(Reflect.call(aweme, "getAuthor"), "getUniqueId");
        if (handle.isEmpty()) handle = "i";
        String share = "https://www.tiktok.com/@" + handle + "/video/" + id;
        String enc = enc(share);
        try {
            String sub = httpBody("https://www.tikwm.com/api/video/task/submit", "url=" + enc + "&web=1");
            String taskId = new JSONObject(sub).getJSONObject("data").getString("task_id");
            String resultUrl = "https://www.tikwm.com/api/video/task/result?task_id=" + enc(taskId);
            for (int i = 0; i < 30; i++) {
                JSONObject d = new JSONObject(httpBody(resultUrl, null)).getJSONObject("data");
                int st = d.optInt("status", -1);
                if (st == 2) {
                    String u = d.getJSONObject("detail").optString("play_url");
                    if (!u.isEmpty()) return u;
                    break;
                }
                if (st == 3) break;   // failed
                progress(a, Loc.t("Resolving HQ…") + " " + (i + 1) + "s");
                try { Thread.sleep(1000); } catch (InterruptedException e) { break; }
            }
        } catch (Throwable ignored) {}
        try {
            JSONObject d = new JSONObject(httpBody("https://www.tikwm.com/api/?url=" + enc + "&hd=1", null))
                    .getJSONObject("data");
            String u = d.optString("hdplay");
            if (u.isEmpty()) u = d.optString("play");
            if (!u.isEmpty()) return u.startsWith("http") ? u : "https://www.tikwm.com" + u;
        } catch (Throwable ignored) {}
        return "https://tikwm.com/video/media/hdplay/" + id + ".mp4";
    }

    private static String enc(String s) {
        try {
            return URLEncoder.encode(s, "UTF-8");
        } catch (Throwable t) {
            return s;
        }
    }

    private static String httpBody(String url, String body) {
        HttpURLConnection conn = null;
        try {
            conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Linux; Android)");
            conn.setConnectTimeout(15000);
            conn.setReadTimeout(30000);
            if (body != null) {
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                try (OutputStream os = conn.getOutputStream()) {
                    os.write(body.getBytes("UTF-8"));
                }
            }
            ByteArrayOutputStream bo = new ByteArrayOutputStream();
            try (InputStream in = conn.getInputStream()) {
                byte[] buf = new byte[8192];
                int n;
                while ((n = in.read(buf)) > 0) bo.write(buf, 0, n);
            }
            return bo.toString("UTF-8");
        } catch (Throwable t) {
            return null;
        } finally {
            if (conn != null) conn.disconnect();
        }
    }

    private static void save(final Activity a, final String url, final boolean image) {
        if (url == null) {
            toast(a, Loc.t("No download URL"));
            return;
        }
        toast(a, Loc.t("Downloading…"));
        new Thread(() -> {
            boolean ok = writeToGallery(a, url, image, null);
            a.runOnUiThread(() -> toast(a, Loc.t(ok ? "Saved to gallery" : "Save failed")));
        }).start();
    }

    // prog != null -> report download percentage as a live toast (used for the long HQ download).
    private static boolean writeToGallery(final Activity a, final String url, final boolean image,
            final Activity prog) {
        ContentValues cv = new ContentValues();
        cv.put(MediaStore.MediaColumns.DISPLAY_NAME,
                "tiktok_" + System.nanoTime() + (image ? ".jpg" : ".mp4"));
        cv.put(MediaStore.MediaColumns.MIME_TYPE, image ? "image/jpeg" : "video/mp4");
        if (Build.VERSION.SDK_INT >= 29) {
            cv.put(MediaStore.MediaColumns.RELATIVE_PATH,
                    (image ? Environment.DIRECTORY_PICTURES : Environment.DIRECTORY_MOVIES) + "/tiktokkk");
        }
        Uri collection = image ? MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                : MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        Uri uri = a.getContentResolver().insert(collection, cv);
        if (uri == null) return false;
        boolean ok = false;
        HttpURLConnection conn = null;
        try {
            conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setRequestProperty("User-Agent", "com.ss.android.ugc.trill/2023 (Linux; Android)");
            conn.setConnectTimeout(15000);
            conn.setReadTimeout(30000);
            long total = conn.getContentLengthLong();
            try (InputStream in = conn.getInputStream();
                 OutputStream os = a.getContentResolver().openOutputStream(uri)) {
                byte[] buf = new byte[65536];
                int n;
                long done = 0, lastNs = 0;
                while ((n = in.read(buf)) > 0) {
                    os.write(buf, 0, n);
                    done += n;
                    if (prog != null && System.nanoTime() - lastNs > 400_000_000L) {
                        lastNs = System.nanoTime();
                        String p = total > 0 ? (done * 100 / total) + "%"
                                : (done / 1048576) + " MB";
                        progress(prog, Loc.t("Downloading HQ…") + " " + p);
                    }
                }
            }
            ok = true;
        } catch (Throwable t) {
            TikToKKK.log("save failed: " + t);
        } finally {
            if (conn != null) conn.disconnect();
        }
        if (!ok) {
            try { a.getContentResolver().delete(uri, null, null); } catch (Throwable ignored) {}
        }
        return ok;
    }

    private static void toast(Context c, String m) {
        Toast.makeText(c, m, Toast.LENGTH_SHORT).show();
    }

    private static Toast progToast;

    // A single reused toast, re-shown so it stays visible through the long HQ resolve + download.
    private static void progress(final Activity a, final String msg) {
        a.runOnUiThread(() -> {
            try {
                if (progToast == null) progToast = Toast.makeText(a, msg, Toast.LENGTH_SHORT);
                else progToast.setText(msg);
                progToast.show();
            } catch (Throwable ignored) {}
        });
    }

    private VideoDownloader() {}
}
