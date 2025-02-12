package com.twitter.twitter_rest_api.repository;

import com.twitter.twitter_rest_api.entity.Tweet;
import com.twitter.twitter_rest_api.entity.TweetLike;
import com.twitter.twitter_rest_api.entity.TweetType;
import com.twitter.twitter_rest_api.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TweetLikeRepository extends JpaRepository<TweetLike,Long> {
    // Bir kullanıcının beğendiği tüm tweetleri getir
    @Query("SELECT l.tweet FROM TweetLike l WHERE l.user.id=:userId")
    List<Tweet> findTweetsLikedByUser(@Param("userId")Long userId);
    // Bir tweet'i beğenen tüm kullanıcıları getir
    @Query("SELECT l.user FROM TweetLike l WHERE l.tweet.id=:tweetId ")
    List<User> findUsersByLikedTweet(@Param("tweetId")Long tweetId);

    // Belirli bir kullanıcının belirli bir tweet'i beğenip beğenmediğini kontrol et
    @Query("SELECT l FROM TweetLike l WHERE l.user.id=:userId AND l.tweet.id=:tweetId")
    Optional<TweetLike> findByUserIdAndTweetId(@Param("userId")Long userId,@Param("tweetId")Long tweetId);

    // Bir kullanıcının beğendiği tweet sayısını getir
    @Query("SELECT COUNT(tl) FROM TweetLike tl WHERE tl.user.id = :userId")
    Long countLikesByUser(@Param("userId") Long userId);

    // Bir tweet'i beğenen kullanıcı sayısını getir
    @Query("SELECT COUNT(l) FROM TweetLike l WHERE l.tweet = :tweet")
    Long countByTweet(@Param("tweet") Tweet tweet);

    // Bir tweeti kullanıcının beğenip beğenmediğini kontrol et Tekil beğeni kontrolü için
    @Query("SELECT l FROM TweetLike l WHERE l.user=:user AND l.tweet=:targetTweet")
    Optional<TweetLike> findByUserAndTweet(@Param("user") User user,@Param("targetTweet") Tweet targetTweet);

    // Toplu beğeni kontrolü için (N+1 sorgu problemini önlemek için)
    @Query("SELECT tl FROM TweetLike tl WHERE tl.user = :user AND tl.tweet IN :tweets")
    List<TweetLike> findByUserAndTweetIn(@Param("user") User user, @Param("tweets") List<Tweet> tweets);
}
