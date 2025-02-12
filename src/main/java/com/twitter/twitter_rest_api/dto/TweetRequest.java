package com.twitter.twitter_rest_api.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.twitter.twitter_rest_api.entity.MediaType;
import com.twitter.twitter_rest_api.entity.TweetType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "Tweet oluşturma isteği için gerekli bilgiler")
public class TweetRequest {
    @Schema(description = "Tweet içeriği", example = "Merhaba dünya!")
    @NotBlank(message = "Tweet içeriği boş olamaz")
    @Size(min = 1, max = 280, message = "Tweet 1-280 karakter arasında olmalıdır")
    private String content;

    @Schema(description = "Medya URL'si (opsiyonel)", example = "https://example.com/image.jpg")
    private String mediaUrl;

    @Schema(description = "Medya tipi (opsiyonel)", example = "IMAGE")
    private MediaType mediaType = MediaType.NONE;


}
