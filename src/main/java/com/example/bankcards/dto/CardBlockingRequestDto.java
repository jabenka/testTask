package com.example.bankcards.dto;

import com.example.bankcards.entity.enums.BlockRequestStatus;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CardBlockingRequestDto {
    @JsonProperty("id")
    UUID Id;
    @JsonProperty("last_four_card_digits")
    String last_four_card_digits;
    @JsonProperty("user_id")
    UUID userId;
    @JsonProperty("admin_id")
    UUID adminId;
    @JsonProperty("status")
    BlockRequestStatus status;
    @JsonProperty("created_at")
    LocalDate createdAt;
    @JsonProperty("updated_at")
    LocalDate updatedAt;
}
