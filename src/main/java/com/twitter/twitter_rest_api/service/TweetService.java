package com.twitter.twitter_rest_api.service;

import com.twitter.twitter_rest_api.dto.*;
import com.twitter.twitter_rest_api.entity.Tweet;
import com.twitter.twitter_rest_api.entity.TweetType;
import com.twitter.twitter_rest_api.entity.User;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface TweetService {
    Page<TweetResponse> findAll(Pageable pageable, String userEmail);
    TweetResponse findById(Long id,String userEmail);

    TweetResponse update(Long id, Tweet tweet, String userEmail);
    TweetResponse delete(Long id, String userEmail);

    TweetResponse createTweet(@Valid TweetRequest tweetRequest, String username);

    TweetResponse replyToTweet(@Valid ReplyTweetRequest replyTweetRequest, String username);

    TweetResponse retweet(RetweetRequest retweetRequest, String username);

    TweetResponse quoteTweet(@Valid QuoteTweetRequest quoteTweetRequest, String username);

    Page<TweetResponse> findByUserId(Long userID,Pageable pageable);

    Page<TweetResponse> findRepliesByTweetId(Long tweetId, Pageable pageable, String username);

}
