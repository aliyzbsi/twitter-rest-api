package com.twitter.twitter_rest_api.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "tweets",schema = "twitterapi")
public class Tweet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "content",nullable = false)
    @Size(min = 1, max = 280)
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name = "tweet_type")
    private TweetType tweetType=TweetType.TWEET;

    @Column(name = "media_url")
    private String mediaUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "media_type")
    private MediaType mediaType=MediaType.NONE;

    @Column(name = "like_count")
    private Integer likeCount = 0;

    @Column(name = "retweet_count")
    private Integer retweetCount=0;

    @Column(name = "reply_count")
    private Integer replyCount=0;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    //Atılan tweetin sahibi olan kullanıcıyı tutacak
    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.DETACH,CascadeType.MERGE,CascadeType.PERSIST,CascadeType.REFRESH})
    @JoinColumn(name = "user_id",nullable = false)
    @JsonIgnore         //sonsuz döngüyü engellemek için
    private User user;

    //Eğer tweet retweet yada tweete verilen yanıtsa bunu belirtmek için hangi tweete bağlı olduğunu belirttik
    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
    @JoinColumn(name = "parent_tweet_id")
    @JsonIgnore
    private Tweet parentTweet;

    @OneToMany(mappedBy = "tweet",cascade = {CascadeType.DETACH,CascadeType.MERGE,CascadeType.PERSIST,CascadeType.REFRESH})
    @JsonIgnore
    private Set<TweetLike> likes=new HashSet<>();

    @ManyToMany
    @JoinTable(name = "retweets", schema = "twitterapi",
            joinColumns = @JoinColumn(name = "tweet_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id"))
    @JsonIgnore
    private Set<User> retweetedBy = new HashSet<>();

    // Yanıt ilişkileri - Thread yapısı için
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "in_reply_to_tweet_id")
    @JsonIgnore
    private Tweet inReplyToTweet;


    @OneToMany(mappedBy = "inReplyToTweet")
    @JsonIgnore
    private Set<Tweet> replies = new HashSet<>();

    @Column(name = "is_deleted")
    private boolean deleted = false;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    // Silinen içeriği saklamak için
    @Column(name = "original_content", length = 280)
    private String originalContent;

    @Column(name = "original_media_url")
    private String originalMediaUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "original_media_type")
    private MediaType originalMediaType;

    @Scheduled(fixedRate = 300000) // 5 dakikada bir
    public void updateCounts() {
        this.likeCount = likes.size();
        this.retweetCount=retweetedBy.size();

    }

    public void addRetweet(User user){
        retweetedBy.add(user);
        this.retweetCount++;
    }


    public void removeRetweet(User user) {
        if (retweetedBy.remove(user)) {
            this.retweetCount = Math.max(0, this.retweetCount - 1);
        }
    }
    @Transient
    public Integer getLikeCount() {
        return Math.max(likeCount, likes != null ? likes.size() : 0);
    }


    public void incrementReplyCount() {
        this.replyCount = (this.replyCount == null ? 0 : this.replyCount) + 1;
    }

    public void decrementReplyCount() {
        if (this.replyCount != null && this.replyCount > 0) {
            this.replyCount--;
        }
    }
    @PrePersist
    @PreUpdate
    private void prePersist() {
        if (this.tweetType == TweetType.REPLY) {
            // Reply type tweet'lerde parentTweet ve inReplyToTweet aynı olmalı
            this.inReplyToTweet = this.parentTweet;
        }
    }


    // Tweet silinmeden önce parent tweet'in sayaçlarını güncelle
    @PreRemove
    private void preRemove() {
        if (this.parentTweet != null) {
            if (Objects.requireNonNull(this.tweetType) == TweetType.REPLY) {
                this.parentTweet.decrementReplyCount();
            }
        }
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Tweet)) return false;
        Tweet tweet = (Tweet) o;
        return id != null && id.equals(tweet.getId());
    }

    @Override
    public int hashCode() {
        // Sadece ID'yi kullan, koleksiyonları dahil etme
        return getClass().hashCode();
    }

    //1. ConcurrentModificationException hatası aldık
    //2. Hata, Tweet entity'sinde Lombok'un @Data anotasyonundan kaynaklanan hashCode() metodunun çalışması sırasında oluşuyor
    //3. Sorun, Tweet entity'sindeki koleksiyonların (likes ve retweetedBy) lazy loading sırasında değiştirilmeye çalışılmasından kaynaklanıyor
    //4. @Data anotasyonu yerine daha spesifik Lombok anotasyonları kullanmalıyız
}
