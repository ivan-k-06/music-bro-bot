package com.ivank.music_telegram_bot.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record RecommendationResponse(
        @JsonProperty("tracks") Tracks tracks
) {
    public record Tracks(
            @JsonProperty("track") List<Track> trackList
    ) {}

    public record Track(
            @JsonProperty("name") String trackName,

            @JsonProperty("url") String url,

            @JsonProperty("artist") Artist artist
    ) {}

    public record Artist(
            @JsonProperty("name") String artistName
    ) {}
}