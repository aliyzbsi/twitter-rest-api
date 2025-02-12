package com.twitter.twitter_rest_api.dto;

import com.twitter.twitter_rest_api.entity.MediaType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "Tweet'e yanıt verme isteği")
public class ReplyTweetRequest {
    @Schema(description = "Yanıt içeriği", example = "Katılıyorum!")
    @NotBlank(message = "Yanıt içeriği boş olamaz")
    @Size(min = 1, max = 280, message = "Yanıt 1-280 karakter arasında olmalıdır")
    private String content;

    @Schema(description = "Yanıt verilen tweet ID'si", example = "1")
    @NotNull(message = "Yanıt verilen tweet ID'si gereklidir")
    private Long parentTweetId;

    @Schema(description = "Medya URL'si (opsiyonel)", example = "https://example.com/image.jpg")
    private String mediaUrl;

    @Schema(description = "Medya tipi (opsiyonel)", example = "IMAGE")
    private MediaType mediaType = MediaType.NONE;
}
