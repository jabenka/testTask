package com.example.bankcards.util.mappers;

import com.example.bankcards.dto.requests.RegisterRequest;
import com.example.bankcards.dto.UserDto;
import com.example.bankcards.entity.UserEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    public UserMapper(BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }
    public UserDto toDto(UserEntity userEntity) {
       return UserDto.builder()
                .Id(userEntity.getId())
                .username(userEntity.getUsername())
                .role(userEntity.getRole())
                .build();
    }
    public UserEntity toEntity(RegisterRequest registerRequest) {
        UserEntity userEntity = new UserEntity();
        userEntity.setUsername(registerRequest.getUsername());
        userEntity.setPassword(bCryptPasswordEncoder.encode(registerRequest.getPassword()));
        userEntity.setRole(registerRequest.getRole());
        return userEntity;
    }
}
