package com.ivank.music_telegram_bot.bot;

import com.ivank.music_telegram_bot.service.LocalizationService;
import com.ivank.music_telegram_bot.service.TelegramSender;
import com.ivank.music_telegram_bot.service.TrackRecommendationService;
import com.ivank.music_telegram_bot.service.UserService;
import com.ivank.music_telegram_bot.ui.KeyboardFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.time.LocalTime;

@Component
public class Bot extends TelegramLongPollingBot {
    @Value("${telegram.bot.name}")
    private String botUsername;
    private final TrackRecommendationService trackRecommendationService;
    private final KeyboardFactory keyboardFactory;
    private final TelegramSender telegramSender;
    private final UserService userService;
    private final LocalizationService localizationService;

    public Bot(@Value("${telegram.bot.token}") String botToken,
               TrackRecommendationService trackRecommendationService,
               TelegramSender telegramSender,
               UserService userService,
               KeyboardFactory keyboardFactory,
               LocalizationService localizationService) {
        super(botToken);
        this.trackRecommendationService = trackRecommendationService;
        this.telegramSender = telegramSender;
        this.userService = userService;
        this.keyboardFactory = keyboardFactory;
        this.localizationService = localizationService;
    }

    public void onUpdateReceived(Update update) {
        String langCode = extractLanguageCode(update);
        Long chatIdForLangUpdate = extractChatId(update);
        if (chatIdForLangUpdate != null) {
            userService.updateUserLanguage(chatIdForLangUpdate, langCode);
        }

        if (update.hasMessage() && update.getMessage().hasText()) {
            handleTextMessage(update, langCode);
            return;
        }

        if (update.hasCallbackQuery()) {
            handleCallbackQuery(update, langCode);
        }
    }

    private void handleTextMessage(Update update, String langCode) {
        String messageText = update.getMessage().getText();
        long chatId = update.getMessage().getChatId();

        if (messageText.equals("/start")) {
            var user = userService.getOrCreateUser(chatId, langCode);
            if (user.getGenres() != null && !user.getGenres().isEmpty()) {
                telegramSender.sendMessage(chatId, localizationService.getMessage("menu.welcome_back", langCode));
                telegramSender.sendMessage(chatId, localizationService.getMessage("menu.main", langCode), keyboardFactory.getMainMenuKeyboard(langCode));
            } else {
                telegramSender.sendMessage(chatId, localizationService.getMessage("onboarding.step1", langCode));
            }
            return;
        }

        if (messageText.startsWith("/tz")) {
            handleTimezoneCommand(chatId, messageText, langCode);
            return;
        }

        if (messageText.startsWith("/update_time")) {
            handleUpdateTimeCommand(chatId, messageText, langCode);
        }
    }

    private void handleTimezoneCommand(long chatId, String messageText, String langCode) {
        try {
            int offset = Integer.parseInt(messageText.replace("/tz", "").trim());

            if (offset < -12 || offset > 14) {
                telegramSender.sendMessage(chatId, localizationService.getMessage("time.tz.error", langCode));
                return;
            }

            userService.updateUserTimezone(chatId, offset);

            String offsetStr = (offset > 0 ? "+" : "") + offset;
            telegramSender.sendMessage(chatId, localizationService.getMessage("onboarding.step2", langCode, offsetStr));
        } catch (NumberFormatException e) {
            telegramSender.sendMessage(chatId, localizationService.getMessage("time.tz.error", langCode));
        }
    }

    private void handleUpdateTimeCommand(long chatId, String messageText, String langCode) {
        try {
            LocalTime parsedTime = LocalTime.parse(messageText.replace("/update_time", "").trim());
            userService.updateUserTime(chatId, parsedTime);

            var user = userService.getOrCreateUser(chatId, langCode);
            String text = localizationService.getMessage("onboarding.step3", langCode, parsedTime.toString());
            telegramSender.sendMessage(chatId, text, keyboardFactory.getGenresKeyboard(langCode, user.getGenres()));
        } catch (Exception e) {
            telegramSender.sendMessage(chatId, localizationService.getMessage("time.format.error", langCode));
        }
    }

