package com.ivank.music_telegram_bot.ui;

import com.ivank.music_telegram_bot.service.LocalizationService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.List;
import java.util.Set;

@Component
@AllArgsConstructor
public class KeyboardFactory {

    private final LocalizationService localizationService;

    public InlineKeyboardMarkup getSettingsKeyboard(String langCode) {
        return markup(List.of(
                List.of(
                        button(msg("settings.set_time", langCode), "btn_change_time"),
                        button(msg("settings.set_offset", langCode), "btn_change_offset")
                ),
                List.of(backButton(langCode))
        ));
    }

    public InlineKeyboardMarkup getGenresKeyboard(String langCode, Set<String> userGenres) {
        Set<String> genres = userGenres != null ? userGenres : Set.of();

        return markup(List.of(
                List.of(
                        button(genreLabel(genres, "rock", msg("genre.rock", langCode)), "rock"),
                        button(genreLabel(genres, "ambient", msg("genre.ambient", langCode)), "ambient")
                ),
                List.of(
                        button(genreLabel(genres, "country", msg("genre.country", langCode)), "country"),
                        button(genreLabel(genres, "jazz", msg("genre.jazz", langCode)), "jazz")
                ),
                List.of(button(msg("button.back", langCode), "btn_back"))
        ));
    }

    public InlineKeyboardMarkup getSettingsBackKeyboard(String langCode) {
        return markup(List.of(List.of(button(msg("settings.back", langCode), "btn_settings_back"))));
    }

    public InlineKeyboardMarkup getBackKeyboard(String langCode) {
        return markup(List.of(List.of(backButton(langCode))));
    }

    public InlineKeyboardMarkup getMainMenuKeyboard(String langCode) {
        return markup(List.of(
                List.of(
                        button(msg("button.get_track", langCode), "btn_get_track"),
                        button(msg("button.genres", langCode), "btn_genres")
                ),
                List.of(button(msg("button.history", langCode), "btn_history")),
                List.of(button(msg("button.settings", langCode), "btn_settings"))
        ));
    }

    public InlineKeyboardMarkup getHistoryKeyboard(String langCode) {
        return markup(List.of(List.of(
                backButton(langCode),
                button(msg("history.clear", langCode), "btn_clear_history")
        )));
    }

    public InlineKeyboardMarkup getTrackCarouselKeyboard(String langCode) {
        return markup(List.of(List.of(
                backButton(langCode),
                button(msg("button.next_track", langCode), "btn_next_track")
        )));
    }

    private String msg(String key, String langCode) {
        return localizationService.getMessage(key, langCode);
    }

    private String genreLabel(Set<String> userGenres, String genreKey, String label) {
        return userGenres.contains(genreKey) ? "✅ " + label : label;
    }

    private InlineKeyboardButton backButton(String langCode) {
        return button(msg("button.back", langCode), "btn_back");
    }

    private InlineKeyboardButton button(String text, String callbackData) {
        InlineKeyboardButton button = new InlineKeyboardButton(text);
        button.setCallbackData(callbackData);
        return button;
    }

    private InlineKeyboardMarkup markup(List<List<InlineKeyboardButton>> rows) {
        InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();
        keyboard.setKeyboard(rows);
        return keyboard;
    }
}
