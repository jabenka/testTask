package com.example.bankcards.controller;

import com.example.bankcards.dto.CardBlockingRequestDto;
import com.example.bankcards.dto.CardDto;
import com.example.bankcards.dto.requests.BalanceRequest;
import com.example.bankcards.dto.requests.TransferRequest;
import com.example.bankcards.dto.responses.BalanceResponse;
import com.example.bankcards.dto.responses.TransferResponse;
import com.example.bankcards.entity.enums.CardStatus;
import com.example.bankcards.service.CardBlockingService;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class UserControllerTest {

    private static final UUID TEST_CARD_BLOCKING_REQUEST_ID = UUID.randomUUID();
    private static final String TEST_SOURCE_LAST4DIGITS = "1221";
    private static final String TEST_TARGET_LAST4DIGITS = "1488";


    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private UserService userService;

    @Mock
    private CardBlockingService cardBlockingService;

    @InjectMocks
    private UserController userController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        objectMapper.registerModule(new JavaTimeModule());
        mockMvc = MockMvcBuilders.standaloneSetup(userController)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();
    }

    @Test
    void getCards_ShouldReturnPageOfCards() throws Exception {
        Page<CardDto> mockPage = new PageImpl<>(List.of(
                new CardDto(UUID.randomUUID(), "****1234", "1234", UUID.randomUUID(), LocalDate.of(2020, 1, 1),
                        CardStatus.ACTIVE, BigDecimal.valueOf(1000)),
                new CardDto(UUID.randomUUID(), "****5678", "5678", UUID.randomUUID(), LocalDate.of(2020, 1, 1),
                        CardStatus.ACTIVE, BigDecimal.valueOf(1000))
        ), PageRequest.of(0, 10, Sort.Direction.DESC, "id"), 2);

        when(userService.getCards(any(Pageable.class), anyString()))
                .thenReturn(mockPage);

        mockMvc.perform(get("/user/cards/get")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sort", "id,desc")
                        .param("search_query", "test"))
                .andDo(result -> System.out.println("Response: " +
                        result.getResponse().getContentAsString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].card_number").exists())

        ;
    }

    @Test
    void getCards_ShouldUseDefaultPagination() throws Exception {
        Page<CardDto> page = new PageImpl<>(
                Collections.emptyList(),
                PageRequest.of(0, 10, Sort.Direction.DESC, "id"),
                1
        );
        when(userService.getCards(any(Pageable.class), any()))
                .thenReturn(page);

        mockMvc.perform(get("/user/cards/get"))
                .andExpect(status().isOk());
    }

    @Test
    void blockCard_ShouldReturnBlockRequest() throws Exception {
        String lastFourDigits = "1234";
        CardBlockingRequestDto expectedResponse = createCardBlockingRequestDto(lastFourDigits);
        when(cardBlockingService.createBlockRequest(lastFourDigits))
                .thenReturn(expectedResponse);

        mockMvc.perform(post("/user/cards/block")
                        .param("last_four_card_digits", lastFourDigits))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(expectedResponse.getId().toString()));
    }

    public CardBlockingRequestDto createCardBlockingRequestDto(String lastFourDigits) {
        return CardBlockingRequestDto.builder()
                .Id(TEST_CARD_BLOCKING_REQUEST_ID)
                .last_four_card_digits(lastFourDigits)
                .userId(UUID.randomUUID())
                .createdAt(LocalDate.now())
                .build();
    }

    @Test
    void blockCard_WithoutParams_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(post("/user/cards/block"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getBalance_ShouldReturnBalance() throws Exception {
        BalanceRequest request = new BalanceRequest();
        request.setLastFourCardDigits(List.of(TEST_SOURCE_LAST4DIGITS));
        List<BalanceResponse> expectedResponse = new ArrayList<BalanceResponse>();
        when(userService.getBalance(any(BalanceRequest.class)))
                .thenReturn(expectedResponse);

        mockMvc.perform(get("/user/cards/balance")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void getBalance_WithInvalidRequest_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/user/cards/balance")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(" "))
                .andDo(result -> System.out.println("Response: " +
                        result.getResponse().getContentAsString()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void transfer_ShouldReturnSuccess() throws Exception {
        TransferRequest request = new TransferRequest();
        request.setSourceLastFourCardDigits(TEST_SOURCE_LAST4DIGITS);
        request.setTargetLastFourCardDigits(TEST_TARGET_LAST4DIGITS);
        request.setAmount(new BigDecimal("500.00"));
        TransferResponse expectedResponse = createTransferResponse();
        when(userService.transfer(any(TransferRequest.class)))
                .thenReturn(expectedResponse);

        mockMvc.perform(post("/user/cards/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.source").value(expectedResponse.getSourceLastFourCardDigits()));
    }

    private TransferResponse createTransferResponse() {
        return TransferResponse.builder()
                .sourceLastFourCardDigits(TEST_SOURCE_LAST4DIGITS)
                .targetLastFourCardDigits(TEST_TARGET_LAST4DIGITS)
                .amount(new BigDecimal("500.00"))
                .build();
    }

    @Test
    void transfer_WithInvalidAmount_ShouldReturnBadRequest() throws Exception {
        TransferRequest request = new TransferRequest();
        request.setSourceLastFourCardDigits(TEST_SOURCE_LAST4DIGITS);
        request.setTargetLastFourCardDigits(TEST_TARGET_LAST4DIGITS);
        request.setAmount(new BigDecimal("-100.00"));

        mockMvc.perform(post("/user/cards/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}