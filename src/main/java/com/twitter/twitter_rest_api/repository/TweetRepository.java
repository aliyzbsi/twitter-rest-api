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

    @Query("SELECT t FROM Tweet t WHERE t.deleted=false AND t.user.id = :userId ORDER BY t.createdAt DESC")
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
    // Tweet'i tüm detaylarıyla getir
    @Query("""
        SELECT t FROM Tweet t
        LEFT JOIN FETCH t.user u
        LEFT JOIN FETCH t.inReplyToTweet rt
     
        WHERE t.id = :id
        """)
    Optional<Tweet> findByIdWithDetails(@Param("id") Long id);

    // Bir tweet'e yapılan yanıtları getir
    @Query("""
    SELECT DISTINCT t FROM Tweet t
    LEFT JOIN FETCH t.user u
    WHERE t.parentTweet.id = :tweetId
    AND t.tweetType = com.twitter.twitter_rest_api.entity.TweetType.REPLY
    AND t.deleted = false
    ORDER BY t.createdAt DESC
    """)
    Page<Tweet> findRepliesByTweetId(@Param("tweetId") Long tweetId, Pageable pageable);



    // Ana sayfa için tweetleri getir
    @Query("""
        SELECT t FROM Tweet t
        LEFT JOIN FETCH t.user u
        WHERE t.deleted = false
        ORDER BY t.createdAt DESC
        """)
    Page<Tweet> findAllNonDeletedTweets(Pageable pageable);

    // Kullanıcının tweetlerini getir
    @Query("""
        SELECT t FROM Tweet t
        LEFT JOIN FETCH t.user u
        WHERE t.user.id = :userId
        AND t.deleted = false
        ORDER BY t.createdAt DESC
        """)
    Page<Tweet> findByUserIdNonDeleted(@Param("userId") Long userId, Pageable pageable);
    List<Tweet> findByParentTweetAndTweetType(Tweet existingTweet, TweetType tweetType);
}
