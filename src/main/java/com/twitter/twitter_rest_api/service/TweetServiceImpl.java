package com.twitter.twitter_rest_api.service;

import com.twitter.twitter_rest_api.dto.*;
import com.twitter.twitter_rest_api.entity.*;
import com.twitter.twitter_rest_api.exceptions.ApiException;
import com.twitter.twitter_rest_api.mapper.TweetMapper;
import com.twitter.twitter_rest_api.repository.TweetLikeRepository;
import com.twitter.twitter_rest_api.repository.TweetRepository;
import com.twitter.twitter_rest_api.repository.UserRepository;
import com.twitter.twitter_rest_api.validations.TweetValidations;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor //Contructor injection için
public class TweetServiceImpl implements TweetService {
    private final TweetRepository tweetRepository;
    private final UserRepository userRepository;
    private final TweetLikeRepository tweetLikeRepository;
    private final TweetMapper tweetMapper;
    private final S3Service s3Service;



    @Override
    public Page<TweetResponse> findAll(Pageable pageable, String userEmail) {
        validateSortProperties(pageable.getSort());

        User currentUser = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ApiException("Kullanıcı bulunamadı!", HttpStatus.NOT_FOUND));

        return tweetRepository.findAllNonDeletedTweets(pageable)
                .map(tweet -> tweetMapper.toTweetResponse(tweet,currentUser));
    }
    @Override
    public Page<TweetResponse> findByUserId(Long userId, Pageable pageable) {
        User targetUser = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException("Kullanıcı bulunamadı!", HttpStatus.NOT_FOUND));

        // Mevcut kullanıcıyı al
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName();
        User currentUser = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ApiException("Kullanıcı bulunamadı!", HttpStatus.NOT_FOUND));

        Page<Tweet> tweets = tweetRepository.findByUserIdNonDeleted(userId, pageable);
        return tweets.map(tweet -> tweetMapper.toTweetResponse(tweet, currentUser));
    }

    private void validateSortProperties(Sort sort) {
        Set<String> validProperties = Set.of(
                "id", "content", "createdAt", "updatedAt",
                "likeCount", "retweetCount", "replyCount"
        );

        sort.forEach(order -> {
            if (!validProperties.contains(order.getProperty())) {
                throw new IllegalArgumentException(
                        "Geçersiz sıralama alanı: " + order.getProperty() +
                                ". Geçerli alanlar: " + String.join(", ", validProperties)
                );
            }
        });
    }


    @Override
    @Cacheable(value = "tweets",key = "#id")
    public TweetDetailResponse findById(Long id, String userEmail) {
        User currentUser = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ApiException("Kullanıcı bulunamadı", HttpStatus.NOT_FOUND));

        return tweetRepository.findByIdWithDetails(id)
                .map(tweet -> tweetMapper.toTweetDetailResponse(tweet, currentUser))
                .orElseThrow(() -> new ApiException("Tweet bulunamadı", HttpStatus.NOT_FOUND));
    }



    @Override
    @Transactional
    public TweetDetailResponse createTweet(String content, MultipartFile media, String username) {
        User user=userRepository.findByEmail(username)
                .orElseThrow(() -> new ApiException("User not found", HttpStatus.NOT_FOUND));
        TweetRequest tweetRequest=new TweetRequest();
        tweetRequest.setContent(content);
        if(media!=null&&!media.isEmpty()){
            String mediaUrl=s3Service.uploadFile(media);
            tweetRequest.setMediaUrl(mediaUrl);
            tweetRequest.setMediaType(determineMediaType(media.getContentType()));
        }
        Tweet newTweet=new Tweet();
        newTweet.setContent(tweetRequest.getContent());
        newTweet.setTweetType(TweetType.TWEET);
        newTweet.setMediaUrl(tweetRequest.getMediaUrl());
        newTweet.setMediaType(tweetRequest.getMediaType());
        newTweet.setUser(user);

        tweetRepository.save(newTweet);
        user.incrementTweetsCount();
        return tweetMapper.toTweetDetailResponse(newTweet,user);
    }

    @Override
    public Page<TweetResponse> findRepliesByTweetId(Long tweetId, Pageable pageable, String username) {
        log.debug("Finding replies for tweet ID: {} with page: {}", tweetId, pageable.getPageNumber());

        // Mevcut kullanıcıyı bul
        User currentUser = userRepository.findByEmail(username)
                .orElseThrow(() -> new ApiException("Kullanıcı bulunamadı", HttpStatus.NOT_FOUND));
        log.debug("Current user found: {}", currentUser.getUsername());

        // Tweet'in var olduğunu kontrol et
        Tweet parentTweet = tweetRepository.findById(tweetId)
                .orElseThrow(() -> new ApiException("Tweet bulunamadı", HttpStatus.NOT_FOUND));
        log.debug("Parent tweet found: {}", parentTweet.getId());

        // Parent tweet'in detaylarını logla
        log.debug("Parent tweet details - ID: {}, Content: {}, Type: {}",
                parentTweet.getId(),
                parentTweet.getContent(),
                parentTweet.getTweetType());

        // Yanıtları getir
        Page<Tweet> replies = tweetRepository.findRepliesByTweetId(tweetId, pageable);
        log.debug("Found {} replies", replies.getTotalElements());

        // Eğer yanıt yoksa nedenini anlamak için veritabanını kontrol et
        if (replies.isEmpty()) {
            // Manuel olarak yanıtları kontrol et
            List<Tweet> allReplies = tweetRepository.findAll()
                    .stream()
                    .filter(t -> t.getTweetType() == TweetType.REPLY)
                    .filter(t -> !t.isDeleted())
                    .filter(t -> t.getParentTweet() != null && t.getParentTweet().getId().equals(tweetId))
                    .toList();

            log.debug("Manual reply check found {} potential replies", allReplies.size());
            allReplies.forEach(reply -> {
                log.debug("Potential reply - ID: {}, ParentID: {}, Type: {}, Deleted: {}",
                        reply.getId(),
                        reply.getParentTweet() != null ? reply.getParentTweet().getId() : "null",
                        reply.getTweetType(),
                        reply.isDeleted());
            });
        }

        return replies.map(reply -> {
            TweetResponse response = tweetMapper.toTweetResponse(reply, currentUser);
            response.setParentTweetID(tweetId);
            return response;
        });
    }

    @Override
    @Transactional
    public TweetDetailResponse replyToTweet(Long tweetId, String content, MultipartFile media, String username) {
        // 1. Kullanıcı kontrolü
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new ApiException("Kullanıcı bulunamadı", HttpStatus.NOT_FOUND));

        // 2. Parent tweet kontrolü
        Tweet parentTweet = tweetRepository.findById(tweetId)
                .orElseThrow(() -> new ApiException("Yanıt verilen tweet bulunamadı", HttpStatus.BAD_REQUEST));

        // 3. Silinen tweet kontrolü
        if (parentTweet.isDeleted()) {
            throw new ApiException("Silinen tweet'e yanıt verilemez", HttpStatus.BAD_REQUEST);
        }

        // 4. Yeni reply tweet oluştur
        Tweet newReplyTweet = new Tweet();
        newReplyTweet.setContent(content);
        newReplyTweet.setTweetType(TweetType.REPLY);
        newReplyTweet.setUser(user);

        // 5. Media işleme
        if (media != null && !media.isEmpty()) {
            String mediaUrl = s3Service.uploadFile(media);
            newReplyTweet.setMediaUrl(mediaUrl);
            newReplyTweet.setMediaType(determineMediaType(media.getContentType()));
        }

        // 6. Parent tweet'i belirle ve reply count'u güncelle
        Tweet targetParent = determineParentTweet(parentTweet);
        newReplyTweet.setParentTweet(targetParent);
        targetParent.incrementReplyCount();

        // 7. Değişiklikleri kaydet
        tweetRepository.save(targetParent);
        tweetRepository.save(newReplyTweet);
        user.incrementTweetsCount();

        return tweetMapper.toTweetDetailResponse(newReplyTweet, user);
    }

    @Override
    @Transactional
    public TweetDetailResponse retweet(RetweetRequest retweetRequest, String username) {
     try {
         User user=userRepository.findByEmail(username)
                 .orElseThrow(()->new ApiException("Kullanıcı bulunamadı! ",HttpStatus.NOT_FOUND));
         Tweet targetTweet=tweetRepository.findById(retweetRequest.getParentTweetId())
                 .orElseThrow(()->new ApiException("Retweetlemek istenen tweet bulunamadı!", HttpStatus.NOT_FOUND));

         Tweet originalTweet=targetTweet.getTweetType()==TweetType.RETWEET
                 ?targetTweet.getParentTweet()
                 :targetTweet;

        Optional<Tweet> existingRetweet=tweetRepository.findByUserAndParentTweetAndTweetType(user,originalTweet,TweetType.RETWEET);
         if (targetTweet.isDeleted()) {
             throw new ApiException("Silinen tweet'e yenilden gönderilemez", HttpStatus.BAD_REQUEST);
         }
         if (existingRetweet.isPresent()){
        // Eğer kullanıcı daha önce retweet yapmışsa, retweet'i geri al
             Tweet retweet=existingRetweet.get();
             originalTweet.removeRetweet(user);
             user.decrementTweetsCount();

             tweetRepository.delete(retweet);
             tweetRepository.save(originalTweet);
             TweetDetailResponse response= tweetMapper.toTweetDetailResponse(originalTweet,user);
             response.setRetweetId(null);
             response.setRetweeted(false);

             return response;
         }else {
             // Eğer kullanıcı daha önce retweet yapmamışsa, retweet yap

             Tweet retweet = new Tweet();
             retweet.setContent(originalTweet.getContent());
             retweet.setTweetType(TweetType.RETWEET);
             retweet.setParentTweet(originalTweet);
             retweet.setUser(user);
             retweet.setMediaUrl(originalTweet.getMediaUrl());
             retweet.setMediaType(originalTweet.getMediaType());

             originalTweet.addRetweet(user);
             user.incrementTweetsCount();


             tweetRepository.save(retweet);
             tweetRepository.save(originalTweet);
             userRepository.save(user);
             TweetDetailResponse response= tweetMapper.toTweetDetailResponse(retweet,user);
             response.setRetweeted(true);
             response.setRetweetId(retweet.getId());

             return response;
         }

     }catch (Exception e){

            throw new ApiException("İşlem sırasında bir hata oluştu: "+e.getMessage(),HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @Override
    @Transactional
    public TweetDetailResponse quoteTweet(Long tweetId, String content, MultipartFile media, String username) {
        try {
            User user = userRepository.findByEmail(username)
                    .orElseThrow(() -> new ApiException("Kullanıcı bulunamadı!", HttpStatus.NOT_FOUND));

            Tweet parentTweet = tweetRepository.findById(tweetId)
                    .orElseThrow(() -> new ApiException("Alıntılanmak istenen tweet bulunamadı!", HttpStatus.NOT_FOUND));

            if (parentTweet.isDeleted()) {
                throw new ApiException("Silinen tweet alıntılanamaz", HttpStatus.BAD_REQUEST);
            }

            // Retweet ise orijinal tweet'i al
            Tweet originalTweet = parentTweet.getTweetType() == TweetType.RETWEET
                    ? parentTweet.getParentTweet()
                    : parentTweet;

            Tweet quoteTweet = new Tweet();
            quoteTweet.setContent(content);
            quoteTweet.setTweetType(TweetType.QUOTE);
            quoteTweet.setUser(user);
            quoteTweet.setParentTweet(originalTweet);

            // Media işleme
            if (media != null && !media.isEmpty()) {
                String mediaUrl = s3Service.uploadFile(media);
                quoteTweet.setMediaUrl(mediaUrl);
                quoteTweet.setMediaType(determineMediaType(media.getContentType()));
            }

            tweetRepository.save(quoteTweet);
            user.incrementTweetsCount();

            // Quote count'u artır
            originalTweet.setRetweetCount(originalTweet.getRetweetCount() + 1);
            tweetRepository.save(originalTweet);

            return tweetMapper.toTweetDetailResponse(quoteTweet, user);
        } catch (Exception e) {
            throw new ApiException("İşlem sırasında bir hata oluştu: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @Override
    @Transactional
    public TweetDetailResponse update(Long id, Tweet tweet, String userEmail) {
        User currentUser = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ApiException("Kullanıcı bulunamadı", HttpStatus.NOT_FOUND));
        Tweet existingTweet = tweetRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ApiException("Tweet bulunamadı", HttpStatus.NOT_FOUND));

        if(!existingTweet.getUser().getEmail().equals(userEmail)){
            throw new ApiException("Sadece kendi tweetlerini güncelleyebilirsin!", HttpStatus.FORBIDDEN);
        }
        TweetValidations.tweetControl(tweet,existingTweet);
        if (tweet.getMediaType() != null) {
            existingTweet.setMediaType(tweet.getMediaType());
        }
        if (tweet.getMediaUrl() != null) {
            existingTweet.setMediaUrl(tweet.getMediaUrl());
        }
        existingTweet.setUpdatedAt(LocalDateTime.now());
        Tweet savedTweet = tweetRepository.save(existingTweet);
        return tweetMapper.toTweetDetailResponse(savedTweet, currentUser);
    }

    @Override
    @Transactional
    public TweetDetailResponse delete(Long id, String userEmail) {
        User currentUser = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ApiException("Kullanıcı bulunamadı", HttpStatus.NOT_FOUND));

        Tweet existingTweet = tweetRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ApiException("Tweet bulunamadı", HttpStatus.NOT_FOUND));

        if(!existingTweet.getUser().getEmail().equals(userEmail)){
            throw new ApiException("Sadece kendi twitlerini silebilirsin!", HttpStatus.FORBIDDEN);
        }

        // Parent tweet varsa reply count'u azalt
        if(existingTweet.getParentTweet() != null&&existingTweet.getTweetType()==TweetType.REPLY){
            Tweet parentTweet = existingTweet.getParentTweet();
            parentTweet.decrementReplyCount();
            tweetRepository.save(parentTweet);
        }

        // Retweet'leri bul ve sil
        List<Tweet> retweets = tweetRepository.findByParentTweetAndTweetType(existingTweet, TweetType.RETWEET);
        for (Tweet retweet : retweets) {
            retweet.getUser().decrementTweetsCount();
            tweetRepository.delete(retweet);
        }

        // Orijinal içeriği sakla
        existingTweet.setOriginalContent(existingTweet.getContent());
        existingTweet.setOriginalMediaUrl(existingTweet.getMediaUrl());
        existingTweet.setOriginalMediaType(existingTweet.getMediaType());

        // Tweet'i soft delete yap

        existingTweet.setDeleted(true);
        existingTweet.setDeletedAt(LocalDateTime.now());
        existingTweet.setContent("Bu tweet silinmiş");
        existingTweet.setMediaUrl(null);
        existingTweet.setMediaType(MediaType.NONE);

        // Kullanıcının tweet sayısını azalt
        existingTweet.getUser().decrementTweetsCount();

        tweetRepository.save(existingTweet);

        return tweetMapper.toTweetDetailResponse(existingTweet, currentUser);
    }

    private MediaType determineMediaType(String contentType) {
        if (contentType == null) return MediaType.NONE;

        if (contentType.startsWith("image/")) {
            if (contentType.equals("image/gif")) {
                return MediaType.GIF;
            }
            return MediaType.IMAGE;
        } else if (contentType.startsWith("video/")) {
            return MediaType.VIDEO;
        }

        return MediaType.NONE;
    }
    private Tweet determineParentTweet(Tweet parentTweet) {
        if (parentTweet.getTweetType() == TweetType.RETWEET) {
            // Retweet'e yapılan yanıtlar orjinal tweet'e bağlanır
            return parentTweet.getParentTweet();
        }
        // Quote ve normal tweet'lere yapılan yanıtlar direkt tweet'e bağlanır
        return parentTweet;
    }
}
