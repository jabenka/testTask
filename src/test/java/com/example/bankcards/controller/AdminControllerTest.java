package com.example.bankcards.controller;

import com.example.bankcards.dto.CardBlockingRequestDto;
import com.example.bankcards.dto.CardDto;
import com.example.bankcards.dto.UserDto;
import com.example.bankcards.dto.requests.CardCreationRequest;
import com.example.bankcards.dto.requests.RegisterRequest;
import com.example.bankcards.entity.enums.BlockRequestStatus;
import com.example.bankcards.entity.enums.CardStatus;
import com.example.bankcards.entity.enums.Role;
import com.example.bankcards.service.AuthService;
import com.example.bankcards.service.CardBlockingService;
import com.example.bankcards.service.CardService;
import com.example.bankcards.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.*;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AdminControllerTest {

    private static final UUID TEST_USER_ID = UUID.randomUUID();
    private static final UUID TEST_ADMIN_ID = UUID.randomUUID();
    private static final LocalDate TEST_EXPIRY_DATE = LocalDate.of(2027, 1, 1);
    private static final String TEST_CARD_NUMBER = "1234123412341234";
    private static final String TEST_LAST_FOUR_CARD_DIGITS = "1234";
    private static final UUID TEST_CARD_BLOCKING_REQUEST_ID = UUID.randomUUID();

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private CardService cardService;

    @Mock
    private UserService userService;

    @Mock
    private AuthService authService;

    @Mock
    private CardBlockingService cardBlockingService;

    @InjectMocks
    private AdminController adminController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        objectMapper.registerModule(new JavaTimeModule());
        mockMvc = MockMvcBuilders.standaloneSetup(adminController)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();
    }

    @Test
    void createCard_ShouldReturnOk() throws Exception {
        CardCreationRequest request = createCardCreationRequest();
        CardDto expectedResponse = createCardDtoFromRequest(request);
        when(cardService.createCard(any(CardCreationRequest.class))).thenReturn(expectedResponse);

        mockMvc.perform(post("/admin/cards/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(expectedResponse.getId().toString()))
                .andExpect(jsonPath("$.card_number").value(expectedResponse.getCardNumber()))
                .andExpect(jsonPath("$.last_four_card_digits").value(expectedResponse.getLastFourCardDigits()))
                .andExpect(jsonPath("$.owner_id").value(expectedResponse.getOwnerId().toString()))
                .andExpect(jsonPath("$.expires_in[0]").value(2027))
                .andExpect(jsonPath("$.expires_in[1]").value(1))
                .andExpect(jsonPath("$.expires_in[2]").value(1))
                .andExpect(jsonPath("$.status").value(expectedResponse.getStatus().toString()))
                .andExpect(jsonPath("$.balance").value(expectedResponse.getBalance().doubleValue()));

        verify(cardService, times(1)).createCard(any(CardCreationRequest.class));
    }

    private CardDto createCardDtoFromRequest(CardCreationRequest request) {
        return CardDto.builder()
                .Id(UUID.randomUUID())
                .cardNumber(TEST_CARD_NUMBER)
                .lastFourCardDigits(TEST_LAST_FOUR_CARD_DIGITS)
                .ownerId(TEST_USER_ID)
                .expiresIn(TEST_EXPIRY_DATE)
                .status(CardStatus.ACTIVE)
                .balance(BigDecimal.ZERO)
                .build();
    }

    private CardCreationRequest createCardCreationRequest() {
        return CardCreationRequest.builder()
                .cardNumber(TEST_CARD_NUMBER)
                .ownerId(TEST_USER_ID)
                .expiresIn(TEST_EXPIRY_DATE)
                .status(CardStatus.ACTIVE)
                .startBalance(BigDecimal.ZERO)
                .build();
    }

    @Test
    void createCard_WithInvalidRequest_ShouldReturnBadRequest() throws Exception {
        CardCreationRequest request = new CardCreationRequest();

        mockMvc.perform(post("/admin/cards/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(cardService, never()).createCard(any());
    }

    @Test
    void updateCard_ShouldReturnOk() throws Exception {
        String lastFourDigits = "1234";
        String activate = "deactivate";
        CardDto expectedResponse = createCardDtoFromRequest(createCardCreationRequest());
        expectedResponse.setLastFourCardDigits(lastFourDigits);
        expectedResponse.setStatus(CardStatus.BLOCKED);

        when(cardService.updateCard(lastFourDigits, activate)).thenReturn(expectedResponse);

        mockMvc.perform(patch("/admin/cards/update")
                        .param("last_four_card_digits", lastFourDigits)
                        .param("activate", activate))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(expectedResponse.getId().toString()))
                .andExpect(jsonPath("$.card_number").value(expectedResponse.getCardNumber()))
                .andExpect(jsonPath("$.last_four_card_digits").value(expectedResponse.getLastFourCardDigits()))
                .andExpect(jsonPath("$.owner_id").value(expectedResponse.getOwnerId().toString()))
                .andExpect(jsonPath("$.expires_in[0]").value(2027))
                .andExpect(jsonPath("$.expires_in[1]").value(1))
                .andExpect(jsonPath("$.expires_in[2]").value(1))
                .andExpect(jsonPath("$.status").value(expectedResponse.getStatus().toString()))
                .andExpect(jsonPath("$.balance").value(expectedResponse.getBalance().doubleValue()));

        verify(cardService, times(1)).updateCard(lastFourDigits, activate);
    }

    @Test
    void deleteCard_ShouldReturnNoContent() throws Exception {
        String lastFourDigits = "1234";

        mockMvc.perform(delete("/admin/cards/delete")
                        .param("last_four_card_digits", lastFourDigits))
                .andExpect(status().isNoContent());

        verify(cardService, times(1)).deleteCard(lastFourDigits);
    }

    @Test
    void getAllCards_ShouldReturnOk() throws Exception {
        when(cardService.getAllCards()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/admin/cards/"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));

        verify(cardService, times(1)).getAllCards();
    }

    @Test
    void getAllCardsBlockRequests_ShouldReturnPageOfRequests() throws Exception {
        CardBlockingRequestDto requestDto = createCardBlockingRequest();
        requestDto.setId(UUID.randomUUID());

        Page<CardBlockingRequestDto> page = new PageImpl<>(
                Collections.singletonList(requestDto),
                PageRequest.of(0, 10, Sort.Direction.DESC, "id"),
                1
        );

        when(cardBlockingService.getAllRequests(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/admin/cards/block/requests")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sort", "id,desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].id").exists())
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    private CardBlockingRequestDto createCardBlockingRequest() {
        return CardBlockingRequestDto.builder()
                .Id(TEST_CARD_BLOCKING_REQUEST_ID)
                .last_four_card_digits(TEST_LAST_FOUR_CARD_DIGITS)
                .userId(TEST_USER_ID)
                .status(BlockRequestStatus.PENDING)
                .createdAt(LocalDate.now())
                .build();
    }

    @Test
    void resolveCard_ShouldReturnOk() throws Exception {
        UUID requestId = TEST_CARD_BLOCKING_REQUEST_ID;
        CardBlockingRequestDto expectedResponse = createCardBlockingRequest();
        expectedResponse.setAdminId(TEST_ADMIN_ID);
        expectedResponse.setUpdatedAt(LocalDate.now());
        expectedResponse.setStatus(BlockRequestStatus.APPROVED);
        when(cardBlockingService.resolveRequest(requestId)).thenReturn(expectedResponse);

        mockMvc.perform(patch("/admin/cards/block/resolve")
                        .param("request_id", requestId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(expectedResponse.getId().toString()))
                .andExpect(jsonPath("$.status").value(expectedResponse.getStatus().toString()));

        verify(cardBlockingService, times(1)).resolveRequest(requestId);
    }

    @Test
    void getAllUsers_ShouldReturnOk() throws Exception {
        when(userService.getAllUsers()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/admin/users/"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));

        verify(userService, times(1)).getAllUsers();
    }

    @Test
    void registerUser_ShouldReturnOk() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("admin");
        request.setPassword("password");
        request.setRole(Role.USER);
        UserDto expectedResponse = createUserDto(request);
        when(authService.register(any(RegisterRequest.class))).thenReturn(expectedResponse);

        mockMvc.perform(post("/admin/users/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(expectedResponse.getId().toString()));

        verify(authService, times(1)).register(any(RegisterRequest.class));
    }

    private UserDto createUserDto(RegisterRequest request) {
        return UserDto.builder()
                .Id(TEST_USER_ID)
                .username(request.getUsername())
                .role(Role.USER)
                .build();
    }

    @Test
    void deleteUser_ShouldReturnNoContent() throws Exception {
        UUID userId = UUID.randomUUID();

        mockMvc.perform(delete("/admin/users/delete")
                        .param("id", userId.toString()))
                .andExpect(status().isNoContent());

        verify(userService, times(1)).deleteUser(userId);
    }
}