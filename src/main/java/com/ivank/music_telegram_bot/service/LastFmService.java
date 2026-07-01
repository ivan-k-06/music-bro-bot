package com.ivank.music_telegram_bot.service;

import com.ivank.music_telegram_bot.api.RecommendationResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;

@Service
public class LastFmService {
    private static final Logger log = LoggerFactory.getLogger(LastFmService.class);
    private static final RecommendationResponse EMPTY_RESPONSE =
            new RecommendationResponse(new RecommendationResponse.Tracks(List.of()));

    @Value("${lastfm.api.key}")
    private String apiKey;

    private final RestClient restClient = RestClient.create();

    public RecommendationResponse getRecommendationByGenre(String genre) {
        try {
            RecommendationResponse response = restClient.get()
                    .uri("http://ws.audioscrobbler.com/2.0/?method=tag.gettoptracks&tag={genre}&api_key={key}&format=json", genre, apiKey)
                    .retrieve()
                    .body(RecommendationResponse.class);

            return response != null ? response : EMPTY_RESPONSE;
        } catch (RestClientException e) {
            log.warn("Last.fm request failed for genre '{}': {}", genre, e.getMessage());
            return EMPTY_RESPONSE;
        }
    }
}
