package com.example.bankcards.service;

import com.example.bankcards.dto.UserDto;
import com.example.bankcards.dto.requests.LoginRequest;
import com.example.bankcards.dto.requests.RegisterRequest;
import com.example.bankcards.dto.responses.LoginResponse;
import com.example.bankcards.entity.UserEntity;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.service.impl.AuthServiceImpl;
import com.example.bankcards.util.mappers.UserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserMapper userMapper;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthServiceImpl authService;

    private static final String USERNAME = "testUser";
    private static final  String PASSWORD = "password123";
    private static final  String ENCODED_PASSWORD = "encodedPassword";
    private static final String ACCESS_TOKEN = "access.token.jwt";
    private static final  String REFRESH_TOKEN = "refresh.token.jwt";
    private static final  Long EXPIRES_IN = 3600L;
    private static final UUID USER_ID = UUID.randomUUID();

    private UserEntity userEntity;
    private UserDto userDto;
    private LoginRequest loginRequest;
    private RegisterRequest registerRequest;

    @BeforeEach
    void setUp() {
        userEntity = new UserEntity();
        userEntity.setId(USER_ID);
        userEntity.setUsername(USERNAME);
        userEntity.setPassword(ENCODED_PASSWORD);

        userDto = new UserDto();
        userDto.setId(USER_ID);
        userDto.setUsername(USERNAME);

        loginRequest = new LoginRequest();
        loginRequest.setUsername(USERNAME);
        loginRequest.setPassword(PASSWORD);

        registerRequest = new RegisterRequest();
        registerRequest.setUsername(USERNAME);
        registerRequest.setPassword(PASSWORD);
    }

    @Test
    void login_Success() {
        when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.of(userEntity));
        when(passwordEncoder.matches(PASSWORD, ENCODED_PASSWORD)).thenReturn(true);
        when(userMapper.toDto(userEntity)).thenReturn(userDto);
        when(jwtService.generateAccessToken(userDto)).thenReturn(ACCESS_TOKEN);
        when(jwtService.generateRefreshToken(userDto)).thenReturn(REFRESH_TOKEN);
        when(jwtService.getExpirationTime()).thenReturn(EXPIRES_IN);

        LoginResponse response = authService.login(loginRequest);

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        assertEquals(ACCESS_TOKEN, response.getToken());
        assertEquals(REFRESH_TOKEN, response.getRefreshToken());
        assertEquals(EXPIRES_IN, response.getExpiresIn());
    }

    @Test
    void login_UserNotFound() {
        when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> authService.login(loginRequest));
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    void login_WrongPassword() {
        when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.of(userEntity));
        when(passwordEncoder.matches(PASSWORD, ENCODED_PASSWORD)).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> authService.login(loginRequest));
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    void register_Success() {
        when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.empty());
        when(userMapper.toEntity(registerRequest)).thenReturn(userEntity);
        when(userRepository.saveAndFlush(userEntity)).thenReturn(userEntity);
        when(userMapper.toDto(userEntity)).thenReturn(userDto);

        UserDto result = authService.register(registerRequest);

        assertEquals(userDto, result);
        verify(userRepository).saveAndFlush(userEntity);
    }

    @Test
    void register_UserAlreadyExists() {
        when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.of(userEntity));

        assertThrows(IllegalArgumentException.class, () -> authService.register(registerRequest));
        verify(userRepository, never()).saveAndFlush(any());
    }

    @Test
    void refresh_Success() {
        when(jwtService.refreshAccessToken(REFRESH_TOKEN)).thenReturn(ACCESS_TOKEN);
        when(jwtService.getExpirationTime()).thenReturn(EXPIRES_IN);

        LoginResponse response = authService.refresh(REFRESH_TOKEN);

        assertEquals(ACCESS_TOKEN, response.getToken());
        assertEquals(REFRESH_TOKEN, response.getRefreshToken());
        assertEquals(EXPIRES_IN, response.getExpiresIn());
        verify(jwtService).refreshAccessToken(REFRESH_TOKEN);
        verify(jwtService).getExpirationTime();
    }
}
