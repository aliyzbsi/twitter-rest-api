package com.twitter.twitter_rest_api.dto;

import com.twitter.twitter_rest_api.entity.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterUser(@Schema(description = "User's first name", example = "John")
                           @NotBlank(message = "First name is required")
                           String firstName,

                           @Schema(description = "User's last name", example = "Doe")
                           @NotBlank(message = "Last name is required")
                           String lastName,

                           @Schema(description = "User's username", example = "johndoe")
                           @NotBlank(message = "Username is required")
                           @Size(min = 1, max = 15, message = "Username must be between 1 and 15 characters")
                           String username,  // Bu alan eksikti

                           @Schema(description = "User's email address", example = "john.doe@example.com")
                           @NotBlank(message = "Email is required")
                           @Email(message = "Invalid email format")
                           String email,
                           @Schema(description = "User's bio", example = "açıklama")
                           String bio,
                           @Schema(description = "User's profile image", example = "profil resmi")
                           String profileImage,
                           @Schema(description = "User's header image", example = "kapak resmi")
                           String headerImage,
                           @Schema(description = "User's password", example = "password123")
                           @NotBlank(message = "Password is required")
                           @Size(min = 6, message = "Password must be at least 6 characters")
                           String password,

                           @Schema(description = "User's role")
                           Role role) {
}
