package com.ivank.music_telegram_bot.repository;

import com.ivank.music_telegram_bot.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalTime;
import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {
    List<User> findAllByNotificationTimeUtc(LocalTime timeUtc);
}
