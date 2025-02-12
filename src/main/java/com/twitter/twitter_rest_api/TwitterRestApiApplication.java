package com.twitter.twitter_rest_api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.CrossOrigin;

@SpringBootApplication
@CrossOrigin("/**")
public class TwitterRestApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(TwitterRestApiApplication.class, args);
	}

}
