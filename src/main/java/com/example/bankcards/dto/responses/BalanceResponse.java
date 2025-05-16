package com.example.bankcards.dto.responses;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BalanceResponse {
    BigDecimal balance;
    String lastFourCardDigits;
}
