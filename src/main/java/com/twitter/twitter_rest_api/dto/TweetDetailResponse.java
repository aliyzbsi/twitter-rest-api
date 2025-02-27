package com.twitter.twitter_rest_api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Schema(description = "Detaylı tweet yanıtı")
public class TweetDetailResponse extends TweetResponse {
    @Schema(description = "Varsa, yanıt verilen/alıntılanan/retweet edilen orijinal tweet")
    private TweetResponse parentTweet;

    @Schema(description = "Tweet zincirindeki önceki tweet'ler")
    private List<TweetResponse> conversationThread;

    @Schema(description = "Tweet'in bir konuşma zincirinin parçası olup olmadığı")
    private boolean isPartOfThread;
}