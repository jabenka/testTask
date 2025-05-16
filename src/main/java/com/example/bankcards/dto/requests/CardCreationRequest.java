package com.example.bankcards.dto.requests;

import com.example.bankcards.entity.enums.CardStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
public class CardCreationRequest {
    @JsonProperty("card_number")
    @NotBlank(message = "card number cant be empty")
    @Size(min = 16, max = 16)
    String cardNumber;
    @JsonProperty("owner_id")
    @NotNull(message = "card cant be created without owner")
    UUID ownerId;
    @JsonProperty("expires_in")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    @NotNull(message = "expiration date must not be null")
    LocalDate expiresIn;
    @JsonProperty("status")
    @NotNull(message = "status must not be null")
    CardStatus status;
    @JsonProperty("start_balance")
    BigDecimal startBalance;
}