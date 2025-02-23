package com.twitter.twitter_rest_api.controller;

import com.twitter.twitter_rest_api.dto.UserResponse;
import com.twitter.twitter_rest_api.exceptions.ApiException;
import com.twitter.twitter_rest_api.service.S3Service;
import com.twitter.twitter_rest_api.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/user")
@Tag(name = "User", description = "User management APIs")
public class UserController {
    private final UserService userService;
    private final S3Service s3Service;
    @Autowired
    public UserController(UserService userService, S3Service s3Service) {
        this.userService = userService;
        this.s3Service = s3Service;
    }


    @GetMapping("/me")
    @Operation(
            summary = "Get current user",
            description = "Returns the authenticated user's details. Requires JWT token."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved user details"),
            @ApiResponse(responseCode = "401", description = "Not authenticated"),
            @ApiResponse(responseCode = "403", description = "Invalid or missing JWT token")
    })
    public UserResponse getCurrentUser(@AuthenticationPrincipal UserDetails userDetails) {
        return userService.getCurrentUser(userDetails.getUsername());
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Get user by ID",
            description = "Returns user details by ID. Requires JWT token."
    )
    public UserResponse getUserById(@PathVariable("id") Long id) {
        return userService.getById(id);
    }
    @PutMapping("/profile-image")
    public ResponseEntity<UserResponse> updateProfileImage(@RequestParam("file")MultipartFile file,
                                                           @AuthenticationPrincipal UserDetails userDetails){
        try {
            String imageUrl=s3Service.uploadFile(file);
            UserResponse updatedUser=userService.updateProfileImage(userDetails.getUsername(),imageUrl);
            return ResponseEntity.ok(updatedUser);
        }catch (Exception e){
            throw new ApiException("Profil resmi yüklenirken hata oluştu: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @PutMapping("/header-image")
    public ResponseEntity<UserResponse> updateHeaderImage(
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            String imageUrl = s3Service.uploadFile(file);
            UserResponse updatedUser = userService.updateHeaderImage(userDetails.getUsername(), imageUrl);
            return ResponseEntity.ok(updatedUser);
        } catch (Exception e) {
            throw new ApiException("Kapak fotoğrafı yüklenirken hata oluştu: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
