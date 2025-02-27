package com.twitter.twitter_rest_api.service;

import com.twitter.twitter_rest_api.dto.*;
import com.twitter.twitter_rest_api.entity.Tweet;
import com.twitter.twitter_rest_api.entity.TweetType;
import com.twitter.twitter_rest_api.entity.User;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

public interface TweetService {
    Page<TweetResponse> findAll(Pageable pageable, String userEmail);
    TweetDetailResponse findById(Long id, String userEmail);
    TweetDetailResponse update(Long id, Tweet tweet, String userEmail);
    TweetDetailResponse delete(Long id, String userEmail);
    TweetDetailResponse createTweet(String content, MultipartFile media, String username);
    TweetDetailResponse replyToTweet(Long tweetId, String content, MultipartFile media, String username);
    TweetDetailResponse retweet(RetweetRequest retweetRequest, String username);
    TweetDetailResponse quoteTweet(Long tweetId,String content, MultipartFile media, String username);
    Page<TweetResponse> findByUserId(Long userID,Pageable pageable);

    Page<TweetResponse> findRepliesByTweetId(Long tweetId, Pageable pageable, String username);

}
