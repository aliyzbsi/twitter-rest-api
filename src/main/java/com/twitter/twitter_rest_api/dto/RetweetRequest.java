package com.twitter.twitter_rest_api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "Retweet isteği")
public class RetweetRequest {
    @Schema(description = "Retweet edilecek tweet ID'si", example = "1")
    @NotNull(message = "Retweet edilecek tweet ID'si gereklidir")
    private Long parentTweetId;
}
