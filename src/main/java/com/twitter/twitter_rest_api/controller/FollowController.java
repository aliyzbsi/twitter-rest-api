package com.twitter.twitter_rest_api.controller;

import com.twitter.twitter_rest_api.dto.UserResponse;
import com.twitter.twitter_rest_api.entity.User;
import com.twitter.twitter_rest_api.exceptions.ApiException;
import com.twitter.twitter_rest_api.repository.UserRepository;
import com.twitter.twitter_rest_api.service.FollowService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/follow")
@RequiredArgsConstructor
public class FollowController {

    private final FollowService followService;
    private final UserRepository userRepository;
    @PostMapping("/{followingId}")
    @Operation(summary = "Kullanıcıyı takip et")
    public ResponseEntity<UserResponse> followUser(
            @PathVariable("followingId") Long followingId,
            @AuthenticationPrincipal UserDetails userDetails
            ){
        return ResponseEntity.ok(followService.followUser(followingId, userDetails.getUsername()));
    }
    @DeleteMapping("/{followingId}")
    @Operation(summary = "Kullanıcıyı takipten çık")
    public ResponseEntity<UserResponse> unfollowUser(
            @PathVariable("followingId") Long followingId,
            @AuthenticationPrincipal UserDetails userDetails
    ){
        return ResponseEntity.ok(followService.unfollowUser(followingId,userDetails.getUsername()));
    }
    @GetMapping("/{userId}/followers")
    @Operation(summary = "Kullanıcının takipçileri")
    public ResponseEntity<Page<UserResponse>> getFollowers(@PathVariable("userId")Long userId,
                                                           @PageableDefault(size = 20)Pageable pageable){
        return ResponseEntity.ok(followService.getFollowers(userId,pageable));
    }
    @GetMapping("/{userId}/following")
    @Operation(summary = "Kullanıcının takip ettikleri")
    public ResponseEntity<Page<UserResponse>> getFollowing(@PathVariable("userId")Long userId,
                                                           @PageableDefault(size = 20)Pageable pageable){
        return ResponseEntity.ok(followService.getFollowing(userId,pageable));
    }
    @GetMapping("/check/{followingId}")
    @Operation(summary = "Aktif kullanıcı diğer kullanıcıyı takip ediyor mu kontrolü")
    public ResponseEntity<Boolean> checkFollowStatus(@PathVariable("followingId") Long followingId,
                                                     @AuthenticationPrincipal UserDetails userDetails){
        User currentUser=userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ApiException("Aktif kullanıcı bulunamadı!", HttpStatus.NOT_FOUND));
        return ResponseEntity.ok(followService.isFollowing(currentUser.getId(),followingId));
    }
}
