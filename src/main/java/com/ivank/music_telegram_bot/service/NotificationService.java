package com.ivank.music_telegram_bot.service;

import com.ivank.music_telegram_bot.model.User;
import com.ivank.music_telegram_bot.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.List;

@AllArgsConstructor
@Service
public class NotificationService {
    private final UserRepository userRepository;
    private final TrackRecommendationService trackRecommendationService;
    private final TelegramSender telegramSender;
    private final LocalizationService localizationService;

    @Transactional
    @Scheduled(cron = "0 * * * * *")
    public void sendTargetedNotifications() {
        LocalTime currentUtcTime = LocalTime.now(ZoneOffset.UTC).withSecond(0).withNano(0);
        List<User> usersToNotify = userRepository.findAllByNotificationTimeUtc(currentUtcTime);

        for (User user : usersToNotify) {
            if (user.getGenres() == null || user.getGenres().isEmpty()) {
                continue;
            }

            trackRecommendationService.pickFreshTrack(user).ifPresent(track -> {
                String text = localizationService.getMessage("recommendation.daily", user.getLanguageCode())
                        + "\n" + trackRecommendationService.formatTrack(track);
                telegramSender.sendMessage(user.getUserId(), text);
            });
        }
    }
}
