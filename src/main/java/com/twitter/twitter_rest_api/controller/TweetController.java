package com.twitter.twitter_rest_api.controller;

import com.twitter.twitter_rest_api.dto.*;
import com.twitter.twitter_rest_api.entity.MediaType;
import com.twitter.twitter_rest_api.entity.Tweet;
import com.twitter.twitter_rest_api.service.LikeService;
import com.twitter.twitter_rest_api.service.S3Service;
import com.twitter.twitter_rest_api.service.TweetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/tweet")
@Tag(name = "Tweet", description = "Tweet management APIs")
public class TweetController {

    private final TweetService tweetService;
    private final LikeService likeService;
    private final S3Service s3Service;
    @Autowired
    public TweetController(LikeService likeService, TweetService tweetService,S3Service s3Service) {
        this.likeService = likeService;
        this.tweetService = tweetService;
        this.s3Service=s3Service;
    }

    @GetMapping
    @Operation(
            summary = "Bütün tweetleri getir",
            description = "Sayfalama ve sıralama parametreleri ile tweetleri getirir. " +
                    "Sıralama için kullanılabilecek alanlar: id, content, createdAt, updatedAt, likeCount, retweetCount, replyCount"
    )
    @Parameter(
            name = "sort",
            description = "Sıralama parametreleri. Örnek: createdAt,desc veya likeCount,desc",
            example = "createdAt,desc"
    )
    public ResponseEntity<Page<TweetResponse>> getAllTweets(

            @PageableDefault(
                    size = 20,
                    sort = "createdAt",
                    direction = Sort.Direction.DESC
            ) Pageable pageable,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(tweetService.findAll(pageable,userDetails.getUsername()));
    }

    @GetMapping("/user/{userID}")
    @Operation(summary = "Kullanıcının tweetlerini getir")
    public ResponseEntity<Page<TweetResponse>> getAllTweetForUser(
            @PathVariable("userID") Long userID,
            @PageableDefault(
                    size = 20,
                    sort = "createdAt",
                    direction = Sort.Direction.DESC
            ) Pageable pageable
    ) {
        return ResponseEntity.ok(tweetService.findByUserId(userID, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Tweet'i ID'ye göre getir")
    public ResponseEntity<TweetResponse> findById(
            @PathVariable("id") long id,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.ok(tweetService.findById(id, userDetails.getUsername()));
    }




    @Operation(
            summary = "Yeni Tweet ",
            description = "yeni tweet atar",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @Parameter(
            name = "file",
            description = "Yüklenecek fotoğraf (Optional)",
            content = @Content(mediaType = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    )
    @ApiResponse(responseCode = "201", description = "Tweet paylaşıldı")

    @PostMapping(consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<TweetResponse> createTweet(
            @RequestParam(value = "media", required = false) MultipartFile media,
            @RequestParam("content") String content,
            @AuthenticationPrincipal UserDetails userDetails) {

        TweetResponse tweet = tweetService.createTweet(content, media, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(tweet);
    }

    @PostMapping(value = "/{tweetId}/reply",consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    @Parameter(
            name = "file",
            description = "Yüklenecek fotoğraf (Optional)",
            content = @Content(mediaType = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    )
    @Operation(summary = "Tweet'e yanıt ver",
            security = @SecurityRequirement(name = "Bearer Authentication"))
    public ResponseEntity<TweetResponse> replyToTweet(
            @PathVariable("tweetId") long tweetId,
            @RequestParam(value = "media", required = false) MultipartFile media,
            @RequestParam("content") String content,
            @AuthenticationPrincipal UserDetails userDetails) {

        TweetResponse tweet = tweetService.replyToTweet(tweetId, content, media, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(tweet);
    }


    @PostMapping("/{tweetId}/like")
    @Operation(summary = "Tweet beğen/beğeniyi kaldır")
    public TweetResponse likeTweet(@PathVariable("tweetId")Long tweetId,
                          @AuthenticationPrincipal UserDetails userDetails){
        return likeService.toggleLike(tweetId,userDetails.getUsername());
    }

    @GetMapping("/{tweetId}/reply")
    @Operation(summary = "Tweet'e verilen yanıtları getir")
    public ResponseEntity<Page<TweetResponse>> getReplies(
            @PathVariable("tweetId") Long tweetId,
            @PageableDefault(
                    size = 20,
                    sort = "createdAt",
                    direction = Sort.Direction.DESC
            ) Pageable pageable,
            @AuthenticationPrincipal UserDetails userDetails
    ){
        return ResponseEntity.ok(tweetService.findRepliesByTweetId(tweetId,pageable,userDetails.getUsername()));
    }




    @PostMapping("/{tweetId}/retweet")
    @Operation(summary = "Tweet'i retweet et")
    public TweetResponse retweet(@PathVariable("tweetId")Long tweetId,
                                 @AuthenticationPrincipal UserDetails userDetails){
        RetweetRequest retweetRequest=new RetweetRequest();
        retweetRequest.setParentTweetId(tweetId);
        return tweetService.retweet(retweetRequest,userDetails.getUsername());

    }

    @PostMapping("/{tweetId}/quote")
    @Operation(summary = "Tweet'i alıntıla")
    public TweetResponse quoteTweet(
            @PathVariable("tweetId") Long tweetId,
            @Valid @RequestBody QuoteTweetRequest quoteTweetRequest,
            @AuthenticationPrincipal UserDetails userDetails
            ){
        quoteTweetRequest.setParentTweetId(tweetId);
        return tweetService.quoteTweet(quoteTweetRequest,userDetails.getUsername());
    }

    @PutMapping("/{id}")
    @SecurityRequirement(name = "basicAuth")
    @Operation(summary = "Tweet güncelle")
    public TweetResponse updateTweet(
            @PathVariable("id") Long id,
            @Valid @RequestBody Tweet tweet,
            @AuthenticationPrincipal UserDetails userDetails) {
        return tweetService.update(id, tweet, userDetails.getUsername());
    }

    @DeleteMapping("/{id}")
    @SecurityRequirement(name = "basicAuth")
    @Operation(summary = "Tweet sil eğer seninse")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public TweetResponse deleteTweet(
            @PathVariable("id") Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        return tweetService.delete(id, userDetails.getUsername());
    }
}