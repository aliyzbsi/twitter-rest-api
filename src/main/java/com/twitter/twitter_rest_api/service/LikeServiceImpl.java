package com.twitter.twitter_rest_api.service;

import com.twitter.twitter_rest_api.dto.TweetResponse;
import com.twitter.twitter_rest_api.entity.Tweet;
import com.twitter.twitter_rest_api.entity.TweetLike;
import com.twitter.twitter_rest_api.entity.TweetType;
import com.twitter.twitter_rest_api.entity.User;
import com.twitter.twitter_rest_api.exceptions.ApiException;
import com.twitter.twitter_rest_api.mapper.TweetMapper;
import com.twitter.twitter_rest_api.repository.TweetLikeRepository;
import com.twitter.twitter_rest_api.repository.TweetRepository;
import com.twitter.twitter_rest_api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LikeServiceImpl implements LikeService{

    private final TweetLikeRepository tweetLikeRepository;
    private final TweetRepository tweetRepository;
    private final UserRepository userRepository;
    private final TweetMapper tweetMapper;


    @Override
    @Transactional
    public TweetResponse toggleLike(Long tweetId, String userEmail) {
        try {
            User user = userRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new ApiException("Kullanıcı bulunamadı", HttpStatus.NOT_FOUND));
            Tweet tweet = tweetRepository.findById(tweetId)
                    .orElseThrow(() -> new ApiException("Tweet bulunamadı", HttpStatus.NOT_FOUND));
            if(tweet.isDeleted()){
                throw new ApiException("Silinmiş Tweet Beğenilemez",HttpStatus.BAD_REQUEST);
            }
            // Hedef tweet'i belirle (retweet ise parent tweet'i al)
            Tweet targetTweet = tweet.getTweetType() == TweetType.RETWEET
                    ? tweet.getParentTweet()
                    : tweet;

            Optional<TweetLike> existingLike = tweetLikeRepository
                    .findByUserAndTweet(user, targetTweet);

            if (existingLike.isPresent()) {
                tweetLikeRepository.delete(existingLike.get());

            } else {
                TweetLike newLike = new TweetLike();
                newLike.setTweet(targetTweet);
                newLike.setUser(user);
                tweetLikeRepository.save(newLike);


            }
            tweetRepository.flush();
            return tweetMapper.toTweetResponse(tweet, user);
        } catch (Exception e) {
            throw new ApiException("Beğeni işlemi sırasında hata: "+e.getMessage(),HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }



    @Override
    @Transactional(readOnly = true)
    public List<Tweet> getLikedTweets(Long userId) {
        if (!userRepository.existsById(userId)){
            throw new ApiException("Kullanıcı bulunamadı", HttpStatus.NOT_FOUND);
        }
        return tweetLikeRepository.findTweetsLikedByUser(userId);

    }

    @Override
    @Transactional(readOnly = true)
    public List<User> getLikedByUsers(Long tweetId) {
        if(!tweetRepository.existsById(tweetId)){
            throw new ApiException("Tweet bulunamadı", HttpStatus.NOT_FOUND);
        }
        return tweetLikeRepository.findUsersByLikedTweet(tweetId);
    }




}
