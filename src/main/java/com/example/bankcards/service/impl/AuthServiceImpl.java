package com.example.bankcards.service.impl;

import com.example.bankcards.dto.requests.LoginRequest;
import com.example.bankcards.dto.responses.LoginResponse;
import com.example.bankcards.dto.requests.RegisterRequest;
import com.example.bankcards.dto.UserDto;
import com.example.bankcards.entity.UserEntity;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.service.AuthService;
import com.example.bankcards.service.JwtService;
import com.example.bankcards.util.mappers.UserMapper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AuthServiceImpl implements AuthService {


    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final AuthenticationManager authenticationManager;
    private final UserMapper userMapper;
    private final JwtService jwtService;

    public AuthServiceImpl(UserRepository userRepository,
                           BCryptPasswordEncoder bCryptPasswordEncoder,
                           AuthenticationManager authenticationManager,
                           UserMapper userMapper, JwtService jwtService) {
        this.userRepository = userRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.authenticationManager = authenticationManager;
        this.userMapper = userMapper;
        this.jwtService = jwtService;
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        String username = request.getUsername();
        String password = request.getPassword();
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        username,
                        password
                )
        );
        UserEntity user = userRepository.findByUsername(username).stream().findFirst()
                .orElseThrow(() -> new UsernameNotFoundException(String.format("User %s not found", username)));
        if (!bCryptPasswordEncoder.matches(password, user.getPassword())) {
            throw new IllegalArgumentException(String.format("Wrong password provided: %s", password));
        }
        return buildLoginResponse(userMapper.toDto(user));
    }

    @Override
    public UserDto register(RegisterRequest request) {
        String username = request.getUsername();
        if (userRepository.findByUsername(username).isPresent()) {
            throw new IllegalArgumentException(String.format("User %s already exists", username));
        }
        UserEntity newUser = userMapper.toEntity(request);
        newUser = userRepository.saveAndFlush(newUser);
        return userMapper.toDto(newUser);
    }

    @Override
    public LoginResponse refresh(String refreshToken) {
        String accessToken = jwtService.refreshAccessToken(refreshToken);
        Long expiresIn = jwtService.getExpirationTime();
        return new LoginResponse(accessToken, refreshToken, expiresIn);
    }

    private LoginResponse buildLoginResponse(UserDto user) {
        String token = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);
        Long expiresIn = jwtService.getExpirationTime();
        return new LoginResponse(token, refreshToken, expiresIn);
    }
}
