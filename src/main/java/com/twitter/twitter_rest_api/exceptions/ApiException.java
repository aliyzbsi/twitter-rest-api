package com.twitter.twitter_rest_api.exceptions;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@Schema(description = "API özel istisna sınıfı")
public class ApiException extends RuntimeException {
    @Schema(description = "HTTP durum kodu")
    private final HttpStatus httpStatus;

    public ApiException(String message, HttpStatus httpStatus) {
        super(message);
        this.httpStatus = httpStatus;
    }

}