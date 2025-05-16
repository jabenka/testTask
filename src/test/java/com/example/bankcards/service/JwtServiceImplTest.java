package com.example.bankcards.service;

import com.example.bankcards.dto.UserDto;
import com.example.bankcards.entity.UserEntity;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.service.impl.JwtServiceImpl;
import com.example.bankcards.util.mappers.UserMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.util.ReflectionTestUtils;

import java.security.Key;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private JwtServiceImpl jwtService;

    private final String SECRET_KEY = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";
    private final long EXPIRATION_TIME = 86400000;
    private final long REFRESH_EXPIRATION_TIME = 604800000;
    private final String USERNAME = "testUser";
    private final UUID USER_ID = UUID.randomUUID();

    private UserDto userDto;
    private UserEntity userEntity;
    private String validToken;
    private String expiredToken;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(jwtService, "secret", SECRET_KEY);
        ReflectionTestUtils.setField(jwtService, "expirationTime", EXPIRATION_TIME);
        ReflectionTestUtils.setField(jwtService, "refreshExpirationTime", REFRESH_EXPIRATION_TIME);

        userDto = new UserDto();
        userDto.setId(USER_ID);
        userDto.setUsername(USERNAME);

        userEntity = new UserEntity();
        userEntity.setId(USER_ID);
        userEntity.setUsername(USERNAME);

        validToken = Jwts.builder()
                .setSubject(USERNAME)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();

        expiredToken = Jwts.builder()
                .setSubject(USERNAME)
                .setIssuedAt(new Date(System.currentTimeMillis() - 2 * EXPIRATION_TIME))
                .setExpiration(new Date(System.currentTimeMillis() - EXPIRATION_TIME))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    @Test
    void extractUsername_Success() {
        String result = jwtService.extractUsername(validToken);

        assertEquals(USERNAME, result);
    }

    @Test
    void generateRefreshToken_Success() {
        String refreshToken = jwtService.generateRefreshToken(userDto);

        assertNotNull(refreshToken);
        String username = jwtService.extractUsername(refreshToken);
        assertEquals(USERNAME, username);

        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(refreshToken)
                .getBody();

        Date expiration = claims.getExpiration();
        Date issuedAt = claims.getIssuedAt();
        long duration = expiration.getTime() - issuedAt.getTime();

        assertTrue(Math.abs(duration - REFRESH_EXPIRATION_TIME) < 1000);
    }

    @Test
    void refreshAccessToken_Success() {
        when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.of(userEntity));
        when(userMapper.toDto(userEntity)).thenReturn(userDto);

        String newAccessToken = jwtService.refreshAccessToken(validToken);

        assertNotNull(newAccessToken);
        String username = jwtService.extractUsername(newAccessToken);
        assertEquals(USERNAME, username);
        verify(userRepository).findByUsername(USERNAME);
    }

    @Test
    void refreshAccessToken_ExpiredToken() {
        assertThrows(RuntimeException.class, () ->
                jwtService.refreshAccessToken(expiredToken)
        );
    }

    @Test
    void refreshAccessToken_UserNotFound() {
        when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () ->
                jwtService.refreshAccessToken(validToken)
        );
    }

    @Test
    void generateAccessToken_Success() {
        String accessToken = jwtService.generateAccessToken(userDto);

        assertNotNull(accessToken);
        String username = jwtService.extractUsername(accessToken);
        assertEquals(USERNAME, username);

        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(accessToken)
                .getBody();

        Date expiration = claims.getExpiration();
        Date issuedAt = claims.getIssuedAt();
        long duration = expiration.getTime() - issuedAt.getTime();

        assertTrue(Math.abs(duration - EXPIRATION_TIME) < 1000);
    }

    @Test
    void isTokenValid_ValidToken() {
        boolean result = jwtService.isTokenValid(validToken, userDto);

        assertTrue(result);
    }

    @Test
    void isTokenValid_ExpiredToken() {
        assertThrows(ExpiredJwtException.class, () ->
                jwtService.isTokenValid(expiredToken, userDto));
    }

    @Test
    void isTokenValid_WrongUsername() {
        UserDto anotherUser = new UserDto();
        anotherUser.setId(UUID.randomUUID());
        anotherUser.setUsername("anotherUser");

        boolean result = jwtService.isTokenValid(validToken, anotherUser);

        assertFalse(result);
    }

    @Test
    void isTokenExpired_ValidToken() {
        boolean result = jwtService.isTokenExpired(validToken);

        assertFalse(result);
    }

    @Test
    void isTokenExpired_ExpiredToken() {
        assertThrows(ExpiredJwtException.class, () ->
                jwtService.isTokenExpired(expiredToken));
    }

    @Test
    void getExpirationTime_Success() {
        Long result = jwtService.getExpirationTime();

        assertEquals(EXPIRATION_TIME, result);
    }
}
