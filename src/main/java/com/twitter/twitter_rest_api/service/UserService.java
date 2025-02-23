package com.twitter.twitter_rest_api.service;

import com.twitter.twitter_rest_api.dto.UserResponse;
import com.twitter.twitter_rest_api.entity.User;
import com.twitter.twitter_rest_api.exceptions.ApiException;
import com.twitter.twitter_rest_api.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
@Slf4j
@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final S3Service s3Service;
    @Autowired
    public UserService(UserRepository userRepository, S3Service s3Service) {
        this.userRepository = userRepository;
        this.s3Service = s3Service;
    }


    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        log.info("Kullanıcı yükleniyor: {}", email);

        return userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.error("Kullanıcı bulunamadı: {}", email);
                    return new UsernameNotFoundException("Bu email ile kullanıcı bulunamadı: " + email);
                });
    }

    public UserResponse getCurrentUser(String email){
        log.info("Getting current user details: {}",email);
        User user=userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));

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
    public UserResponse getById(Long id){
        User user=userRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + id));
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

    public UserResponse updateProfileImage(String username, String imageUrl) {
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new ApiException("Kullanıcı bulunamadı", HttpStatus.NOT_FOUND));

        // Eski profil resmini S3'ten sil
        if (user.getProfileImage() != null) {
            s3Service.deleteFile(extractFileNameFromUrl(user.getProfileImage()));
        }

        user.setProfileImage(imageUrl);
        User updatedUser = userRepository.save(user);
        return convertToUserResponse(updatedUser);
    }



    public UserResponse updateHeaderImage(String username, String imageUrl) {
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new ApiException("Kullanıcı bulunamadı", HttpStatus.NOT_FOUND));

        // Eski kapak fotoğrafını S3'ten sil
        if (user.getHeaderImage() != null) {
            s3Service.deleteFile(extractFileNameFromUrl(user.getHeaderImage()));
        }

        user.setHeaderImage(imageUrl);
        User updatedUser = userRepository.save(user);
        return convertToUserResponse(updatedUser);
    }



    private String extractFileNameFromUrl(String profileImage) {
        return profileImage.substring(profileImage.lastIndexOf("/") + 1);
    }
    private UserResponse convertToUserResponse(User user) {
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

