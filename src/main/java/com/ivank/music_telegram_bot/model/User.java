package com.ivank.music_telegram_bot.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalTime;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "users")
public class User {
    @Id
    private long userId;

    @Column(name = "notification_time", nullable = false)
    private LocalTime notificationTime;

    @Column(name = "notification_time_utc", nullable = false)
    private LocalTime notificationTimeUtc;

    @Column(name = "timezone_offset", nullable = false)
    @ColumnDefault("0")
    private int timezoneOffset;

    @Column(name = "language_code")
    private String languageCode;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name="user_genres", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "genre")
    public Set<String> genres;
}
