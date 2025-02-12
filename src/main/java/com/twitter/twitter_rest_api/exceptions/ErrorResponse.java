package com.twitter.twitter_rest_api.exceptions;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "Hata yanıt modeli")
@NoArgsConstructor
@Data
public class ErrorResponse {
    @Schema(description = "Hata mesajı")
    private String message;

    @Schema(description = "HTTP durum kodu")
    private int status;

    public ErrorResponse(String message, int status) {
        this.message = message;
        this.status = status;
    }
}