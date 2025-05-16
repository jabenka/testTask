package com.example.bankcards.service;

import com.example.bankcards.dto.UserDto;
import io.jsonwebtoken.Claims;

import java.util.Map;
import java.util.function.Function;

public interface JwtService {

    String extractUsername(String token);

    <T> T extractClaim(String token, Function<Claims, T> claimsResolver);

    String generateAccessToken(UserDto userDetails);

    String generateAccessToken(Map<String, Object> extraClaims, UserDto userDetails);

    Boolean isTokenValid(String token, UserDto userDetails);

    Boolean isTokenExpired(String token);

    String refreshAccessToken(String refreshToken);

    String generateRefreshToken(UserDto userDetails);

    Long getExpirationTime();
}
