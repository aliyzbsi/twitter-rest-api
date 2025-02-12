package com.twitter.twitter_rest_api.validations;

import com.twitter.twitter_rest_api.entity.Tweet;
import com.twitter.twitter_rest_api.exceptions.ApiException;
import org.springframework.http.HttpStatus;

public class TweetValidations {

    public static void tweetControl(Tweet tweet, Tweet existingTweet) {
        if (tweet.getContent() != null) {
            if (tweet.getContent().length() > 280) {
                throw new ApiException("Tweet içeriği 280 karakterden uzun olamaz!", HttpStatus.BAD_REQUEST);
            }
            if (tweet.getContent().trim().isEmpty()) {
                throw new ApiException("Tweet içeriği boş olamaz!", HttpStatus.BAD_REQUEST);
            }
            existingTweet.setContent(tweet.getContent());
        }
    }
}
