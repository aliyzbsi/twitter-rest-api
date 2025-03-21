package com.twitter.twitter_rest_api.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.twitter.twitter_rest_api.entity.MediaType;
import com.twitter.twitter_rest_api.entity.TweetType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "Tweet yanıtı için bilgiler")
public class TweetResponse {
    @Schema(description = "Tweet ID")
    private Long id;
    @Schema(description = "Tweet User ID")
    private Long userId;

    @Schema(description = "Tweet içeriği")
    private String content;

    @Schema(description = "Tweet tipi")
    private TweetType tweetType;

    @Schema(description = "Medya URL'si")
    private String mediaUrl;

    @Schema(description = "Medya tipi")
    private MediaType mediaType;

    @Schema(description = "Beğeni sayısı")
    private Integer likeCount;

    @Schema(description = "Retweet sayısı")
    private Integer retweetCount;

    @Schema(description = "Yanıt sayısı")
    private Integer replyCount;

    @Schema(description = "Oluşturulma tarihi")
    private LocalDateTime createdAt;

    @Schema(description = "Tweet sahibinin kullanıcı adı")
    private String username;

    @Schema(description = "Tweet sahibinin tam adı")
    private String userFullName;

    @Schema(description = "Tweet sahibinin profil resmi")
    private String userProfileImage;

    private boolean isRetweeted;
    private boolean isLiked;
    private Long retweetId;

    private String originalUsername;
    private String originalUserFullName;
    private String originalUserProfileImage;
    private Long parentTweetID;
    private Long parentTweetUserId;
    private LocalDateTime retweetedAt;

    @Schema(description = "Tweet'in silinip silinmediği")
    private boolean deleted;

    @Schema(description = "Tweet'in silinme tarihi")
    private LocalDateTime deletedAt;

    @Schema(description = "Parent tweet'in silinip silinmediği")
    private boolean parentTweetDeleted;

    @Schema(description = "Orijinal içerik (admin/moderatör erişimi için)")
    @JsonIgnore // Normal kullanıcılara gösterilmeyecek
    private String originalContent;

    @Schema(description = "Orijinal medya URL'si (admin/moderatör erişimi için)")
    @JsonIgnore
    private String originalMediaUrl;

    @Schema(description = "Orijinal medya tipi (admin/moderatör erişimi için)")
    @JsonIgnore
    private MediaType originalMediaType;


}
