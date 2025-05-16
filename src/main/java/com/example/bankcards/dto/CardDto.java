package com.example.bankcards.dto;

import com.example.bankcards.entity.enums.CardStatus;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CardDto {
    @JsonProperty("id")
    UUID Id;
    @JsonProperty("card_number")
    String cardNumber;
    @JsonProperty("last_four_card_digits")
    String lastFourCardDigits;
    @JsonProperty("owner_id")
    UUID ownerId;
    @JsonProperty("expires_in")
    LocalDate expiresIn;
    @JsonProperty("status")
    CardStatus status;
    @JsonProperty("balance")
    BigDecimal balance;
}
