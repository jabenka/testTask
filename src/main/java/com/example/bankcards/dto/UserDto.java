package com.example.bankcards.dto;

import com.example.bankcards.entity.enums.Role;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserDto {

    @JsonProperty("id")
    UUID Id;

    @JsonProperty("username")
    String username;

    @JsonProperty("role")
    Role role;

}
