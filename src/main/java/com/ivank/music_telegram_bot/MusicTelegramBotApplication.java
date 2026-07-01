package com.ivank.music_telegram_bot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
@SpringBootApplication
public class MusicTelegramBotApplication {
	public static void main(String[] args) {
		SpringApplication.run(MusicTelegramBotApplication.class, args);
	}
}
