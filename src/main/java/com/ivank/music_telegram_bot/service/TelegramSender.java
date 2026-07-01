package com.ivank.music_telegram_bot.service;

import com.ivank.music_telegram_bot.bot.Bot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Service
public class TelegramSender {
    private static final Logger log = LoggerFactory.getLogger(TelegramSender.class);

    private final Bot bot;

    public TelegramSender(@Lazy Bot bot) {
        this.bot = bot;
    }

    public void sendMessage(Long chatId, String text, ReplyKeyboard keyboard) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(text);

        if (keyboard != null) {
            message.setReplyMarkup(keyboard);
        }

        try {
            bot.execute(message);
        } catch (TelegramApiException e) {
            log.warn("Failed to send Telegram message: {}", e.getMessage());
        }
    }

    public void sendMessage(Long chatId, String text) {
        sendMessage(chatId, text, null);
    }

    public void editMessage(Long chatId, Integer messageId, String newText, InlineKeyboardMarkup newKeyboard) {
        EditMessageText edit = new EditMessageText();
        edit.setChatId(String.valueOf(chatId));
        edit.setMessageId(messageId);
        edit.setText(newText);
        edit.setReplyMarkup(newKeyboard);
        try {
            bot.execute(edit);
        } catch (TelegramApiException e) {
            log.warn("Failed to send Telegram message: {}", e.getMessage());
        }
    }
}
