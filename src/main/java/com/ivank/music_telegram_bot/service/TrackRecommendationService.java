package com.ivank.music_telegram_bot.service;

import com.ivank.music_telegram_bot.api.RecommendationResponse;
import com.ivank.music_telegram_bot.model.RecommendationHistory;
import com.ivank.music_telegram_bot.model.User;
import com.ivank.music_telegram_bot.repository.RecommendationHistoryRepository;
import com.ivank.music_telegram_bot.repository.UserRepository;
import com.ivank.music_telegram_bot.ui.KeyboardFactory;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

@AllArgsConstructor
@Service
public class TrackRecommendationService {
    private final UserRepository userRepository;
    private final LastFmService lastFmService;
    private final RecommendationHistoryRepository recommendationHistoryRepository;
    private final TelegramSender telegramSender;
    private final LocalizationService localizationService;
    private final KeyboardFactory keyboardFactory;

    @Transactional
    public void getRecommendation(Long chatId, Integer messageId, String langCode) {
        User user = userRepository.findById(chatId).orElseThrow();

        if (user.getGenres() == null || user.getGenres().isEmpty()) {
            String text = localizationService.getMessage("genre.error.empty", langCode);
            telegramSender.editMessage(chatId, messageId, text, keyboardFactory.getGenresKeyboard(langCode, user.getGenres()));
            return;
        }

        Optional<RecommendationResponse.Track> track = pickFreshTrack(user);
        if (track.isEmpty()) {
            String text = localizationService.getMessage("recommendation.none_available", langCode);
            telegramSender.editMessage(chatId, messageId, text, keyboardFactory.getTrackCarouselKeyboard(langCode));
            return;
        }

        String text = localizationService.getMessage("recommendation.general", langCode) + "\n" + formatTrack(track.get());
        telegramSender.editMessage(chatId, messageId, text, keyboardFactory.getTrackCarouselKeyboard(langCode));
    }

    @Transactional
    public String userRecommendationsHistory(Long userId, String langCode) {
        var history = recommendationHistoryRepository.findTop14ByUserIdOrderBySendAtDesc(userId);

        if (history == null || history.isEmpty()) {
            return localizationService.getMessage("history.empty", langCode);
        }

        StringBuilder result = new StringBuilder(localizationService.getMessage("history.title", langCode));
        for (int i = 0; i < history.size(); i++) {
            RecommendationHistory item = history.get(i);
            String label = (item.getTrackName() != null && !item.getTrackName().isBlank())
                    ? item.getTrackName()
                    : item.getTrackUrl();
            result.append("\n").append(i + 1).append(". ").append(label);
        }

        return result.toString();
    }

    @Transactional
    public void clearUserHistory(Long userId) {
        recommendationHistoryRepository.deleteByUserId(userId);
    }

    /**
     * Picks a genre the user follows and returns the first track from it that hasn't
     * been recommended to them before, recording it in the history. Used both for the
     * on-demand "get track" flow and the scheduled daily notifications.
     */
    @Transactional
    public Optional<RecommendationResponse.Track> pickFreshTrack(User user) {
        List<String> genres = new ArrayList<>(user.getGenres());
        if (genres.isEmpty()) {
            return Optional.empty();
        }

        String randomGenre = genres.get(ThreadLocalRandom.current().nextInt(genres.size()));
        List<RecommendationResponse.Track> tracks = extractTracks(lastFmService.getRecommendationByGenre(randomGenre));

        for (RecommendationResponse.Track track : tracks) {
            if (!recommendationHistoryRepository.existsByUserIdAndTrackUrl(user.getUserId(), track.url())) {
                saveToHistory(user.getUserId(), track);
                return Optional.of(track);
            }
        }

        return Optional.empty();
    }

    public String formatTrack(RecommendationResponse.Track track) {
        return track.trackName() + " - " + track.artist().artistName() + "\n" + track.url();
    }

    private List<RecommendationResponse.Track> extractTracks(RecommendationResponse response) {
        if (response == null || response.tracks() == null || response.tracks().trackList() == null) {
            return List.of();
        }
        return response.tracks().trackList();
    }

    private void saveToHistory(Long userId, RecommendationResponse.Track track) {
        RecommendationHistory history = new RecommendationHistory();
        history.setUserId(userId);
        history.setTrackUrl(track.url());
        history.setTrackName(track.trackName() + " — " + track.artist().artistName());
        history.setSendAt(ZonedDateTime.now());
        recommendationHistoryRepository.save(history);
    }
}
