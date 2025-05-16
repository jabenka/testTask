package com.example.bankcards.dto.responses;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TransferResponse {
    @JsonProperty("source")
    String sourceLastFourCardDigits;
    @JsonProperty("target")
    String targetLastFourCardDigits;
    @JsonProperty("amount")
    BigDecimal amount;
}