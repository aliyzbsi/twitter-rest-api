package com.twitter.twitter_rest_api.mapper;

import com.twitter.twitter_rest_api.dto.TweetResponse;
import com.twitter.twitter_rest_api.entity.Tweet;
import com.twitter.twitter_rest_api.entity.TweetType;
import com.twitter.twitter_rest_api.entity.User;
import com.twitter.twitter_rest_api.repository.TweetLikeRepository;
import com.twitter.twitter_rest_api.repository.TweetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class TweetMapper {
    private final TweetLikeRepository tweetLikeRepository;
    private final TweetRepository tweetRepository;


    public TweetResponse toTweetResponse(Tweet tweet, User currentUser) {
        TweetResponse response = new TweetResponse();
        response.setId(tweet.getId());

        if (tweet.getTweetType() == TweetType.RETWEET) {
            mapRetweetResponse(response, tweet, currentUser);
        } else {
            mapNormalTweetResponse(response, tweet, currentUser);
        }

        return response;
    }
    private void mapRetweetResponse(TweetResponse response, Tweet tweet, User currentUser) {
        Tweet originalTweet = tweet.getParentTweet();

        // Tweet'i görüntüleyen kullanıcı (currentUser) tweet'i beğenmiş mi?
        response.setLiked(originalTweet.getLikes().stream()
                .anyMatch(like->like.getUser().getId().equals(currentUser.getId())));
        // Tweet'i görüntüleyen kullanıcı (currentUser) tweet'i retweet etmiş mi?
        Optional<Tweet> userRetweet = tweetRepository.findByUserAndParentTweetAndTweetType(
                currentUser, originalTweet, TweetType.RETWEET);
        response.setRetweeted(userRetweet.isPresent());
        userRetweet.ifPresent(rt -> response.setRetweetId(rt.getId()));

        // Retweet yapan kullanıcı bilgileri
        mapUserInfo(response, tweet.getUser());

        // Orijinal tweet bilgileri
        mapOriginalTweetInfo(response, originalTweet);

        response.setTweetType(TweetType.RETWEET);
        response.setParentTweetID(originalTweet.getId());
        response.setParentTweetUserId(originalTweet.getUser().getId());
        response.setRetweetedAt(tweet.getCreatedAt());
        response.setUserId(originalTweet.getUser().getId());
    }

    private void mapNormalTweetResponse(TweetResponse response, Tweet tweet, User currentUser) {
        response.setLiked(tweetLikeRepository.findByUserAndTweet(currentUser, tweet).isPresent());

        Optional<Tweet> userRetweet = tweetRepository.findByUserAndParentTweetAndTweetType(
                currentUser, tweet, TweetType.RETWEET);
        response.setRetweeted(userRetweet.isPresent());
        userRetweet.ifPresent(rt -> response.setRetweetId(rt.getId()));

        mapUserInfo(response, tweet.getUser());
        mapTweetInfo(response, tweet);
    }

    private void mapUserInfo(TweetResponse response, User user) {
        response.setUsername(user.getUsername());
        response.setUserFullName(user.getFullName());
        response.setUserProfileImage(user.getProfileImage());
        response.setUserId(user.getId());
    }
    private void mapOriginalTweetInfo(TweetResponse response, Tweet originalTweet) {
        response.setOriginalUsername(originalTweet.getUser().getUsername());
        response.setOriginalUserFullName(originalTweet.getUser().getFullName());
        response.setOriginalUserProfileImage(originalTweet.getUser().getProfileImage());
        mapTweetInfo(response, originalTweet);
    }
    private void mapTweetInfo(TweetResponse response, Tweet tweet) {
        response.setContent(tweet.getContent());
        response.setMediaUrl(tweet.getMediaUrl());
        response.setMediaType(tweet.getMediaType());
        response.setLikeCount(tweet.getLikeCount());
        response.setRetweetCount(tweet.getRetweetCount());
        response.setReplyCount(tweet.getReplyCount());
        response.setCreatedAt(tweet.getCreatedAt());
        response.setTweetType(tweet.getTweetType());

    }

}
