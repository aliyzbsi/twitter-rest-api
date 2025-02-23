// SearchService.java
package com.twitter.twitter_rest_api.service;

import com.twitter.twitter_rest_api.dto.TweetResponse;
import com.twitter.twitter_rest_api.entity.Tweet;
import com.twitter.twitter_rest_api.entity.User;
import com.twitter.twitter_rest_api.exceptions.ApiException;
import com.twitter.twitter_rest_api.mapper.TweetMapper;
import com.twitter.twitter_rest_api.repository.TweetRepository;
import com.twitter.twitter_rest_api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class SearchService {
    private final TweetRepository tweetRepository;
    private final UserRepository userRepository;
    private final TweetMapper tweetMapper;

    public Map<String, Object> search(String query, Pageable pageable, String username) {

        if (query == null || query.trim().isEmpty()) {
            throw new ApiException("Arama sorgusu boş olamaz", HttpStatus.BAD_REQUEST);
        }

        // Önce email ile deneyelim, bulamazsa username ile arayalım
        User currentUser = userRepository.findByUsername(username)
                .orElseGet(() -> userRepository.findByEmail(username)
                        .orElseThrow(() -> new ApiException("Kullanıcı bulunamadı: " + username, HttpStatus.NOT_FOUND)));

        // Tweet araması
        Page<TweetResponse> tweets = tweetRepository.searchTweets(query.trim(), pageable)
                .map(tweet -> tweetMapper.toTweetResponse(tweet, currentUser));

        // Kullanıcı araması
        List<User> users = userRepository.searchUsers(query.trim());

        Map<String, Object> result = new HashMap<>();
        result.put("tweets", tweets);
        result.put("users", users);

        return result;
    }

    public Page<TweetResponse> findByHashtag(String hashtag, Pageable pageable, String username) {
        if (hashtag == null || hashtag.trim().isEmpty()) {
            throw new ApiException("Hashtag boş olamaz", HttpStatus.BAD_REQUEST);
        }

        // Önce email ile deneyelim, bulamazsa username ile arayalım
        User currentUser = userRepository.findByUsername(username)
                .orElseGet(() -> userRepository.findByEmail(username)
                        .orElseThrow(() -> new ApiException("Kullanıcı bulunamadı: " + username, HttpStatus.NOT_FOUND)));

        return tweetRepository.findByHashtag(hashtag.trim(), pageable)
                .map(tweet -> tweetMapper.toTweetResponse(tweet, currentUser));
    }
}