package me.yokkkoso.tiktokkk;

import me.yokkkoso.tiktokkk.feed.FeedFilter;
import me.yokkkoso.tiktokkk.feed.SearchAdFilter;
import me.yokkkoso.tiktokkk.feed.RefreshBlock;
import me.yokkkoso.tiktokkk.feed.AuthorDates;
import me.yokkkoso.tiktokkk.feed.FeedDecorator;
import me.yokkkoso.tiktokkk.feed.SeekBar;
import me.yokkkoso.tiktokkk.feed.CommentConfirm;
import me.yokkkoso.tiktokkk.download.VideoDownloader;
import me.yokkkoso.tiktokkk.download.StickerDownload;
import me.yokkkoso.tiktokkk.download.DownloadUnlock;
import me.yokkkoso.tiktokkk.download.WatermarkRemover;
import me.yokkkoso.tiktokkk.profile.ProfileExtras;
import me.yokkkoso.tiktokkk.profile.AnonymousView;
import me.yokkkoso.tiktokkk.region.RegionSpoof;
import me.yokkkoso.tiktokkk.tabbar.TabBar;
import me.yokkkoso.tiktokkk.links.UrlSanitizer;
import me.yokkkoso.tiktokkk.ui.OverlayFab;

import android.app.AlertDialog;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public class TikToKKK implements IXposedHookLoadPackage {

    private static final String TAG = "TikToKKK";
    private static final String TARGET = "com.zhiliaoapp.musically";
    private static final String TARGET_TRILL = "com.ss.android.ugc.trill";
    static final String TESTED_VERSION = "46.0.3";
    private static boolean versionLogged;

    @Override
    public void handleLoadPackage(LoadPackageParam lpparam) {
        if (!TARGET.equals(lpparam.packageName) && !TARGET_TRILL.equals(lpparam.packageName)) return;
        log("loaded into " + lpparam.packageName);

        ClassLoader cl = lpparam.classLoader;
        safe("clickConfirmations", this::hookClickConfirmations);
        safe("forceProgressBar", this::hookForceProgressBar);
        safe("hidePlusButton", this::hookHidePlusButton);
        safe("disableSecure", this::hookDisableSecure);
        safe("modMenu", OverlayFab::installTrigger);
        safe("settingsEntry", () -> me.yokkkoso.tiktokkk.ui.SettingsEntry.install(cl));
        safe("feedFilter", () -> FeedFilter.install(cl));
        safe("searchAdFilter", () -> SearchAdFilter.install(cl));
        safe("refreshBlock", () -> RefreshBlock.install(cl));
        safe("authorDates", () -> AuthorDates.install(cl));
        safe("feedDecorator", () -> FeedDecorator.install(cl));
        safe("anonymousView", () -> AnonymousView.install(cl));
        safe("urlSanitizer", () -> UrlSanitizer.install(cl));
        safe("regionSpoof", () -> RegionSpoof.install(cl));
        safe("tabBar", () -> TabBar.install(cl));
        safe("profileExtras", () -> ProfileExtras.install(cl));
        safe("commentConfirm", () -> CommentConfirm.install(cl));
        safe("favoriteConfirm", () -> me.yokkkoso.tiktokkk.feed.FavoriteConfirm.install(cl));
        safe("downloadUnlock", () -> DownloadUnlock.install(cl));
        safe("watermarkRemover", () -> WatermarkRemover.install(cl));
        safe("videoDownloader", () -> VideoDownloader.install(cl));
        safe("stickerDownload", () -> StickerDownload.install(cl));
        safe("seekBar", () -> SeekBar.install(cl));
    }

    private static final ThreadLocal<Boolean> BYPASS = new ThreadLocal<>();

    private void hookClickConfirmations() {
        XposedHelpers.findAndHookMethod(View.class, "performClick", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                if (Boolean.TRUE.equals(BYPASS.get())) return;
                final View v = (View) param.thisObject;
                if (isOwnUi(v)) return;

                if (Prefs.is(Prefs.DEBUG_CLICKS)) {
                    log("click target: id=" + Ids.nameOf(v)
                            + " desc=" + v.getContentDescription()
                            + " cls=" + v.getClass().getSimpleName());
                }

                if (!confirmsOrChooserActive()) return;

                final String prompt = promptFor(v);
                if (prompt != null) {
                    param.setResult(true);
                    dialog(v, () -> new AlertDialog.Builder(activityOf(v.getContext()))
                            .setMessage(prompt)
                            .setNegativeButton(android.R.string.cancel, null)
                            .setPositiveButton(android.R.string.ok, (d, w) -> replay(v))
                            .show());
                    return;
                }
            }
        });
    }

    private void dialog(View v, Runnable show) {
        android.app.Activity act = activityOf(v.getContext());
        if (act == null || act.isFinishing()) {
            replay(v);
            return;
        }
        v.post(() -> {
            try {
                show.run();
            } catch (Throwable t) {
                replay(v);
            }
        });
    }

    private static void replay(View v) {
        BYPASS.set(Boolean.TRUE);
        try {
            v.performClick();
        } finally {
            BYPASS.set(Boolean.FALSE);
        }
    }

    private boolean confirmsOrChooserActive() {
        return Prefs.is(Prefs.CONFIRM_LIKE) || Prefs.is(Prefs.CONFIRM_UNLIKE)
                || Prefs.is(Prefs.CONFIRM_FOLLOW)
                || Prefs.is(Prefs.CONFIRM_COMMENT_LIKE) || Prefs.is(Prefs.CONFIRM_DISLIKE_COMMENT)
                || Prefs.is(Prefs.CONFIRM_STORY_LIKE) || Prefs.is(Prefs.CONFIRM_QUICK_SHARE)
                || Prefs.is(Prefs.CONFIRM_QUICK_REPOST);
    }

    private boolean idBtn(View v, java.util.List<String> ids) {
        return Ids.inSubtreeExact(v, ids, 4) || Ids.inAncestryExact(v, ids, 2);
    }

    private String promptFor(View v) {
        java.util.List<String> descs = new java.util.ArrayList<>();
        collectUp(v, descs, 2);
        collectDown(v, descs, 4);

        boolean storyCtx = false, commentCtx = false, unlikeDesc = false;
        boolean followDesc = false, repostDesc = false, likeCommentDesc = false, dislikeDesc = false;
        boolean shareQuick = false;
        for (String d : descs) {
            if (d.startsWith("поделиться с ") || d.startsWith("share with ")) shareQuick = true;
            if (d.contains("истори") || d.contains("story")) storyCtx = true;
            if (d.contains("коммент") || d.contains("comment")) commentCtx = true;
            if (d.startsWith("unlike") || d.startsWith("убрать лайк") || d.startsWith("отменить лайк")
                    || d.startsWith("удалить лайк") || d.startsWith("вам понравилось")
                    || d.startsWith("you liked") || d.contains("понравилось это видео")) unlikeDesc = true;
            if ((d.startsWith("follow") && !d.startsWith("following")
                    && !d.startsWith("followers") && !d.startsWith("unfollow"))
                    || d.startsWith("подписаться")) followDesc = true;
            // only the quick-repost button, NOT the profile "Reposts" tab (bare "репост")
            if (d.startsWith("репост подписчикам") || d.startsWith("repost to")
                    || d.startsWith("quick repost")) repostDesc = true;
            if (d.startsWith("like comment") || d.equals("палец вверх") || d.equals("thumbs up")
                    || (d.contains("comment") && d.contains("like"))) likeCommentDesc = true;
            if (d.startsWith("dislike") || d.equals("палец вниз") || d.equals("thumbs down")
                    || d.contains("не нравится") || d.contains("понизить")) dislikeDesc = true;
        }

        boolean likeBtn = idBtn(v, Ids.LIKE_BTN);
        boolean followBtn = idBtn(v, Ids.FOLLOW_BTN);
        boolean commentLikeBtn = idBtn(v, Ids.COMMENT_LIKE);

        boolean unlikeVideo = likeBtn && unlikeDesc;
        boolean likeVideo = likeBtn && !unlikeDesc && !commentCtx && !storyCtx;
        boolean storyLike = likeBtn && storyCtx;
        boolean likeComment = commentLikeBtn || likeCommentDesc;
        boolean dislikeComment = dislikeDesc;
        boolean follow = followBtn || followDesc;
        boolean share = shareQuick;
        boolean repost = repostDesc;

        if (Prefs.is(Prefs.CONFIRM_FOLLOW) && follow) return Loc.t("Follow this account?");
        if (Prefs.is(Prefs.CONFIRM_COMMENT_LIKE) && likeComment) return Loc.t("Like this comment?");
        if (Prefs.is(Prefs.CONFIRM_DISLIKE_COMMENT) && dislikeComment) return Loc.t("Dislike this comment?");
        if (Prefs.is(Prefs.CONFIRM_STORY_LIKE) && storyLike) return Loc.t("Like this story?");
        if (Prefs.is(Prefs.CONFIRM_QUICK_REPOST) && repost) return Loc.t("Repost this video?");
        if (Prefs.is(Prefs.CONFIRM_QUICK_SHARE) && share) return Loc.t("Share this video?");
        if (Prefs.is(Prefs.CONFIRM_UNLIKE) && unlikeVideo) return Loc.t("Remove like from this video?");
        if (Prefs.is(Prefs.CONFIRM_LIKE) && likeVideo) return Loc.t("Like this video?");
        return null;
    }

    private void collectUp(View v, java.util.List<String> out, int levels) {
        int n = 0;
        while (v != null && n <= levels) {
            addDesc(v, out);
            android.view.ViewParent p = v.getParent();
            v = (p instanceof View) ? (View) p : null;
            n++;
        }
    }

    private void collectDown(View v, java.util.List<String> out, int depth) {
        if (v == null || depth < 0) return;
        if (v instanceof ViewGroup) {
            ViewGroup g = (ViewGroup) v;
            for (int i = 0; i < g.getChildCount(); i++) {
                View c = g.getChildAt(i);
                addDesc(c, out);
                collectDown(c, out, depth - 1);
            }
        }
    }

    private void addDesc(View v, java.util.List<String> out) {
        CharSequence d = v.getContentDescription();
        if (d != null && d.length() > 0) out.add(d.toString().trim().toLowerCase());
        if (v instanceof android.widget.TextView) {
            CharSequence t = ((android.widget.TextView) v).getText();
            if (t != null && t.length() > 0 && t.length() < 40) out.add(t.toString().trim().toLowerCase());
        }
    }

    private void hookForceProgressBar() {
        XposedHelpers.findAndHookMethod(View.class, "setVisibility", int.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                View v = (View) param.thisObject;
                if (Prefs.is(Prefs.HIDE_AI_ASSISTANT)
                        && (int) param.args[0] == View.VISIBLE && Ids.matches(v, Ids.AI_ASSISTANT)) {
                    param.args[0] = View.GONE;
                }
            }
        });
    }

    private void hookDisableSecure() {
        final int SECURE = android.view.WindowManager.LayoutParams.FLAG_SECURE;
        XposedHelpers.findAndHookMethod(android.view.Window.class, "setFlags",
                int.class, int.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                param.args[0] = ((int) param.args[0]) & ~SECURE;
                param.args[1] = ((int) param.args[1]) | SECURE;
            }
        });
        XposedHelpers.findAndHookMethod(android.view.Window.class, "addFlags",
                int.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                param.args[0] = ((int) param.args[0]) & ~SECURE;
            }
        });
    }

    private void hookHidePlusButton() {
        XposedHelpers.findAndHookMethod(ViewGroup.class, "addView",
                View.class, int.class, ViewGroup.LayoutParams.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) {
                hideTargetsIn((View) param.args[0]);
            }
        });
    }

    private void hideTargetsIn(View v) {
        if (v == null) return;
        if (Prefs.is(Prefs.DEBUG_CLICKS)) {
            String id = Ids.nameOf(v);
            if (id != null && (id.contains("tako") || id.contains("ai") || id.contains("aigc")
                    || id.contains("entrance") || id.contains("assistant"))) {
                log("addView id=" + id + " desc=" + v.getContentDescription()
                        + " cls=" + v.getClass().getSimpleName());
            }
        }
        boolean plus = Prefs.is(Prefs.HIDE_PLUS_BUTTON);
        boolean ai = Prefs.is(Prefs.HIDE_AI_ASSISTANT);
        if ((plus && Ids.matches(v, Ids.CREATE_TAB))
                || (ai && Ids.matches(v, Ids.AI_ASSISTANT))) {
            v.setVisibility(View.GONE);
            return;
        }
        if (v instanceof ViewGroup) {
            ViewGroup g = (ViewGroup) v;
            for (int i = 0; i < g.getChildCount(); i++) hideTargetsIn(g.getChildAt(i));
        }
    }

    private void safe(String name, Runnable r) {
        try {
            r.run();
        } catch (Throwable t) {
            log("hook '" + name + "' failed: " + t);
        }
    }

    public static void log(String m) {
        XposedBridge.log(TAG + ": " + m);
        Logs.add(m);
    }

    public static void logHostVersion(Context c) {
        if (versionLogged || c == null) return;
        versionLogged = true;
        try {
            android.content.pm.PackageInfo pi =
                    c.getPackageManager().getPackageInfo(c.getPackageName(), 0);
            log("host " + c.getPackageName() + " " + pi.versionName + " (vc " + pi.versionCode
                    + "); tested on TikTok " + TESTED_VERSION);
            if (!TESTED_VERSION.equals(pi.versionName)) {
                log("WARNING: untested TikTok version — obfuscated-symbol features may not work");
            }
        } catch (Throwable ignored) {}
    }

    public static android.app.Activity activityOf(Context c) {
        while (c instanceof android.content.ContextWrapper) {
            if (c instanceof android.app.Activity) return (android.app.Activity) c;
            c = ((android.content.ContextWrapper) c).getBaseContext();
        }
        return null;
    }

    static boolean isOwnUi(View v) {
        for (int i = 0; i < 30 && v != null; i++) {
            Object tag = v.getTag();
            if (tag instanceof String && ((String) tag).startsWith("kkk")) return true;
            android.view.ViewParent p = v.getParent();
            v = (p instanceof View) ? (View) p : null;
        }
        return false;
    }
}
