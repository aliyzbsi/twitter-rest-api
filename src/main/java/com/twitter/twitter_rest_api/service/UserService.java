package com.twitter.twitter_rest_api.service;

import com.twitter.twitter_rest_api.dto.UserResponse;
import com.twitter.twitter_rest_api.entity.User;
import com.twitter.twitter_rest_api.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
@Slf4j
@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
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
}

