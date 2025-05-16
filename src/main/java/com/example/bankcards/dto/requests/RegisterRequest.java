package com.example.bankcards.dto.requests;

import com.example.bankcards.entity.enums.Role;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RegisterRequest {
    @JsonProperty("username")
    @NotEmpty
    @Size(min = 3, max = 100)
    String username;
    @JsonProperty("password")
    @NotEmpty
    @Size(min = 3, max = 100)
    String password;
    @JsonProperty("role")
    @NotNull(message = "Role cannot be null")
    Role role;
}
