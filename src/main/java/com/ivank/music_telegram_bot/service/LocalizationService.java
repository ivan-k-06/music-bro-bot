package com.ivank.music_telegram_bot.service;

import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
public class LocalizationService {

    private final MessageSource messageSource;

    public LocalizationService(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    public String getMessage(String key, String langCode, Object... args) {
        Locale locale = (langCode != null && !langCode.isEmpty())
                ? Locale.forLanguageTag(langCode)
                : Locale.forLanguageTag("ru");

        return messageSource.getMessage(key, args, locale);
    }
}