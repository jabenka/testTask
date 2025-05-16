package com.example.bankcards.service.impl;

import com.example.bankcards.dto.UserDto;
import com.example.bankcards.entity.UserEntity;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.service.JwtService;
import com.example.bankcards.util.mappers.UserMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtServiceImpl implements JwtService {
    @Value("${jwt.secret}")
    private String secret;
    @Value("${jwt.expiration-time}")
    private long expirationTime;
    @Value("${jwt.refresh-expiration-time}")
    private long refreshExpirationTime;

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public JwtServiceImpl(UserRepository userRepository, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }


    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public String generateRefreshToken(UserDto userDetails) {
        return buildToken(new HashMap<>(), userDetails, refreshExpirationTime);
    }

    public String refreshAccessToken(String refreshToken) {
        if (isTokenExpired(refreshToken)) {
            throw new RuntimeException("Refresh token expired");
        }

        String username = extractUsername(refreshToken);
        UserEntity userDetails = userRepository.findByUsername(username).stream().findFirst()
                .orElseThrow(() -> new UsernameNotFoundException(
                        String.format("User with username %s not found", username)
                ));
        userDetails.setUsername(username);

        return generateAccessToken(userMapper.toDto(userDetails));
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);

    }

    public String generateAccessToken(UserDto userDetails) {
        return generateAccessToken(new HashMap<>(), userDetails);
    }

    public String generateAccessToken(Map<String, Object> extraClaims, UserDto userDetails) {
        return buildToken(extraClaims, userDetails, expirationTime);
    }

    private String buildToken(
            Map<String, Object> extraClaims,
            UserDto userDetails,
            long expiration
    ) {
        return Jwts
                .builder()
                .setClaims(extraClaims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }


    public Boolean isTokenValid(String token, UserDto userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    public Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public Long getExpirationTime() {
        return expirationTime;
    }

    private Claims extractAllClaims(String token) {
        return Jwts
                .parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
