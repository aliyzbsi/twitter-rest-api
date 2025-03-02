package com.twitter.twitter_rest_api.controller;

import com.twitter.twitter_rest_api.dto.*;
import com.twitter.twitter_rest_api.entity.User;
import com.twitter.twitter_rest_api.service.AuthenticationService;
import com.twitter.twitter_rest_api.service.RefreshTokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@Tag(name = "Authentication", description = "Authentication management APIs")
public class AuthController {


    @Value("${app.jwt.cookie.domain}")
    private String cookieDomain;


    private final AuthenticationService authenticationService;
    private final RefreshTokenService refreshTokenService;
    @Autowired
    public AuthController(AuthenticationService authenticationService, RefreshTokenService refreshTokenService) {
        this.authenticationService = authenticationService;
        this.refreshTokenService = refreshTokenService;
    }
    private ResponseCookie createCookie(String name,String value,int maxAge){
        return ResponseCookie.from(name,value)
                .httpOnly(true)
                .secure(true) //HTTPS için
                .sameSite("Strict")
                .domain(cookieDomain)
                .path("/")
                .maxAge(maxAge)
                .build();
    }

    @PostMapping("/login")
    @Operation(summary = "Login user", description = "Authenticates user with Basic auth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully authenticated"),
            @ApiResponse(responseCode = "401", description = "Invalid credentials")
    })
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest){
        AuthResponse authResponse=authenticationService.login(loginRequest.getEmail(),
                loginRequest.getPassword());
        ResponseCookie accessTokenCookie=createCookie(
                "accessToken",
                authResponse.accessToken(),
                30*60
        );
        ResponseCookie refreshTokenCookie=createCookie(
                "refreshToken",
                authResponse.refreshToken(),
                7*24*60*60
        );

        UserResponse userResponse = new UserResponse(
                authResponse.user().id(),
                authResponse.user().firstAndLastName(),
                authResponse.user().username(),
                authResponse.user().email(),
                authResponse.user().bio(),
                authResponse.user().profileImage(),
                authResponse.user().headerImage(),
                authResponse.user().followersCount(),
                authResponse.user().followingCount(),
                authResponse.user().tweetsCount(),
                authResponse.user().verified(),
                authResponse.user().privateAccount(),
                authResponse.user().createdAt()
        );

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, accessTokenCookie.toString())
                .header(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString())
                .body(userResponse);

    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@CookieValue(name = "refreshToken", required = false) String refreshToken){

        if(refreshToken==null){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        AuthResponse authResponse=refreshTokenService.refreshToken(refreshToken);

        // Yeni access token için cookie
        ResponseCookie accessTokenCookie = createCookie(
                "accessToken",
                authResponse.accessToken(),
                30 * 60
        );
        // Yeni refresh token için cookie
        ResponseCookie refreshTokenCookie = createCookie(
                "refreshToken",
                authResponse.refreshToken(),
                7 * 24 * 60 * 60
        );
        UserResponse userResponse = new UserResponse(
                authResponse.user().id(),
                authResponse.user().firstAndLastName(),
                authResponse.user().username(),
                authResponse.user().email(),
                authResponse.user().bio(),
                authResponse.user().profileImage(),
                authResponse.user().headerImage(),
                authResponse.user().followersCount(),
                authResponse.user().followingCount(),
                authResponse.user().tweetsCount(),
                authResponse.user().verified(),
                authResponse.user().privateAccount(),
                authResponse.user().createdAt()
        );

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, accessTokenCookie.toString())
                .header(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString())
                .body(userResponse);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @CookieValue(name = "accessToken", required = false) String accessToken,
            @CookieValue(name = "refreshToken", required = false) String refreshToken
    ) {
        // Varsa tokenları blacklist'e ekle
        if (accessToken != null) {
            refreshTokenService.blacklistToken(accessToken);
        }
        if (refreshToken != null) {
            refreshTokenService.blacklistToken(refreshToken);
        }

        // Cookie'leri sil (maxAge=0 ile)
        ResponseCookie accessTokenCookie = createCookie("accessToken", "", 0);
        ResponseCookie refreshTokenCookie = createCookie("refreshToken", "", 0);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, accessTokenCookie.toString())
                .header(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString())
                .build();
    }
    @Operation(summary = "Register new user", description = "Creates a new user account")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully registered"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "409", description = "User already exists")
    })
    @PostMapping("/register")
    public UserResponse save(@RequestBody RegisterUser registerUser) {
        User user = authenticationService.register(
                registerUser.firstName(),
                registerUser.lastName(),
                registerUser.username(),
                registerUser.email(),
                registerUser.bio(),
                registerUser.profileImage(),
                registerUser.headerImage(),
                registerUser.password(),
                registerUser.role()
        );
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
