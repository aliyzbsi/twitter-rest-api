package com.twitter.twitter_rest_api.service;

import com.twitter.twitter_rest_api.dto.UserResponse;
import com.twitter.twitter_rest_api.entity.Follow;
import com.twitter.twitter_rest_api.entity.User;
import com.twitter.twitter_rest_api.exceptions.ApiException;
import com.twitter.twitter_rest_api.repository.FollowRepository;
import com.twitter.twitter_rest_api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FollowServiceImpl implements FollowService {
    private final FollowRepository followRepository;
    private final UserRepository userRepository;
    @Override
    public UserResponse followUser(Long followingId, String followerEmail) {
        //current user yani oturum açmış kullanıcıyı bul
        User follower=userRepository.findByEmail(followerEmail)
                .orElseThrow(() -> new ApiException("Current user bulunamadı(takip edecek kullanıcı):", HttpStatus.NOT_FOUND));
        // takip edilecek kullanıcıyı bul
        User following=userRepository.findById(followingId)
                .orElseThrow(() -> new ApiException("Takip edilmek istenen kullanıcı bulunamadı!", HttpStatus.NOT_FOUND));

        if(follower.getId().equals(following.getId())){
            throw new ApiException("Kendini takip edemezsin!", HttpStatus.BAD_REQUEST);
        }
        if(followRepository.findFollowRelation(follower.getId(),following.getId()).isPresent()){
            throw new ApiException("Already following this user", HttpStatus.BAD_REQUEST);
        }

        Follow newFollow=new Follow();
        newFollow.setTakipciler(follower);
        newFollow.setTakipedilenler(following);

        follower.incrementFollowingCount(); //takip eden kullanıcının takip ettiği sayısını arttır
        following.incrementFollowersCount(); //takip edilen kullanıcının takipçi sayısını arttır

        followRepository.save(newFollow);
        userRepository.save(follower);
        userRepository.save(following);

        return new UserResponse(
                following.getId(),
                following.getFullName(),
                following.getUsername(),
                following.getEmail(),
                following.getBio(),
                following.getProfileImage(),
                following.getHeaderImage(),
                following.getFollowersCount(),
                following.getFollowingCount(),
                following.getTweetsCount(),
                following.getVerified(),
                following.getPrivateAccount()
        );
    }

    @Override
    public UserResponse unfollowUser(Long followingId, String followerEmail) {
        //current user yani oturum açmış kullanıcıyı bul
        User follower=userRepository.findByEmail(followerEmail)
                .orElseThrow(() -> new ApiException("Current user bulunamadı(takip edecek kullanıcı):", HttpStatus.NOT_FOUND));
        // takip edilen kullanıcıyı bul
        User following=userRepository.findById(followingId)
                .orElseThrow(() -> new ApiException("Takip edilmek istenen kullanıcı bulunamadı!", HttpStatus.NOT_FOUND));
        //Eğer takip ilişkisi varsa iki kullanıcı arasında bunu bul
        Follow follow=followRepository.findFollowRelation(follower.getId(),following.getId())
                .orElseThrow(()->new ApiException("Takip ilişkisi bulunamadı!",HttpStatus.NOT_FOUND));

        follower.decrementFollowingCount(); //mevcut kullanıcının takip edilenler sayısını düşür
        following.decrementFollowersCount(); // takipten çıkılan kullanıcının takipçi sayısını düşür

        followRepository.delete(follow);
        userRepository.save(follower);
        userRepository.save(following);

        return new UserResponse(
                following.getId(),
                following.getFullName(),
                following.getUsername(),
                following.getEmail(),
                following.getBio(),
                following.getProfileImage(),
                following.getHeaderImage(),
                following.getFollowersCount(),
                following.getFollowingCount(),
                following.getTweetsCount(),
                following.getVerified(),
                following.getPrivateAccount()
        );
    }

    @Override
    public Page<UserResponse> getFollowers(Long userId, Pageable pageable) {
        User targetUser=userRepository.findById(userId)
                .orElseThrow(()->new ApiException("Kullanıcı bulunamadı",HttpStatus.NOT_FOUND));
        return followRepository.findFollowersByUserId(targetUser.getId(),pageable)
                .map(user -> new UserResponse(
                        user.getId(),
                        user.getFullName(),
                        user.getUsername(),
                        user.getEmail(),
                        user.getBio(),
                        user.getProfileImage(),
                        user.getHeaderImage(),
                        user.getFollowersCount(),
                        user.getFollowingCount(),
                        user.getTweetsCount(),
                        user.getVerified(),
                        user.getPrivateAccount()
                ));
    }

    @Override
    public Page<UserResponse> getFollowing(Long userId, Pageable pageable) {
        User targetUser=userRepository.findById(userId)
                .orElseThrow(()->new ApiException("Kullanıcı bulunamadı",HttpStatus.NOT_FOUND));
        return followRepository.findFollowingsByUserId(targetUser.getId(),pageable)
                .map(user ->new UserResponse(
                        user.getId(),
                        user.getFullName(),
                        user.getUsername(),
                        user.getEmail(),
                        user.getBio(),
                        user.getProfileImage(),
                        user.getHeaderImage(),
                        user.getFollowersCount(),
                        user.getFollowingCount(),
                        user.getTweetsCount(),
                        user.getVerified(),
                        user.getPrivateAccount()
                ));
    }

    @Override
    public boolean isFollowing(Long followerId, Long followingId) {
        //1. kullanıcıyı bul
        User follower=userRepository.findById(followerId)
                .orElseThrow(() -> new ApiException("Current user bulunamadı(takip edecek kullanıcı):", HttpStatus.NOT_FOUND));
        //2.kullanıcıyı bul
        User following=userRepository.findById(followingId)
                .orElseThrow(() -> new ApiException("Takip edilmek istenen kullanıcı bulunamadı!", HttpStatus.NOT_FOUND));

        return followRepository.findFollowRelation(follower.getId(),following.getId()).isPresent();
    }
}
