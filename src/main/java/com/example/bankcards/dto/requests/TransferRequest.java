package com.example.bankcards.dto.requests;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Positive;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TransferRequest {
    @JsonProperty("source")
    String sourceLastFourCardDigits;
    @JsonProperty("target")
    String targetLastFourCardDigits;
    @JsonProperty("amount")
    @Positive
    BigDecimal amount;
}
