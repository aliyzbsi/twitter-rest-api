package com.twitter.twitter_rest_api.service;

import com.twitter.twitter_rest_api.dto.LikeResponse;
import com.twitter.twitter_rest_api.dto.TweetResponse;
import com.twitter.twitter_rest_api.entity.Tweet;
import com.twitter.twitter_rest_api.entity.User;

import java.util.List;

public interface LikeService {

    // Bir gönderiyi beğen yada beğenilmişse geri çek
    TweetResponse toggleLike(Long tweetId, String userEmail);
    // Bir kullanıcının beğendiği tweetleri getir
    List<Tweet> getLikedTweets(Long userId);
    // Bir tweeti beğenen kullanıcıları getir
    List<User> getLikedByUsers(Long tweetId);
}
