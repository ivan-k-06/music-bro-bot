package com.ivank.music_telegram_bot.service;

import com.ivank.music_telegram_bot.model.User;
import com.ivank.music_telegram_bot.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.HashSet;
import java.util.Set;

@Service
@AllArgsConstructor
public class UserService {
    private static final String DEFAULT_LANGUAGE = "ru";

    private final UserRepository userRepository;

    @Transactional
    public User getOrCreateUser(Long userId, String langCode) {
        return userRepository.findById(userId).orElseGet(() -> {
            User newUser = new User();
            newUser.setUserId(userId);
            newUser.setNotificationTime(LocalTime.of(10, 0));
            newUser.setNotificationTimeUtc(LocalTime.of(10, 0));
            newUser.setTimezoneOffset(0);
            newUser.setLanguageCode(normalizeLangCode(langCode));
            newUser.setGenres(new HashSet<>());
            return userRepository.save(newUser);
        });
    }

    @Transactional
    public User getOrCreateUser(Long userId) {
        return getOrCreateUser(userId, DEFAULT_LANGUAGE);
    }

    @Transactional
    public void updateUserLanguage(Long userId, String langCode) {
        String normalized = normalizeLangCode(langCode);
        userRepository.findById(userId).ifPresent(user -> {
            if (!normalized.equals(user.getLanguageCode())) {
                user.setLanguageCode(normalized);
                userRepository.save(user);
            }
        });
    }

    @Transactional
    public boolean toggleGenre(Long userId, String genre) {
        User user = getOrCreateUser(userId);

        Set<String> genres = user.getGenres();
        if (genres == null) {
            genres = new HashSet<>();
        }

        boolean isAdded = !genres.remove(genre);
        if (isAdded) {
            genres.add(genre);
        }

        user.setGenres(genres);
        userRepository.save(user);

        return isAdded;
    }

    @Transactional
    public void updateUserTime(Long userId, LocalTime newTime) {
        User user = getOrCreateUser(userId);

        user.setNotificationTime(newTime);
        user.setNotificationTimeUtc(newTime.minusHours(user.getTimezoneOffset()));

        userRepository.save(user);
    }

    @Transactional
    public void updateUserTimezone(Long userId, int offset) {
        User user = getOrCreateUser(userId);

        user.setTimezoneOffset(offset);
        user.setNotificationTimeUtc(user.getNotificationTime().minusHours(offset));

        userRepository.save(user);
    }

    private String normalizeLangCode(String langCode) {
        return (langCode == null || langCode.isBlank()) ? DEFAULT_LANGUAGE : langCode;
    }
}
