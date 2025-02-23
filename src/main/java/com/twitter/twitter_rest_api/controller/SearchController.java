// SearchController.java
package com.twitter.twitter_rest_api.controller;

import com.twitter.twitter_rest_api.dto.SearchResponse;
import com.twitter.twitter_rest_api.dto.TweetResponse;
import com.twitter.twitter_rest_api.dto.UserResponse;
import com.twitter.twitter_rest_api.service.SearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/search")
@RequiredArgsConstructor
@Slf4j
public class SearchController {
    private final SearchService searchService;

    @GetMapping
    public ResponseEntity<?> search(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication) {

        String username = authentication.getName(); // Principal'dan username/email al
        log.debug("Authenticated user: {}", username);

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(searchService.search(query, pageable, username));
    }

    @GetMapping("/hashtags/{hashtag}")
    public ResponseEntity<Page<TweetResponse>> searchByHashtag(
            @PathVariable String hashtag,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication) {

        String username = authentication.getName();
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(searchService.findByHashtag(hashtag, pageable, username));
    }
}