package com.example.bankcards.controller;

import com.example.bankcards.dto.requests.LoginRequest;
import com.example.bankcards.dto.responses.LoginResponse;
import com.example.bankcards.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AuthControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(authController).build();
    }

    @Test
    void login_WithValidCredentials_ShouldReturnToken() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setUsername("testuser");
        request.setPassword("password");

        LoginResponse expectedResponse = new LoginResponse();
        expectedResponse.setToken("access-token");
        expectedResponse.setRefreshToken("refresh-token");

        when(authService.login(any(LoginRequest.class))).thenReturn(expectedResponse);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("access-token"))
                .andExpect(jsonPath("$.refresh_token").value("refresh-token"));
    }

    @Test
    void login_WithInvalidCredentials_ShouldReturnBadRequest() throws Exception {
        LoginRequest invalidRequest = new LoginRequest();

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void refresh_WithValidToken_ShouldReturnNewTokens() throws Exception {
        String refreshToken = "valid-refresh-token";

        LoginResponse expectedResponse = new LoginResponse();
        expectedResponse.setToken("new-access-token");
        expectedResponse.setRefreshToken("new-refresh-token");

        when(authService.refresh(refreshToken)).thenReturn(expectedResponse);

        mockMvc.perform(post("/auth/refresh")
                        .param("refreshToken", refreshToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("new-access-token"))
                .andExpect(jsonPath("$.refresh_token").value("new-refresh-token"));
    }

    @Test
    void refresh_WithEmptyToken_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(post("/auth/refresh")
                        .param("refresh_token", ""))
                .andExpect(status().isBadRequest());
    }

    @Test
    void refresh_WithoutToken_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(post("/auth/refresh"))
                .andExpect(status().isBadRequest());
    }
}