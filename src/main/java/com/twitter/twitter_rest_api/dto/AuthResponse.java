package com.twitter.twitter_rest_api.dto;

public record AuthResponse(
        String accessToken,
        String refreshToken,
        UserResponse user
) {}
