package com.example.bankcards.service;

import com.example.bankcards.dto.requests.LoginRequest;
import com.example.bankcards.dto.responses.LoginResponse;
import com.example.bankcards.dto.requests.RegisterRequest;
import com.example.bankcards.dto.UserDto;

public interface AuthService {
    LoginResponse login(LoginRequest request);

    UserDto register(RegisterRequest request);

    LoginResponse refresh(String refreshToken);
}
