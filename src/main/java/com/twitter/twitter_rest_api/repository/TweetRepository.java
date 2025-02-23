package com.twitter.twitter_rest_api.repository;

import com.twitter.twitter_rest_api.dto.TweetWithStats;
import com.twitter.twitter_rest_api.entity.Tweet;
import com.twitter.twitter_rest_api.entity.TweetType;
import com.twitter.twitter_rest_api.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface TweetRepository extends JpaRepository<Tweet,Long> {

    // Tweet içeriği ve kullanıcı adına göre arama
    @Query("SELECT DISTINCT t FROM Tweet t LEFT JOIN FETCH t.user u WHERE " +
            "LOWER(t.content) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Tweet> searchTweets(@Param("keyword") String keyword, Pageable pageable);

    // Hashtag araması için metod
    @Query("SELECT t FROM Tweet t WHERE " +
            "t.content LIKE CONCAT('%#', :hashtag, '%')")
    Page<Tweet> findByHashtag(@Param("hashtag") String hashtag, Pageable pageable);

    @Query("SELECT t FROM Tweet t WHERE t.user.id = :userId ORDER BY t.createdAt DESC")
    Page<Tweet> findByUserId(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT t FROM Tweet t " +
            "WHERE t.user = :user " +
            "AND t.parentTweet = :parentTweet " +
            "AND t.tweetType = :tweetType")
    Optional<Tweet> findByUserAndParentTweetAndTweetType(
            @Param("user") User user,
            @Param("parentTweet") Tweet parentTweet,
            @Param("tweetType") TweetType tweetType
    );
    // N+1 sorgu problemini çözmek için fetch join kullanıyoruz
    @Query("SELECT DISTINCT t FROM Tweet t " +
            "LEFT JOIN FETCH t.likes " +
            "LEFT JOIN FETCH t.user " +
            "WHERE t.id = :id")
    Optional<Tweet> findByIdWithLikes(@Param("id") Long id);

    // Toplu tweet çekme işlemlerinde de fetch join kullanıyoruz
    @Query("SELECT DISTINCT t FROM Tweet t " +
            "LEFT JOIN FETCH t.likes " +
            "LEFT JOIN FETCH t.user " +
            "ORDER BY t.createdAt DESC")
    Page<Tweet> findAllWithLikes(Pageable pageable);

    // Kullanıcının bir tweet'i retweet edip etmediğini kontrol etmek için sorgu
    @Query("SELECT CASE WHEN COUNT(t) > 0 THEN true ELSE false END FROM Tweet t " +
            "WHERE t.user = :user AND t.parentTweet = :tweet AND t.tweetType = 'RETWEET'")
    boolean hasUserRetweeted(@Param("user") User user, @Param("tweet") Tweet tweet);


    // Tek bir tweet'i tüm detaylarıyla getiren sorgu
    @Query("SELECT DISTINCT t FROM Tweet t " +
            "LEFT JOIN FETCH t.likes " +
            "LEFT JOIN FETCH t.user " +
            "LEFT JOIN FETCH t.parentTweet " +
            "WHERE t.id = :id")
    Optional<Tweet> findByIdWithDetails(@Param("id") Long id);


    // Tüm tweetleri gerekli ilişkilerle birlikte getiren sorgu
    @Query("SELECT DISTINCT t FROM Tweet t " +
            "LEFT JOIN FETCH t.likes " +
            "LEFT JOIN FETCH t.user " +
            "LEFT JOIN FETCH t.parentTweet " +
            "ORDER BY t.createdAt DESC")
    Page<Tweet> findAllWithDetails(Pageable pageable);

    // Tweete verilen yanıtları bulan query
    @Query("SELECT t FROM Tweet t " +
            "LEFT JOIN FETCH t.user " +
            "WHERE t.parentTweet = :parentTweet " +
            "AND t.tweetType = :tweetType " +
            "ORDER BY t.createdAt DESC")
    Page<Tweet> findRepliesByParentTweetAndType(
            @Param("parentTweet") Tweet parentTweet,
            @Param("tweetType") TweetType tweetType,
            Pageable pageable
    );
}
