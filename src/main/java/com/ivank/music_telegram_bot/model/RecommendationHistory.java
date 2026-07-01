package com.ivank.music_telegram_bot.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.ZonedDateTime;

@Getter
@Setter
@Entity
@Table(name = "recommendation_history")
public class RecommendationHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "track_url", nullable = false)
    private String trackUrl;

    @Column(name = "track_name")
    private String trackName;

    @Column(name = "send_at", nullable = false)
    private ZonedDateTime sendAt;

    @Column(name = "user_id", nullable = false)
    private Long userId;
}
