package com.twitter.twitter_rest_api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Detaylı tweet yanıtı")
public class TweetDetailResponse extends TweetResponse{
    @Schema(description = "Varsa, yanıt verilen/alıntılanan/retweet edilen orijinal tweet")
    private TweetResponse parentTweet;
}
