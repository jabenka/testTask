package com.example.bankcards.dto.responses;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LoginResponse {
    @JsonProperty("token")
    String token;
    @JsonProperty("refresh_token")
    String refreshToken;
    @JsonProperty("expires_in")
    Long expiresIn;
}
