package com.twitter.twitter_rest_api.service;

import com.twitter.twitter_rest_api.dto.*;
import com.twitter.twitter_rest_api.entity.MediaType;
import com.twitter.twitter_rest_api.entity.Tweet;
import com.twitter.twitter_rest_api.entity.TweetType;
import com.twitter.twitter_rest_api.entity.User;
import com.twitter.twitter_rest_api.exceptions.ApiException;
import com.twitter.twitter_rest_api.mapper.TweetMapper;
import com.twitter.twitter_rest_api.repository.TweetLikeRepository;
import com.twitter.twitter_rest_api.repository.TweetRepository;
import com.twitter.twitter_rest_api.repository.UserRepository;
import com.twitter.twitter_rest_api.validations.TweetValidations;
import lombok.RequiredArgsConstructor;
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

        return tweetRepository.findAllWithDetails(pageable)
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

        Page<Tweet> tweets = tweetRepository.findByUserId(userId, pageable);
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
    public TweetResponse findById(Long id,String userEmail) {
       User currentUser=userRepository.findByEmail(userEmail)
               .orElseThrow(() -> new ApiException("Kullanıcı bulunamadı",HttpStatus.NOT_FOUND));

       return tweetRepository.findByIdWithDetails(id)
               .map(tweet -> tweetMapper.toTweetResponse(tweet,currentUser))
               .orElseThrow(() -> new ApiException("Tweet bulunamadı",HttpStatus.NOT_FOUND));

    }



    @Override
    public Page<TweetResponse> findRepliesByTweetId(Long tweetId, Pageable pageable, String username) {
        User currentUser = userRepository.findByEmail(username)
                .orElseThrow(() -> new ApiException("Kullanıcı bulunamadı", HttpStatus.NOT_FOUND));
        Tweet parentTweet=tweetRepository.findById(tweetId)
                .orElseThrow(() -> new ApiException("Tweet bulunamadı", HttpStatus.NOT_FOUND));
        Page<Tweet> replies=tweetRepository.findRepliesByParentTweetAndType(
                parentTweet,
                TweetType.REPLY,
                pageable
        );
        return replies.map(reply->tweetMapper.toTweetResponse(reply,currentUser));
    }

    @Override
    @Transactional
    public TweetResponse createTweet(String content, MultipartFile media, String username) {
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
        return tweetMapper.toTweetResponse(newTweet,user);
    }

    @Override
    @Transactional
    public TweetResponse replyToTweet(Long tweetId, String content, MultipartFile media, String username) {
        User user=userRepository.findByEmail(username)
                .orElseThrow(() -> new ApiException("User not found", HttpStatus.NOT_FOUND));

        ReplyTweetRequest replyTweetRequest=new ReplyTweetRequest();
        replyTweetRequest.setContent(content);
        replyTweetRequest.setParentTweetId(tweetId);

        if(media!=null&&!media.isEmpty()){
            String mediaUrl=s3Service.uploadFile(media);
            replyTweetRequest.setMediaUrl(mediaUrl);
            replyTweetRequest.setMediaType(determineMediaType(media.getContentType()));
        }
        Tweet newReplyTweet = new Tweet();
        newReplyTweet.setContent(replyTweetRequest.getContent());
        newReplyTweet.setTweetType(TweetType.REPLY);
        newReplyTweet.setMediaUrl(replyTweetRequest.getMediaUrl());
        newReplyTweet.setMediaType(replyTweetRequest.getMediaType());
        newReplyTweet.setUser(user);

        Tweet parentTweet = tweetRepository.findById(tweetId)
                .orElseThrow(() -> new ApiException("Yanıt verilen tweet bulunamadı", HttpStatus.NOT_FOUND));
        newReplyTweet.setParentTweet(parentTweet);
        parentTweet.incrementReplyCount();
        tweetRepository.save(parentTweet);
        tweetRepository.save(newReplyTweet);
        user.incrementTweetsCount();
        return tweetMapper.toTweetResponse(newReplyTweet,user);
    }

    @Override
    @Transactional
    public TweetResponse retweet(RetweetRequest retweetRequest, String username) {
     try {
         User user=userRepository.findByEmail(username)
                 .orElseThrow(()->new ApiException("Kullanıcı bulunamadı! ",HttpStatus.NOT_FOUND));
         Tweet targetTweet=tweetRepository.findById(retweetRequest.getParentTweetId())
                 .orElseThrow(()->new ApiException("Retweetlemek istenen tweet bulunamadı!", HttpStatus.NOT_FOUND));

         Tweet originalTweet=targetTweet.getTweetType()==TweetType.RETWEET
                 ?targetTweet.getParentTweet()
                 :targetTweet;

        Optional<Tweet> existingRetweet=tweetRepository.findByUserAndParentTweetAndTweetType(user,originalTweet,TweetType.RETWEET);

         if (existingRetweet.isPresent()){
        // Eğer kullanıcı daha önce retweet yapmışsa, retweet'i geri al
             Tweet retweet=existingRetweet.get();
             originalTweet.removeRetweet(user);
             user.decrementTweetsCount();

             tweetRepository.delete(retweet);
             tweetRepository.save(originalTweet);
             TweetResponse response= tweetMapper.toTweetResponse(originalTweet,user);
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
             TweetResponse response= tweetMapper.toTweetResponse(retweet,user);
             response.setRetweeted(true);
             response.setRetweetId(retweet.getId());

             return response;
         }

     }catch (Exception e){
              e.printStackTrace();
            throw new ApiException("İşlem sırasında bir hata oluştu: "+e.getMessage(),HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @Override
    @Transactional
    public TweetResponse quoteTweet(QuoteTweetRequest quoteTweetRequest, String username) {
       try {
           User user=userRepository.findByEmail(username)
                   .orElseThrow(()->new ApiException("Kullanıcı bulunamadı! ",HttpStatus.NOT_FOUND));
           Tweet parentTweet=tweetRepository.findById(quoteTweetRequest.getParentTweetId())
                   .orElseThrow(()->new ApiException("Alıntılanmak istenen tweet bulunamadı!", HttpStatus.NOT_FOUND));
           Tweet quoteTweet=new Tweet();
           quoteTweet.setContent(quoteTweetRequest.getContent());
           quoteTweet.setTweetType(TweetType.QUOTE);
           quoteTweet.setUser(user);
           quoteTweet.setMediaUrl(quoteTweetRequest.getMediaUrl());
           quoteTweet.setMediaType(quoteTweetRequest.getMediaType());
           quoteTweet.setParentTweet(parentTweet);

           tweetRepository.save(quoteTweet);
           user.incrementTweetsCount();
           parentTweet.setRetweetCount(parentTweet.getRetweetCount()+1);
           tweetRepository.save(parentTweet);
           return tweetMapper.toTweetResponse(quoteTweet,user);
       }catch (Exception e){
           throw new ApiException("İşlem sırasında bir hata oluştu: "+e.getMessage(),HttpStatus.INTERNAL_SERVER_ERROR);
       }

    }


    @Override
    @Transactional
    public TweetResponse update(Long id, Tweet tweet, String userEmail) {
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
        return tweetMapper.toTweetResponse(savedTweet, currentUser);
    }

    @Override
    @Transactional
    public TweetResponse delete(Long id, String userEmail) {
        User currentUser = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ApiException("Kullanıcı bulunamadı", HttpStatus.NOT_FOUND));
        Tweet existingTweet = tweetRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ApiException("Tweet bulunamadı", HttpStatus.NOT_FOUND));
        if(!existingTweet.getUser().getEmail().equals(userEmail)){
            throw new ApiException("Sadece kendi twitlerini silebilirsin!",HttpStatus.FORBIDDEN);
        }
        if(existingTweet.getParentTweet()!=null){
            Tweet parentTweet=existingTweet.getParentTweet();
            parentTweet.setReplyCount(parentTweet.getReplyCount()-1);
        }
        System.out.println("Burası çalıştı!");
        existingTweet.getUser().decrementTweetsCount();
        tweetRepository.delete(existingTweet);
        return tweetMapper.toTweetResponse(existingTweet,currentUser);
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
}
