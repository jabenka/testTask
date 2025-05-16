package com.example.bankcards.service;

import com.example.bankcards.dto.CardDto;
import com.example.bankcards.dto.UserDto;
import com.example.bankcards.dto.requests.BalanceRequest;
import com.example.bankcards.dto.requests.TransferRequest;
import com.example.bankcards.dto.responses.BalanceResponse;
import com.example.bankcards.dto.responses.TransferResponse;
import com.example.bankcards.entity.CardEntity;
import com.example.bankcards.entity.UserEntity;
import com.example.bankcards.entity.enums.CardStatus;
import com.example.bankcards.exception.exceptions.CardNotFoundException;
import com.example.bankcards.exception.exceptions.TransferException;
import com.example.bankcards.exception.exceptions.UserNotFoundException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.service.impl.UserServiceImpl;
import com.example.bankcards.util.mappers.CardMapper;
import com.example.bankcards.util.mappers.UserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private CardRepository cardRepository;

    @Mock
    private CardMapper cardMapper;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private UserServiceImpl userService;

    private final UUID USER_ID = UUID.randomUUID();
    private final UUID CARD_ID_1 = UUID.randomUUID();
    private final UUID CARD_ID_2 = UUID.randomUUID();
    private final String CARD_NUMBER_1 = "1234567890123456";
    private final String CARD_NUMBER_2 = "6543210987654321";
    private final String LAST_FOUR_DIGITS_1 = "3456";
    private final String LAST_FOUR_DIGITS_2 = "4321";

    private UserEntity userEntity;
    private UserDto userDto;
    private CardEntity cardEntity1;
    private CardEntity cardEntity2;
    private CardDto cardDto1;
    private CardDto cardDto2;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        userEntity = new UserEntity();
        userEntity.setId(USER_ID);
        userEntity.setUsername("testUser");

        userDto = new UserDto();
        userDto.setId(USER_ID);
        userDto.setUsername("testUser");

        cardEntity1 = new CardEntity();
        cardEntity1.setId(CARD_ID_1);
        cardEntity1.setCardNumber(CARD_NUMBER_1);
        cardEntity1.setCardLastFourDigits(LAST_FOUR_DIGITS_1);
        cardEntity1.setUserEntity(userEntity);
        cardEntity1.setStatus(CardStatus.ACTIVE);
        cardEntity1.setBalance(new BigDecimal("1000.00"));

        cardEntity2 = new CardEntity();
        cardEntity2.setId(CARD_ID_2);
        cardEntity2.setCardNumber(CARD_NUMBER_2);
        cardEntity2.setCardLastFourDigits(LAST_FOUR_DIGITS_2);
        cardEntity2.setUserEntity(userEntity);
        cardEntity2.setStatus(CardStatus.ACTIVE);
        cardEntity2.setBalance(new BigDecimal("500.00"));

        cardDto1 = new CardDto();
        cardDto1.setId(CARD_ID_1);
        cardDto1.setCardNumber(CARD_NUMBER_1);
        cardDto1.setLastFourCardDigits(LAST_FOUR_DIGITS_1);
        cardDto1.setStatus(CardStatus.ACTIVE);
        cardDto1.setBalance(new BigDecimal("1000.00"));

        cardDto2 = new CardDto();
        cardDto2.setId(CARD_ID_2);
        cardDto2.setCardNumber(CARD_NUMBER_2);
        cardDto2.setLastFourCardDigits(LAST_FOUR_DIGITS_2);
        cardDto2.setStatus(CardStatus.ACTIVE);
        cardDto2.setBalance(new BigDecimal("500.00"));

        pageable = mock(Pageable.class);

        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void getAllUsers_Success() {
        List<UserEntity> users = List.of(userEntity);
        when(userRepository.findAll()).thenReturn(users);
        when(userMapper.toDto(userEntity)).thenReturn(userDto);

        List<UserDto> result = userService.getAllUsers();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(USER_ID, result.get(0).getId());
        assertEquals("testUser", result.get(0).getUsername());
    }

    @Test
    void deleteUser_Success() {
        when(userRepository.existsById(USER_ID)).thenReturn(true);

        userService.deleteUser(USER_ID);

        verify(userRepository).deleteById(USER_ID);
    }

    @Test
    void deleteUser_UserNotFound() {
        when(userRepository.existsById(USER_ID)).thenReturn(false);

        assertThrows(UserNotFoundException.class, () ->
                userService.deleteUser(USER_ID)
        );
        verify(userRepository, never()).deleteById(any(UUID.class));
    }

    @Test
    void getCards_WithoutSearchQuery() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userEntity);

        List<CardEntity> cards = List.of(cardEntity1, cardEntity2);
        Page<CardEntity> cardPage = new PageImpl<>(cards);

        when(cardRepository.findAllByUserEntityId(USER_ID, pageable)).thenReturn(cardPage);
        when(cardMapper.toDto(cardEntity1)).thenReturn(cardDto1);
        when(cardMapper.toDto(cardEntity2)).thenReturn(cardDto2);

        Page<CardDto> result = userService.getCards(pageable, null);

        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
        assertEquals(CARD_ID_1, result.getContent().get(0).getId());
        assertEquals(CARD_ID_2, result.getContent().get(1).getId());
    }

    @Test
    void getCards_WithSearchQuery() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userEntity);

        List<CardEntity> cards = List.of(cardEntity1);
        Page<CardEntity> cardPage = new PageImpl<>(cards);

        when(cardRepository.findByUserEntityIdAndCardLastFourDigitsContaining(USER_ID, "345", pageable)).thenReturn(cardPage);
        when(cardMapper.toDto(cardEntity1)).thenReturn(cardDto1);

        Page<CardDto> result = userService.getCards(pageable, "345");

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(CARD_ID_1, result.getContent().get(0).getId());
    }

    @Test
    void getCards_UserNotAuthenticated() {
        when(securityContext.getAuthentication()).thenReturn(null);

        assertThrows(AccessDeniedException.class, () ->
                userService.getCards(pageable, null)
        );
    }

    @Test
    void getBalance_Success() {
        BalanceRequest request = new BalanceRequest();
        request.setLastFourCardDigits(List.of(LAST_FOUR_DIGITS_1, LAST_FOUR_DIGITS_2));

        when(cardRepository.findByCardLastFourDigits(LAST_FOUR_DIGITS_1)).thenReturn(Optional.of(cardEntity1));
        when(cardRepository.findByCardLastFourDigits(LAST_FOUR_DIGITS_2)).thenReturn(Optional.of(cardEntity2));

        List<BalanceResponse> result = userService.getBalance(request);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(new BigDecimal("1000.00"), result.get(0).getBalance());
        assertEquals(LAST_FOUR_DIGITS_1, result.get(0).getLastFourCardDigits());
        assertEquals(new BigDecimal("500.00"), result.get(1).getBalance());
        assertEquals(LAST_FOUR_DIGITS_2, result.get(1).getLastFourCardDigits());
    }

    @Test
    void getBalance_EmptyRequest() {
        BalanceRequest request = new BalanceRequest();
        request.setLastFourCardDigits(Collections.emptyList());

        assertThrows(IllegalArgumentException.class, () ->
                userService.getBalance(request)
        );
    }

    @Test
    void getBalance_CardNotFound() {
        BalanceRequest request = new BalanceRequest();
        request.setLastFourCardDigits(List.of("9999"));

        when(cardRepository.findByCardLastFourDigits("9999")).thenReturn(Optional.empty());

        assertThrows(CardNotFoundException.class, () ->
                userService.getBalance(request)
        );
    }

    @Test
    void transfer_Success() {
        TransferRequest request = new TransferRequest();
        request.setSourceLastFourCardDigits(LAST_FOUR_DIGITS_1);
        request.setTargetLastFourCardDigits(LAST_FOUR_DIGITS_2);
        request.setAmount(new BigDecimal("100.00"));

        List<CardEntity> cards = List.of(cardEntity1, cardEntity2);
        when(cardRepository.existsByCardLastFourDigits(LAST_FOUR_DIGITS_2)).thenReturn(true);
        when(cardRepository.findByCardLastFourDigitsIn(List.of(LAST_FOUR_DIGITS_1, LAST_FOUR_DIGITS_2))).thenReturn(cards);

        TransferResponse result = userService.transfer(request);

        assertNotNull(result);
        assertEquals(CARD_NUMBER_1, result.getSourceLastFourCardDigits());
        assertEquals(CARD_NUMBER_2, result.getTargetLastFourCardDigits());
        assertEquals(new BigDecimal("100.00"), result.getAmount());
        assertEquals(new BigDecimal("900.00"), cardEntity1.getBalance());
        assertEquals(new BigDecimal("600.00"), cardEntity2.getBalance());
        verify(cardRepository).saveAll(List.of(cardEntity1, cardEntity2));
    }

    @Test
    void transfer_SameCard() {
        TransferRequest request = new TransferRequest();
        request.setSourceLastFourCardDigits(LAST_FOUR_DIGITS_1);
        request.setTargetLastFourCardDigits(LAST_FOUR_DIGITS_1);
        request.setAmount(new BigDecimal("100.00"));

        assertThrows(TransferException.class, () ->
                userService.transfer(request)
        );
        verify(cardRepository, never()).saveAll(any());
    }

    @Test
    void transfer_TargetCardNotFound() {
        TransferRequest request = new TransferRequest();
        request.setSourceLastFourCardDigits(LAST_FOUR_DIGITS_1);
        request.setTargetLastFourCardDigits("9999");
        request.setAmount(new BigDecimal("100.00"));

        when(cardRepository.existsByCardLastFourDigits("9999")).thenReturn(false);

        assertThrows(CardNotFoundException.class, () ->
                userService.transfer(request)
        );
        verify(cardRepository, never()).saveAll(any());
    }

    @Test
    void transfer_NegativeAmount() {
        TransferRequest request = new TransferRequest();
        request.setSourceLastFourCardDigits(LAST_FOUR_DIGITS_1);
        request.setTargetLastFourCardDigits(LAST_FOUR_DIGITS_2);
        request.setAmount(new BigDecimal("-100.00"));

        when(cardRepository.existsByCardLastFourDigits(LAST_FOUR_DIGITS_2)).thenReturn(true);

        assertThrows(TransferException.class, () ->
                userService.transfer(request)
        );
        verify(cardRepository, never()).saveAll(any());
    }
}
