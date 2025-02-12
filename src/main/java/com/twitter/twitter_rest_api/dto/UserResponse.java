package com.twitter.twitter_rest_api.dto;

public record UserResponse(Long id,
                           String firstAndLastName,
                           String username,
                           String email,
                           String bio,
                           String profileImage,
                           String headerImage,
                           Integer followersCount,
                           Integer followingCount,
                           Integer tweetsCount,
                           Boolean verified,
                           Boolean privateAccount) {
}
