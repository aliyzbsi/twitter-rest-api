package com.twitter.twitter_rest_api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig {
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**") // Tüm endpoint'lere izin ver
                        .allowedOrigins("http://localhost:5174") // Frontend URL'ini buraya ekle
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // İzin verilen HTTP metodları
                        .allowedHeaders("*") // Tüm başlıklara izin ver
                        .allowCredentials(true); // Kimlik doğrulama bilgilerini içeren isteklere izin ver
            }
        };
    }
}
