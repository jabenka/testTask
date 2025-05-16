package com.example.bankcards.dto.requests;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class LoginRequest {
    @JsonProperty("username")
    @NotEmpty
    @Size(min = 3, max = 100)
    String username;
    @JsonProperty("password")
    @NotEmpty
    @Size(min = 3, max = 100)
    String password;
}
