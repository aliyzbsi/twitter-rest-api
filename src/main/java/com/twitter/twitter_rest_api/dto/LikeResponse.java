package com.twitter.twitter_rest_api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "Tweet beğeni için bilgiler")
public record LikeResponse( boolean isLiked,
         Long originalTweetId,
         Long retweetId,
         Integer likeCount) {

}
