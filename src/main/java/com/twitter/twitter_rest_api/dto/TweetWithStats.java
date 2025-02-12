package com.twitter.twitter_rest_api.dto;

public interface TweetWithStats {
    Long getId();
    String getContent();
    Long getLikeCount();
    Long getRetweetCount();
    Long getReplyCount();
}
