package com.twitter.twitter_rest_api.mapper;

import com.twitter.twitter_rest_api.dto.TweetDetailResponse;
import com.twitter.twitter_rest_api.dto.TweetResponse;
import com.twitter.twitter_rest_api.entity.Tweet;
import com.twitter.twitter_rest_api.entity.TweetType;
import com.twitter.twitter_rest_api.entity.User;
import com.twitter.twitter_rest_api.repository.TweetLikeRepository;
import com.twitter.twitter_rest_api.repository.TweetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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
        } else if (tweet.getTweetType() == TweetType.REPLY) {
            mapReplyResponse(response, tweet, currentUser);
        } else {
            mapNormalTweetResponse(response, tweet, currentUser);
        }

        return response;
    }

    private void mapRetweetResponse(TweetResponse response, Tweet tweet, User currentUser) {
        Tweet originalTweet = tweet.getParentTweet();

        // Retweet yapan kullanıcı bilgileri her zaman gösterilir
        mapUserInfo(response, tweet.getUser());

        if (originalTweet.isDeleted()) {
            // Orijinal tweet silinmişse
            mapDeletedOriginalTweetInfo(response, originalTweet);
        } else {
            // Orijinal tweet silinmemişse
            response.setLiked(originalTweet.getLikes().stream()
                    .anyMatch(like -> like.getUser().getId().equals(currentUser.getId())));

            Optional<Tweet> userRetweet = tweetRepository.findByUserAndParentTweetAndTweetType(
                    currentUser, originalTweet, TweetType.RETWEET);
            response.setRetweeted(userRetweet.isPresent());
            userRetweet.ifPresent(rt -> response.setRetweetId(rt.getId()));

            mapOriginalTweetInfo(response, originalTweet);
        }

        response.setTweetType(TweetType.RETWEET);
        response.setParentTweetID(originalTweet.getId());
        response.setParentTweetUserId(originalTweet.getUser().getId());
        response.setRetweetedAt(tweet.getCreatedAt());
    }

    private void mapReplyResponse(TweetResponse response, Tweet tweet, User currentUser) {
        Tweet parentTweet = tweet.getParentTweet();

        // Reply tweet'inin kendisi silinmiş mi kontrol et
        if (tweet.isDeleted()) {
            mapDeletedTweetInfo(response, tweet);
        } else {
            response.setLiked(tweet.getLikes().stream()
                    .anyMatch(like -> like.getUser().getId().equals(currentUser.getId())));

            Optional<Tweet> userRetweet = tweetRepository.findByUserAndParentTweetAndTweetType(
                    currentUser, tweet, TweetType.RETWEET);
            response.setRetweeted(userRetweet.isPresent());
            userRetweet.ifPresent(rt -> response.setRetweetId(rt.getId()));

            mapTweetInfo(response, tweet);
        }

        // Reply yapan kullanıcı bilgileri her zaman gösterilir
        mapUserInfo(response, tweet.getUser());

        // Parent tweet silinmiş mi kontrol et
        if (parentTweet.isDeleted()) {
            mapDeletedParentTweetInfo(response, parentTweet);
        } else {
            response.setOriginalUsername(parentTweet.getUser().getUsername());
            response.setOriginalUserFullName(parentTweet.getUser().getFullName());
            response.setOriginalUserProfileImage(parentTweet.getUser().getProfileImage());
        }

        response.setTweetType(TweetType.REPLY);
        response.setParentTweetID(parentTweet.getId());
        response.setParentTweetUserId(parentTweet.getUser().getId());
    }

    private void mapNormalTweetResponse(TweetResponse response, Tweet tweet, User currentUser) {
        if (tweet.isDeleted()) {
            mapDeletedTweetInfo(response, tweet);
        } else {
            response.setLiked(tweetLikeRepository.findByUserAndTweet(currentUser, tweet).isPresent());

            Optional<Tweet> userRetweet = tweetRepository.findByUserAndParentTweetAndTweetType(
                    currentUser, tweet, TweetType.RETWEET);
            response.setRetweeted(userRetweet.isPresent());
            userRetweet.ifPresent(rt -> response.setRetweetId(rt.getId()));

            mapTweetInfo(response, tweet);
        }

        // Kullanıcı bilgileri her zaman gösterilir
        mapUserInfo(response, tweet.getUser());
    }

    private void mapDeletedTweetInfo(TweetResponse response, Tweet tweet) {
        response.setContent("Bu tweet silinmiş");
        response.setMediaUrl(null);
        response.setMediaType(null);
        response.setLikeCount(0);
        response.setRetweetCount(0);
        response.setReplyCount(tweet.getReplyCount()); // Reply count'u koruyoruz
        response.setCreatedAt(tweet.getCreatedAt());
        response.setDeletedAt(tweet.getDeletedAt());
        response.setDeleted(true);
    }

    private void mapDeletedOriginalTweetInfo(TweetResponse response, Tweet originalTweet) {
        response.setContent("Bu tweet silinmiş");
        response.setMediaUrl(null);
        response.setMediaType(null);
        response.setLikeCount(0);
        response.setRetweetCount(0);
        response.setReplyCount(originalTweet.getReplyCount());
        response.setCreatedAt(originalTweet.getCreatedAt());
        response.setDeletedAt(originalTweet.getDeletedAt());
        response.setDeleted(true);

        // Orijinal tweet'in kullanıcı bilgilerini yine de göster
        response.setOriginalUsername(originalTweet.getUser().getUsername());
        response.setOriginalUserFullName(originalTweet.getUser().getFullName());
        response.setOriginalUserProfileImage(originalTweet.getUser().getProfileImage());
    }

    private void mapDeletedParentTweetInfo(TweetResponse response, Tweet parentTweet) {
        response.setOriginalUsername(parentTweet.getUser().getUsername());
        response.setOriginalUserFullName(parentTweet.getUser().getFullName());
        response.setOriginalUserProfileImage(parentTweet.getUser().getProfileImage());
        response.setParentTweetDeleted(true);
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
        response.setDeleted(tweet.isDeleted());
        if (tweet.isDeleted()) {
            response.setDeletedAt(tweet.getDeletedAt());
        }
    }
    public TweetDetailResponse toTweetDetailResponse(Tweet tweet, User currentUser) {
        TweetDetailResponse response = new TweetDetailResponse();
        // TweetResponse'dan gelen temel alanları doldur
        copyTweetResponseFields(response, toTweetResponse(tweet, currentUser));

        // Parent tweet varsa ekle
        if (tweet.getParentTweet() != null) {
            response.setParentTweet(toTweetResponse(tweet.getParentTweet(), currentUser));
        }

        // Konuşma zincirini oluştur
        response.setConversationThread(buildConversationThread(tweet, currentUser));
        response.setPartOfThread(response.getConversationThread() != null &&
                !response.getConversationThread().isEmpty());

        return response;
    }



    private List<TweetResponse> buildConversationThread(Tweet tweet, User currentUser) {
        List<TweetResponse> thread = new ArrayList<>();
        Tweet current = tweet.getParentTweet();

        while (current != null) {
            thread.add(toTweetResponse(current, currentUser));
            current = current.getParentTweet();
        }

        Collections.reverse(thread); // En eski tweet'ten başlayarak sırala
        return thread;
    }
    private void copyTweetResponseFields(TweetDetailResponse target, TweetResponse source) {
        target.setId(source.getId());
        target.setUserId(source.getUserId());
        target.setContent(source.getContent());
        target.setTweetType(source.getTweetType());
        target.setMediaUrl(source.getMediaUrl());
        target.setMediaType(source.getMediaType());
        target.setLikeCount(source.getLikeCount());
        target.setRetweetCount(source.getRetweetCount());
        target.setReplyCount(source.getReplyCount());
        target.setCreatedAt(source.getCreatedAt());
        target.setUsername(source.getUsername());
        target.setUserFullName(source.getUserFullName());
        target.setUserProfileImage(source.getUserProfileImage());
        target.setRetweeted(source.isRetweeted());
        target.setLiked(source.isLiked());
        target.setRetweetId(source.getRetweetId());
        target.setOriginalUsername(source.getOriginalUsername());
        target.setOriginalUserFullName(source.getOriginalUserFullName());
        target.setOriginalUserProfileImage(source.getOriginalUserProfileImage());
        target.setParentTweetID(source.getParentTweetID());
        target.setParentTweetUserId(source.getParentTweetUserId());
        target.setRetweetedAt(source.getRetweetedAt());
        target.setDeleted(source.isDeleted());
        target.setDeletedAt(source.getDeletedAt());
        target.setParentTweetDeleted(source.isParentTweetDeleted());
    }
}