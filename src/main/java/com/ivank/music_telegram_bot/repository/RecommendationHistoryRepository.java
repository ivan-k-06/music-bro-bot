package com.ivank.music_telegram_bot.repository;

import com.ivank.music_telegram_bot.model.RecommendationHistory;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface RecommendationHistoryRepository extends JpaRepository<RecommendationHistory, Long> {
    boolean existsByUserIdAndTrackUrl(Long userId, String trackUrl);
    List<RecommendationHistory> findTop14ByUserIdOrderBySendAtDesc(Long userId);
    @Transactional
    void deleteByUserId(Long userId);
}
