package com.twitter.twitter_rest_api.validations;

import com.twitter.twitter_rest_api.exceptions.ApiException;
import com.twitter.twitter_rest_api.repository.UserRepository;
import org.springframework.http.HttpStatus;

public class UserValidations {
    public static void emailExistCheck(UserRepository userRepository, String email) {
        if (userRepository.findByEmail(email).isPresent()){
            throw new RuntimeException("Email already exist: "+email);
        }
    }
}
