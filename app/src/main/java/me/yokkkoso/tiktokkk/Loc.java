package me.yokkkoso.tiktokkk;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public final class Loc {

    private static final Map<String, String> M = new HashMap<>();

    static {
        M.put("Confirmations", "Подтверждения");
        M.put("Feed", "Лента");
        M.put("Profile", "Профиль");
        M.put("Downloads", "Загрузки");
        M.put("Interface", "Интерфейс");
        M.put("Tab Bar", "Панель вкладок");
        M.put("Hide tab bar labels", "Скрыть подписи вкладок");
        M.put("Hide Friends badge", "Скрыть значок «Друзья»");
        M.put("Hide Inbox badge", "Скрыть значок «Входящие»");
        M.put("Hide text labels under tab icons", "Скрыть подписи под иконками вкладок");
        M.put("Hide the red count on the Friends tab", "Скрыть красный счётчик на «Друзья»");
        M.put("Hide the red count on the Inbox tab", "Скрыть красный счётчик на «Входящие»");
        M.put("Advanced", "Дополнительно");
        M.put("Region", "Регион");
        M.put("Language", "Язык");
        M.put("Misc", "Разное");
        M.put("Accent color", "Цвет акцента");
        M.put("Download button opacity", "Прозрачность кнопки загрузки");
        M.put("Export settings", "Экспорт настроек");
        M.put("Import settings", "Импорт настроек");
        M.put("Reset settings", "Сброс настроек");
        M.put("Copy", "Копировать");
        M.put("Copied", "Скопировано");
        M.put("Paste config here", "Вставьте конфиг сюда");
        M.put("Imported — restart to apply", "Импортировано — перезапустите");
        M.put("Nothing to import", "Нечего импортировать");
        M.put("Reset all settings to defaults?", "Сбросить все настройки?");
        M.put("Settings reset", "Настройки сброшены");
        M.put("Pink", "Розовый");
        M.put("Cyan", "Бирюзовый");
        M.put("Purple", "Фиолетовый");
        M.put("Green", "Зелёный");
        M.put("Orange", "Оранжевый");
        M.put("Red", "Красный");
        M.put("Yellow", "Жёлтый");
        M.put("White", "Белый");

        M.put("Like / follow / comment prompts", "Лайк / подписка / коммент");
        M.put("Ads, live, stories, progress bar", "Реклама, эфиры, истории, прогресс");
        M.put("Post stats, anonymous view", "Статистика, аноним. просмотр");
        M.put("Watermark, quality", "Вотермарк, качество");
        M.put("Plus button, AI, links", "Плюс, ИИ, ссылки");
        M.put("Spoof country / SIM", "Подмена страны / SIM");
        M.put("Debug options", "Отладка");

        M.put("Content & Display", "Контент и отображение");
        M.put("Filtering", "Фильтрация");
        M.put("Hide ads", "Скрыть рекламу");
        M.put("Disable live streaming (in feed)", "Отключить эфиры (в ленте)");
        M.put("Hide Shop posts", "Скрыть посты магазина");
        M.put("Disable Visual Search tag", "Отключить тег визуального поиска");
        M.put("Skip friend recommendations", "Пропускать рекомендации друзей");
        M.put("Show upload date", "Дата загрузки");
        M.put("Show upload region", "Регион загрузки");
        M.put("Remove TikTok AI button", "Убрать кнопку ИИ");
        M.put("Disable TikTok Rewards ads", "Отключить рекламу Rewards");
        M.put("Hide commission posts", "Скрыть партнёрские посты");
        M.put("Hide location ads", "Скрыть рекламу мест");
        M.put("Hide AI-generated posts", "Скрыть посты с ИИ");
        M.put("Drop rewarded / incentive ad posts", "Убирать поощрительную рекламу");
        M.put("Drop affiliate / product commission posts", "Убирать партнёрские посты с товарами");
        M.put("Drop local business / place ads", "Убирать рекламу заведений и мест");
        M.put("Drop AI-generated (AIGC) posts", "Убирать посты, созданные ИИ");

        M.put("Ask before liking a video", "Спрашивать перед лайком видео");
        M.put("Ask before following an account", "Спрашивать перед подпиской");
        M.put("Ask before liking a comment", "Спрашивать перед лайком коммента");
        M.put("Remove sponsored posts from the feed", "Убирать рекламные посты из ленты");
        M.put("Skip live streams in the feed", "Пропускать эфиры в ленте");
        M.put("Skip photo slideshow posts", "Пропускать фото-слайдшоу");
        M.put("Skip TikTok Shop product posts", "Пропускать посты магазина");
        M.put("Skip stories in the feed", "Пропускать истории");
        M.put("Hide the visual-search tag on posts", "Скрывать тег визуального поиска");
        M.put("Skip friend-recommendation cards", "Пропускать карточки друзей");
        M.put("Keep the seek bar always visible", "Всегда показывать полосу прогресса");
        M.put("Show the post's upload date on the title", "Показывать дату загрузки поста");
        M.put("Show the post's country flag on the title", "Показывать флаг страны поста");
        M.put("Overlay likes and date on profile posts", "Лайки и дата на постах профиля");
        M.put("View profiles without being seen (exp.)", "Смотреть профили незаметно (эксп.)");
        M.put("Save videos without the watermark", "Сохранять видео без вотермарка");
        M.put("Ask for quality when saving a video", "Спрашивать качество при сохранении");
        M.put("Prefer highest quality when chooser is off", "Максимальное качество без выбора");
        M.put("Hide the create (+) tab button", "Скрывать кнопку создания (+)");
        M.put("Hide the Tako AI button in the feed", "Скрывать кнопку ИИ Tako");
        M.put("Strip tracking params from shared links", "Убирать трекинг из ссылок");
        M.put("Log tapped view ids for debugging", "Логировать нажатия для отладки");

        M.put("Confirm before like", "Подтверждать лайк");
        M.put("Confirm before follow", "Подтверждать подписку");
        M.put("Confirm before comment like", "Подтверждать лайк коммента");
        M.put("Remove feed ads", "Убрать рекламу в ленте");
        M.put("Hide live streams", "Скрыть эфиры");
        M.put("Hide photo slideshows", "Скрыть фото-слайдшоу");
        M.put("Hide TikTok Shop videos", "Скрыть видео TikTok Shop");
        M.put("Hide stories", "Скрыть истории");
        M.put("Hide 'Find similar' on pause", "Скрыть «Похожее» на паузе");
        M.put("Hide friend suggestions in FYP", "Скрыть рекомендации друзей");
        M.put("Always show progress bar", "Всегда показывать прогресс-бар");
        M.put("Show upload date in FYP", "Дата загрузки в ленте");
        M.put("Show date on profile posts", "Дата на постах профиля");
        M.put("Anonymous profile view (exp.)", "Анонимный просмотр профиля (эксп.)");
        M.put("Download without watermark", "Скачивать без вотермарка");
        M.put("Ask quality on Save (menu)", "Спрашивать качество при сохранении");
        M.put("Default HQ (when chooser off)", "По умолчанию HQ");
        M.put("Download in HQ (original)", "Скачивать в HQ (оригинал)");
        M.put("Show download button", "Показывать кнопку загрузки");
        M.put("Floating ⤓ button to download the current post", "Плавающая кнопка ⤓ для загрузки поста");
        M.put("Save comment stickers", "Сохранять стикеры комментариев");
        M.put("Long-press a comment sticker to save it", "Удержание на стикере — сохранить");
        M.put("Allow all downloads", "Разрешить все загрузки");
        M.put("Save the original / highest-resolution video", "Сохранять оригинал / макс. качество");
        M.put("Bypass download restrictions (also the ⤓ button)", "Обходить запрет на скачивание (и кнопка ⤓)");
        M.put("Downloads unlocked", "Загрузки разблокированы");
        M.put("Downloads locked", "Загрузки заблокированы");
        M.put("Hide Plus button", "Скрыть кнопку «Плюс»");
        M.put("Hide AI assistant (Tako) in FYP", "Скрыть ИИ-ассистента (Tako)");
        M.put("Sanitize shared links", "Очищать ссылки при отправке");
        M.put("Debug: log click targets", "Отладка: логировать нажатия");

        M.put("TikTok tweaks", "Твики для TikTok");
        M.put("Dump screen", "Снять экран");
        M.put("View logs", "Логи");
        M.put("Restart", "Перезапуск");
        M.put("Close", "Закрыть");
        M.put("Region:", "Регион:");
        M.put("Off (real region)", "Выкл (реальный регион)");
        M.put("Off (use real region)", "Выкл (реальный регион)");
        M.put("Select region", "Выбор региона");
        M.put("Applies after restart", "Применяется после перезапуска");
        M.put("— banned", "— заблокирован");
        M.put("Download quality", "Качество загрузки");
        M.put("Photo", "Фото");
        M.put("Download photo", "Скачать фото");
        M.put("All", "Все");
        M.put("No video", "Нет видео");
        M.put("No download URL", "Нет ссылки для загрузки");
        M.put("Downloading…", "Скачивание…");
        M.put("Resolving HQ…", "Получаю HQ…");
        M.put("Downloading HQ…", "Загрузка HQ…");
        M.put("HQ unavailable - pick a quality", "HQ недоступно - выберите качество");
        M.put("Saved to gallery", "Сохранено в галерею");
        M.put("Original", "Оригинал");
        M.put("Standard", "Стандарт");
        M.put("Standard (No watermark)", "Стандарт (без вотермарка)");
        M.put("HQ (largest)", "HQ (макс.)");
        M.put("No watermark", "Без вотермарка");
        M.put("Follow this account?", "Подписаться на аккаунт?");
        M.put("Like this video?", "Лайкнуть видео?");
        M.put("Like this comment?", "Лайкнуть комментарий?");
        M.put("Remove like from this video?", "Убрать лайк с видео?");
        M.put("Confirm before unlike", "Подтверждать снятие лайка");
        M.put("Ask before removing a like", "Спрашивать перед снятием лайка");
        M.put("Save profile picture", "Сохранять фото профиля");
        M.put("Copy bio", "Копировать описание");
        M.put("Long-press an avatar to save it", "Удержание на аватаре — сохранить");
        M.put("Long-press profile text to copy it", "Удержание на тексте профиля — копировать");
        M.put("Picture saved", "Фото сохранено");
        M.put("Save failed", "Не удалось сохранить");
        M.put("No image", "Нет изображения");
        M.put("Dislike this comment?", "Дизлайк комментарию?");
        M.put("Like this story?", "Лайкнуть историю?");
        M.put("Repost this video?", "Репостнуть видео?");
        M.put("Share this video?", "Поделиться видео?");
        M.put("Confirm before comment dislike", "Подтверждать дизлайк коммента");
        M.put("Confirm before story like", "Подтверждать лайк истории");
        M.put("Confirm before quick share", "Подтверждать быстрый шэр");
        M.put("Confirm before quick repost", "Подтверждать быстрый репост");
        M.put("Ask before disliking a comment", "Спрашивать перед дизлайком коммента");
        M.put("Ask before liking a story", "Спрашивать перед лайком истории");
        M.put("Ask before quick-sharing a video", "Спрашивать перед быстрым шэром");
        M.put("Ask before reposting a video", "Спрашивать перед репостом");
        M.put("Confirm before favorite", "Подтверждать добавление в избранное");
        M.put("Ask before adding a video to favorites", "Спрашивать перед добавлением в избранное");
        M.put("Add to favorites?", "Добавить в избранное?");
        M.put("Logs", "Логи");
        M.put("Share", "Поделиться");
        M.put("Clear", "Очистить");
        M.put("(empty — enable 'Debug: log click targets', then use the app)",
                "(пусто — включите «Отладка: логировать нажатия» и используйте приложение)");
        M.put("Dumped. Wait 1s, reopen menu -> View logs -> Share",
                "Снято. Подождите 1с, откройте меню → Логи → Поделиться");
    }

    static String lang() {
        String v = Prefs.getString(Prefs.LOCALE, "");
        if (v == null || v.isEmpty()) {
            v = "ru".equals(Locale.getDefault().getLanguage()) ? "ru" : "en";
            Prefs.setString(Prefs.LOCALE, v);
        }
        return v;
    }

    public static boolean isRu() {
        return "ru".equals(lang());
    }

    public static String t(String en) {
        if (en == null || !isRu()) return en;
        String ru = M.get(en);
        return ru != null ? ru : en;
    }

    private Loc() {}
}
