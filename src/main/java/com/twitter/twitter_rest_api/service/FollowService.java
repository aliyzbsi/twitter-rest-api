package com.twitter.twitter_rest_api.service;

import com.twitter.twitter_rest_api.dto.UserResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface FollowService {
    UserResponse followUser(Long followingId,String followerEmail);
    UserResponse unfollowUser(Long followingId,String followerEmail);
    Page<UserResponse> getFollowers(Long userId, Pageable pageable);
    Page<UserResponse> getFollowing(Long userId,Pageable pageable);
    boolean isFollowing(Long followerId,Long followingId);
}