    private void handleCallbackQuery(Update update, String langCode) {
        long chatId = update.getCallbackQuery().getMessage().getChatId();
        Integer messageId = update.getCallbackQuery().getMessage().getMessageId();
        String callbackData = update.getCallbackQuery().getData();

        switch (callbackData) {
            case "btn_settings":
                telegramSender.editMessage(chatId, messageId, localizationService.getMessage("settings.text", langCode), keyboardFactory.getSettingsKeyboard(langCode));
                break;

            case "btn_history":
                String historyText = trackRecommendationService.userRecommendationsHistory(chatId, langCode);
                telegramSender.editMessage(chatId, messageId, historyText, keyboardFactory.getHistoryKeyboard(langCode));
                break;

            case "btn_clear_history":
                trackRecommendationService.clearUserHistory(chatId);
                String clearedText = localizationService.getMessage("history.cleared", langCode);
                telegramSender.editMessage(chatId, messageId, clearedText, keyboardFactory.getHistoryKeyboard(langCode));
                break;

            case "btn_genres": {
                var user = userService.getOrCreateUser(chatId, langCode);
                String genresText = localizationService.getMessage("menu.genres.prompt", langCode);
                telegramSender.editMessage(chatId, messageId, genresText, keyboardFactory.getGenresKeyboard(langCode, user.getGenres()));
                break;
            }

            case "btn_back":
                String mainText = localizationService.getMessage("menu.main", langCode);
                telegramSender.editMessage(chatId, messageId, mainText, keyboardFactory.getMainMenuKeyboard(langCode));
                break;

            case "btn_get_track":
            case "btn_next_track":
                trackRecommendationService.getRecommendation(chatId, messageId, langCode);
                break;

            case "btn_change_time":
                String timePrompt = localizationService.getMessage("time.update.prompt", langCode);
                telegramSender.editMessage(chatId, messageId, timePrompt, keyboardFactory.getSettingsBackKeyboard(langCode));
                break;

            case "btn_change_offset":
                String tzPrompt = localizationService.getMessage("time.tz.prompt", langCode);
                telegramSender.editMessage(chatId, messageId, tzPrompt, keyboardFactory.getSettingsBackKeyboard(langCode));
                break;

            case "btn_settings_back":
                String backText = localizationService.getMessage("settings.text", langCode);
                telegramSender.editMessage(chatId, messageId, backText, keyboardFactory.getSettingsKeyboard(langCode));
                break;

            default:
                handleGenreToggle(chatId, messageId, callbackData, langCode);
                break;
        }
    }

    private void handleGenreToggle(long chatId, Integer messageId, String genre, String langCode) {
        boolean isAdded = userService.toggleGenre(chatId, genre);
        var user = userService.getOrCreateUser(chatId, langCode);

        if (isAdded && user.getGenres().size() == 1) {
            String finishText = localizationService.getMessage("onboarding.finish", langCode) + "\n\n" + localizationService.getMessage("menu.main", langCode);
            telegramSender.editMessage(chatId, messageId, finishText, keyboardFactory.getMainMenuKeyboard(langCode));
        } else {
            String replyText = isAdded
                    ? localizationService.getMessage("genre.add.success", langCode, genre)
                    : localizationService.getMessage("genre.add.duplicate", langCode);
            telegramSender.editMessage(chatId, messageId, replyText, keyboardFactory.getGenresKeyboard(langCode, user.getGenres()));
        }
    }

    private String extractLanguageCode(Update update) {
        if (update.hasMessage() && update.getMessage().getFrom() != null) {
            return update.getMessage().getFrom().getLanguageCode();
        }
        if (update.hasCallbackQuery() && update.getCallbackQuery().getFrom() != null) {
            return update.getCallbackQuery().getFrom().getLanguageCode();
        }
        return "ru";
    }

    private Long extractChatId(Update update) {
        if (update.hasMessage()) {
            return update.getMessage().getChatId();
        }
        if (update.hasCallbackQuery()) {
            return update.getCallbackQuery().getMessage().getChatId();
        }
        return null;
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }
}