package com.twitter.twitter_rest_api.controller;

import com.twitter.twitter_rest_api.dto.UserResponse;
import com.twitter.twitter_rest_api.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
@Tag(name = "User", description = "User management APIs")
public class UserController {
    private final UserService userService;
    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }
    @GetMapping("/me")
    @Operation(summary = "Get current user", description = "Returns the authenticated user's details")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved user details"),
            @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    public UserResponse getCurrentUser(@AuthenticationPrincipal UserDetails userDetails){
        return userService.getCurrentUser(userDetails.getUsername());
    }

    @GetMapping("/{id}")
    public UserResponse getUserById(@PathVariable("id") Long id){
        return userService.getById(id);
    }

}
