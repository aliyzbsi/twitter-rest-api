package com.twitter.twitter_rest_api.service;

import com.twitter.twitter_rest_api.dto.AuthResponse;
import com.twitter.twitter_rest_api.dto.RoleRequest;
import com.twitter.twitter_rest_api.dto.UserResponse;
import com.twitter.twitter_rest_api.entity.Role;
import com.twitter.twitter_rest_api.entity.User;
import com.twitter.twitter_rest_api.exceptions.ApiException;
import com.twitter.twitter_rest_api.repository.RoleRepository;
import com.twitter.twitter_rest_api.repository.UserRepository;
import com.twitter.twitter_rest_api.security.JwtUtils;
import com.twitter.twitter_rest_api.validations.UserValidations;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Service
public class AuthenticationService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final AuthenticationManager authenticationManager;
    private final RefreshTokenService refreshTokenService;
    @Autowired
    public AuthenticationService(AuthenticationManager authenticationManager,
                                 UserRepository userRepository,
                                 RoleRepository roleRepository,
                                 PasswordEncoder passwordEncoder,
                                 JwtUtils jwtUtils,
                                 RefreshTokenService refreshTokenService) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
        this.refreshTokenService = refreshTokenService;
    }


    public AuthResponse login(String email,String password){
        Authentication authentication=authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email,password)
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
        UserDetails userDetails=(UserDetails) authentication.getPrincipal();

        String jwt=jwtUtils.generateAccessToken(userDetails);
        String refreshJwt=jwtUtils.generateRefreshToken(userDetails);
        User user=userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);
        return new AuthResponse(jwt,refreshJwt,convertToUserResponse(user));
    }


    public User register(String firstName, String lastName,String username, String email,String bio,
                         String profileImage,String headerImage,
                         String password, Role requestRole
                         ){

        //validations paketi altında veritabanında email varlığı kontrol edilmesi için yazılan metod
        UserValidations.emailExistCheck(userRepository,email);

        initializeRoles();


        log.info("Registering new user with email: {}",email);

        String encodedPassword=passwordEncoder.encode(password);

        Set<Role> roles=new HashSet<>();
        if (requestRole == null) {
            roles.add(getRoleByName("USER"));
        } else {
            roles.add(getRoleByName(requestRole.getAuthority()));
        }
        User user=new User();
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setUsername(username);
        user.setEmail(email);
        user.setBio(bio);
        user.setProfileImage(profileImage);
        user.setHeaderImage(headerImage);
        user.setPassword(encodedPassword);
        user.setAuthorities(roles);

        log.info("Saving user to database...");
        return userRepository.save(user);


    }

    public void logout(String accessToken,String refreshToken){
        if (accessToken != null) {
            refreshTokenService.blacklistToken(accessToken);
        }
        if (refreshToken != null) {
            refreshTokenService.blacklistToken(refreshToken);
        }
        SecurityContextHolder.clearContext();
    }

    private void initializeRoles() {
        createRoleIfNotExists("USER");
        createRoleIfNotExists("ADMIN");
    }

    private void createRoleIfNotExists(String roleName) {
        if (roleRepository.findByAuthority(roleName).isEmpty()) {
            Role role = new Role();
            role.setAuthority(roleName);
            roleRepository.save(role);
            log.info("Created role: {}", roleName);
        }
    }
    private Role getRoleByName(String roleName) {
        return roleRepository.findByAuthority(roleName)
                .orElseThrow(() -> new ApiException(
                        "Role not found: " + roleName,
                        HttpStatus.INTERNAL_SERVER_ERROR
                ));
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
                user.getPrivateAccount(),
                user.getCreatedAt()
        );
    }

}
