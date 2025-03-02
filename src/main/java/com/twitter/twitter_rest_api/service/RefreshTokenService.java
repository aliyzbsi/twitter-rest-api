package com.twitter.twitter_rest_api.service;

import com.twitter.twitter_rest_api.dto.AuthResponse;
import com.twitter.twitter_rest_api.dto.UserResponse;
import com.twitter.twitter_rest_api.entity.User;
import com.twitter.twitter_rest_api.exceptions.ApiException;
import com.twitter.twitter_rest_api.security.JwtUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Slf4j
@Service
public class RefreshTokenService {
    private final Set<String> blacklistedTokens = new HashSet<>();
    private final JwtUtils jwtUtils;
    private final UserService userService;

    public RefreshTokenService(JwtUtils jwtUtils, UserService userService) {
        this.jwtUtils = jwtUtils;
        this.userService = userService;
    }
    public AuthResponse refreshToken(String refreshToken){
        if(isTokeBlackListed(refreshToken)){
            throw new ApiException("Invalid refresh token", HttpStatus.UNAUTHORIZED);
        }
        if (!jwtUtils.validateJwtToken(refreshToken) || !jwtUtils.isRefreshToken(refreshToken)) {
            throw new ApiException("Invalid refresh token", HttpStatus.UNAUTHORIZED);
        }
        String username = jwtUtils.getUserNameFromJwtToken(refreshToken);
        User user = (User) userService.loadUserByUsername(username);

        // Eski refresh token'ı blacklist'e ekle
        blacklistToken(refreshToken);

        // Yeni tokenları oluştur
        String newAccessToken = jwtUtils.generateAccessToken(user);
        String newRefreshToken = jwtUtils.generateRefreshToken(user);

        return new AuthResponse(
                newAccessToken,
                newRefreshToken,
                convertToUserResponse(user)
        );
    }

    public void blacklistToken(String token) {
        blacklistedTokens.add(token);
    }

    public boolean isTokeBlackListed(String token) {
        return blacklistedTokens.contains(token);
    }
    private UserResponse convertToUserResponse(com.twitter.twitter_rest_api.entity.User user) {
        return new UserResponse(
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
                user.getPrivateAccount(),
                user.getCreatedAt()
        );
    }
}
