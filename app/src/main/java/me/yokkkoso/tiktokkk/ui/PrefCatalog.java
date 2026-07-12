package me.yokkkoso.tiktokkk.ui;

import java.util.LinkedHashMap;
import java.util.Map;

import me.yokkkoso.tiktokkk.Prefs;

public final class PrefCatalog {

    public static final String REGION_CAT = "Region";
    public static final String MISC_CAT = "Misc";
    public static final String[][] CATEGORIES = {
            {"Confirmations", "🔒", "Like / follow / comment prompts"},
            {"Feed", "📺", "Ads, live, stories, progress bar"},
            {"Profile", "👤", "Post stats, anonymous view"},
            {"Downloads", "⬇️", "Watermark, quality"},
            {"Tab Bar", "📌", "Plus button, labels, badges"},
            {REGION_CAT, "🌍", "Spoof country / SIM"},
            {MISC_CAT, "🎨", "Accent, import / export / reset"},
            {"Advanced", "⚙️", "Debug options"},
    };

    public static final Map<String, String[][]> GROUPS = new LinkedHashMap<>();
    public static final Map<String, String> LABELS = new LinkedHashMap<>();
    public static final Map<String, String> DESCS = new LinkedHashMap<>();
    static {
        GROUPS.put("Confirmations", new String[][]{
                {"", Prefs.CONFIRM_LIKE, Prefs.CONFIRM_UNLIKE, Prefs.CONFIRM_COMMENT_LIKE,
                        Prefs.CONFIRM_DISLIKE_COMMENT, Prefs.CONFIRM_STORY_LIKE, Prefs.CONFIRM_FOLLOW,
                        Prefs.CONFIRM_QUICK_SHARE, Prefs.CONFIRM_QUICK_REPOST,
                        Prefs.CONFIRM_FAVORITE, Prefs.CONFIRM_UNFAVORITE}});
        GROUPS.put("Feed", new String[][]{
                {"Content & Display", Prefs.SHOW_FYP_TIMESTAMP, Prefs.SHOW_POST_REGION,
                        Prefs.FORCE_PROGRESS_BAR, Prefs.HIDE_AI_ASSISTANT, Prefs.HIDE_FIND_SIMILAR,
                        Prefs.HIDE_SEARCH_BAR, Prefs.DISABLE_SCROLL_REFRESH, Prefs.DISABLE_HOME_REFRESH},
                {"Filtering", Prefs.HIDE_FEED_ADS, Prefs.HIDE_REWARDS_ADS, Prefs.HIDE_LIVE,
                        Prefs.HIDE_SHOP, Prefs.HIDE_COMMISSION, Prefs.HIDE_LOCATION_ADS,
                        Prefs.HIDE_AI_POSTS, Prefs.HIDE_SLIDESHOW, Prefs.HIDE_STORY,
                        Prefs.HIDE_FRIEND_SUGGEST}});
        GROUPS.put("Profile", new String[][]{
                {"", Prefs.PROFILE_PIC_SAVE, Prefs.COPY_BIO, Prefs.ANONYMOUS_PROFILE_VIEW}});
        GROUPS.put("Downloads", new String[][]{
                {"", Prefs.SHOW_DL_BUTTON, Prefs.STICKER_DOWNLOAD, Prefs.REMOVE_WATERMARK,
                        Prefs.ALLOW_ALL_DOWNLOADS}});
        GROUPS.put("Tab Bar", new String[][]{
                {"", Prefs.HIDE_PLUS_BUTTON, Prefs.HIDE_TAB_LABELS,
                        Prefs.HIDE_FRIENDS_BADGE, Prefs.HIDE_INBOX_BADGE}});
        GROUPS.put("Advanced", new String[][]{
                {"", Prefs.DEBUG_CLICKS}});

        LABELS.put(Prefs.CONFIRM_LIKE, "Confirm before like");
        LABELS.put(Prefs.CONFIRM_FOLLOW, "Confirm before follow");
        LABELS.put(Prefs.CONFIRM_UNLIKE, "Confirm before unlike");
        LABELS.put(Prefs.CONFIRM_COMMENT_LIKE, "Confirm before comment like");
        LABELS.put(Prefs.CONFIRM_DISLIKE_COMMENT, "Confirm before comment dislike");
        LABELS.put(Prefs.CONFIRM_STORY_LIKE, "Confirm before story like");
        LABELS.put(Prefs.CONFIRM_QUICK_SHARE, "Confirm before quick share");
        LABELS.put(Prefs.CONFIRM_QUICK_REPOST, "Confirm before quick repost");
        LABELS.put(Prefs.CONFIRM_FAVORITE, "Confirm before favorite");
        LABELS.put(Prefs.CONFIRM_UNFAVORITE, "Confirm before unfavorite");
        LABELS.put(Prefs.HIDE_FEED_ADS, "Hide ads");
        LABELS.put(Prefs.HIDE_LIVE, "Disable live streaming (in feed)");
        LABELS.put(Prefs.HIDE_SLIDESHOW, "Hide photo slideshows");
        LABELS.put(Prefs.HIDE_SHOP, "Hide Shop posts");
        LABELS.put(Prefs.HIDE_STORY, "Hide stories");
        LABELS.put(Prefs.HIDE_FIND_SIMILAR, "Disable Visual Search tag");
        LABELS.put(Prefs.HIDE_SEARCH_BAR, "Hide fast-search bar");
        LABELS.put(Prefs.DISABLE_SCROLL_REFRESH, "Disable scroll-to-top refresh");
        LABELS.put(Prefs.DISABLE_HOME_REFRESH, "Disable Home-tap refresh");
        LABELS.put(Prefs.HIDE_FRIEND_SUGGEST, "Skip friend recommendations");
        LABELS.put(Prefs.HIDE_REWARDS_ADS, "Disable TikTok Rewards ads");
        LABELS.put(Prefs.HIDE_COMMISSION, "Hide commission posts");
        LABELS.put(Prefs.HIDE_LOCATION_ADS, "Hide location ads");
        LABELS.put(Prefs.HIDE_AI_POSTS, "Hide AI-generated posts");
        LABELS.put(Prefs.FORCE_PROGRESS_BAR, "Always show progress bar");
        LABELS.put(Prefs.SHOW_FYP_TIMESTAMP, "Show upload date");
        LABELS.put(Prefs.SHOW_POST_REGION, "Show upload region");
        LABELS.put(Prefs.PROFILE_PIC_SAVE, "Save profile picture");
        LABELS.put(Prefs.COPY_BIO, "Copy bio");
        LABELS.put(Prefs.ANONYMOUS_PROFILE_VIEW, "Anonymous profile view (exp.)");
        LABELS.put(Prefs.REMOVE_WATERMARK, "Download without watermark");
        LABELS.put(Prefs.SHOW_DL_BUTTON, "Show download button");
        LABELS.put(Prefs.STICKER_DOWNLOAD, "Save comment stickers");
        LABELS.put(Prefs.ALLOW_ALL_DOWNLOADS, "Allow all downloads");
        LABELS.put(Prefs.HIDE_PLUS_BUTTON, "Hide Plus button");
        LABELS.put(Prefs.HIDE_TAB_LABELS, "Hide tab bar labels");
        LABELS.put(Prefs.HIDE_FRIENDS_BADGE, "Hide Friends badge");
        LABELS.put(Prefs.HIDE_INBOX_BADGE, "Hide Inbox badge");
        LABELS.put(Prefs.HIDE_AI_ASSISTANT, "Remove TikTok AI button");
        LABELS.put(Prefs.SANITIZE_LINKS, "Sanitize shared links");
        LABELS.put(Prefs.DEBUG_CLICKS, "Debug: log click targets");

        DESCS.put(Prefs.CONFIRM_LIKE, "Ask before liking a video");
        DESCS.put(Prefs.CONFIRM_FOLLOW, "Ask before following an account");
        DESCS.put(Prefs.CONFIRM_UNLIKE, "Ask before removing a like");
        DESCS.put(Prefs.CONFIRM_COMMENT_LIKE, "Ask before liking a comment");
        DESCS.put(Prefs.CONFIRM_DISLIKE_COMMENT, "Ask before disliking a comment");
        DESCS.put(Prefs.CONFIRM_STORY_LIKE, "Ask before liking a story");
        DESCS.put(Prefs.CONFIRM_QUICK_SHARE, "Ask before quick-sharing a video");
        DESCS.put(Prefs.CONFIRM_QUICK_REPOST, "Ask before reposting a video");
        DESCS.put(Prefs.CONFIRM_FAVORITE, "Ask before adding a video to favorites");
        DESCS.put(Prefs.CONFIRM_UNFAVORITE, "Ask before removing a video from favorites");
        DESCS.put(Prefs.HIDE_FEED_ADS, "Remove sponsored posts from the feed");
        DESCS.put(Prefs.HIDE_LIVE, "Skip live streams in the feed");
        DESCS.put(Prefs.HIDE_SLIDESHOW, "Skip photo slideshow posts");
        DESCS.put(Prefs.HIDE_SHOP, "Skip TikTok Shop product posts");
        DESCS.put(Prefs.HIDE_STORY, "Skip stories in the feed");
        DESCS.put(Prefs.HIDE_FIND_SIMILAR, "Hide the visual-search tag on posts");
        DESCS.put(Prefs.HIDE_SEARCH_BAR, "Hide the inline \"Search · …\" suggestion bar in the feed");
        DESCS.put(Prefs.DISABLE_SCROLL_REFRESH, "Don't refresh the feed when scrolling to the top");
        DESCS.put(Prefs.DISABLE_HOME_REFRESH, "Don't refresh the feed when tapping the Home tab");
        DESCS.put(Prefs.HIDE_FRIEND_SUGGEST, "Skip friend-recommendation cards");
        DESCS.put(Prefs.HIDE_REWARDS_ADS, "Drop rewarded / incentive ad posts");
        DESCS.put(Prefs.HIDE_COMMISSION, "Drop affiliate / product commission posts");
        DESCS.put(Prefs.HIDE_LOCATION_ADS, "Drop local business / place ads");
        DESCS.put(Prefs.HIDE_AI_POSTS, "Drop AI-generated (AIGC) posts");
        DESCS.put(Prefs.FORCE_PROGRESS_BAR, "Keep the seek bar always visible");
        DESCS.put(Prefs.SHOW_FYP_TIMESTAMP, "Show the post's upload date on the title");
        DESCS.put(Prefs.SHOW_POST_REGION, "Show the post's country flag on the title");
        DESCS.put(Prefs.PROFILE_PIC_SAVE, "Long-press an avatar to save it");
        DESCS.put(Prefs.COPY_BIO, "Long-press profile text to copy it");
        DESCS.put(Prefs.ANONYMOUS_PROFILE_VIEW, "View profiles without being seen (exp.)");
        DESCS.put(Prefs.REMOVE_WATERMARK, "Save videos without the watermark");
        DESCS.put(Prefs.SHOW_DL_BUTTON, "Floating ⤓ button to download the current post");
        DESCS.put(Prefs.STICKER_DOWNLOAD, "Long-press a comment sticker to save it");
        DESCS.put(Prefs.ALLOW_ALL_DOWNLOADS, "Bypass download restrictions (also the ⤓ button)");
        DESCS.put(Prefs.HIDE_PLUS_BUTTON, "Hide the create (+) tab button");
        DESCS.put(Prefs.HIDE_TAB_LABELS, "Hide text labels under tab icons");
        DESCS.put(Prefs.HIDE_FRIENDS_BADGE, "Hide the red count on the Friends tab");
        DESCS.put(Prefs.HIDE_INBOX_BADGE, "Hide the red count on the Inbox tab");
        DESCS.put(Prefs.HIDE_AI_ASSISTANT, "Hide the Tako AI button in the feed");
        DESCS.put(Prefs.SANITIZE_LINKS, "Strip tracking params from shared links");
        DESCS.put(Prefs.DEBUG_CLICKS, "Log tapped view ids for debugging");
    }

    private PrefCatalog() {}
}
