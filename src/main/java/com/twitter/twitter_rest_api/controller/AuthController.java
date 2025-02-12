package com.twitter.twitter_rest_api.controller;

import com.twitter.twitter_rest_api.dto.RegisterUser;
import com.twitter.twitter_rest_api.dto.UserResponse;
import com.twitter.twitter_rest_api.entity.User;
import com.twitter.twitter_rest_api.service.AuthenticationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@Tag(name = "Authentication", description = "Authentication management APIs")
public class AuthController {

    private final AuthenticationService authenticationService;

    @Autowired
    public AuthController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @PostMapping("/login")
    @Operation(summary = "Login user", description = "Authenticates user with Basic auth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully authenticated"),
            @ApiResponse(responseCode = "401", description = "Invalid credentials")
    })
    public UserResponse login(@AuthenticationPrincipal UserDetails userDetails){
        return authenticationService.login(userDetails.getUsername());
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
                user.getPrivateAccount()
        );
    }
}
