package com.twitter.twitter_rest_api.dto;

import com.twitter.twitter_rest_api.entity.MediaType;
import com.twitter.twitter_rest_api.entity.Tweet;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;


@Data
@Schema(description = "Alıntı tweet isteği")
public class QuoteTweetRequest {

    @Schema(description = "Alıntı içeriği", example = "Bu tweet çok önemli!")
    @NotBlank(message = "Alıntı içeriği boş olamaz")
    @Size(min = 1, max = 280, message = "Alıntı 1-280 karakter arasında olmalıdır")
    private String content;

    @Schema(description = "Alıntılanan tweet ID'si", example = "1")
    @NotNull(message = "Alıntılanan tweet ID'si gereklidir")
    private Long parentTweetId;

    @Schema(description = "Medya URL'si (opsiyonel)", example = "https://example.com/image.jpg")
    private String mediaUrl;

    @Schema(description = "Medya tipi (opsiyonel)", example = "IMAGE")
    private MediaType mediaType = MediaType.NONE;
}
