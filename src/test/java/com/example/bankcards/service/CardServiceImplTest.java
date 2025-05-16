package com.example.bankcards.service;

import com.example.bankcards.dto.CardDto;
import com.example.bankcards.dto.requests.CardCreationRequest;
import com.example.bankcards.entity.CardEntity;
import com.example.bankcards.entity.UserEntity;
import com.example.bankcards.entity.enums.CardStatus;
import com.example.bankcards.exception.exceptions.CardAlreadyExistsException;
import com.example.bankcards.exception.exceptions.CardNotFoundException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.service.impl.CardServiceImpl;
import com.example.bankcards.util.mappers.CardMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CardServiceImplTest {

    @Mock
    private CardRepository cardRepository;

    @Mock
    private CardMapper cardMapper;

    @InjectMocks
    private CardServiceImpl cardService;

    private final String CARD_NUMBER = "1234567890123456";
    private final String LAST_FOUR_DIGITS = "3456";
    private final UUID CARD_ID = UUID.randomUUID();
    private final UUID USER_ID = UUID.randomUUID();

    private CardEntity cardEntity;
    private CardDto cardDto;
    private CardCreationRequest cardCreationRequest;
    private UserEntity userEntity;

    @BeforeEach
    void setUp() {
        userEntity = new UserEntity();
        userEntity.setId(USER_ID);
        userEntity.setUsername("testUser");

        cardEntity = new CardEntity();
        cardEntity.setId(CARD_ID);
        cardEntity.setCardLastFourDigits(LAST_FOUR_DIGITS);
        cardEntity.setUserEntity(userEntity);
        cardEntity.setStatus(CardStatus.ACTIVE);
        cardEntity.setExpiryDate(LocalDate.now().plusYears(2));

        cardDto = new CardDto();
        cardDto.setId(CARD_ID);
        cardDto.setLastFourCardDigits(LAST_FOUR_DIGITS);
        cardDto.setStatus(CardStatus.ACTIVE);

        cardCreationRequest = new CardCreationRequest();
        cardCreationRequest.setCardNumber(CARD_NUMBER);
    }

    @Test
    void createCard_Success() {
        when(cardRepository.findByCardLastFourDigits(LAST_FOUR_DIGITS)).thenReturn(Optional.empty());
        when(cardMapper.toEntity(cardCreationRequest)).thenReturn(cardEntity);
        when(cardRepository.saveAndFlush(cardEntity)).thenReturn(cardEntity);
        when(cardMapper.toDto(cardEntity)).thenReturn(cardDto);

        CardDto result = cardService.createCard(cardCreationRequest);

        assertNotNull(result);
        assertEquals(CARD_ID, result.getId());
        assertEquals(LAST_FOUR_DIGITS, result.getLastFourCardDigits());
        assertEquals(CardStatus.ACTIVE, result.getStatus());
        verify(cardRepository).saveAndFlush(cardEntity);
    }

    @Test
    void createCard_CardAlreadyExists() {
        when(cardRepository.findByCardLastFourDigits(LAST_FOUR_DIGITS)).thenReturn(Optional.of(cardEntity));

        assertThrows(CardAlreadyExistsException.class, () ->
                cardService.createCard(cardCreationRequest)
        );
        verify(cardRepository, never()).saveAndFlush(any(CardEntity.class));
    }

    @Test
    void updateCard_Activate() {
        cardEntity.setStatus(CardStatus.BLOCKED);
        when(cardRepository.findByCardLastFourDigits(LAST_FOUR_DIGITS)).thenReturn(Optional.of(cardEntity));
        when(cardRepository.saveAndFlush(cardEntity)).thenReturn(cardEntity);
        when(cardMapper.toDto(cardEntity)).thenReturn(cardDto);

        cardService.updateCard(LAST_FOUR_DIGITS, "activate");

        assertEquals(CardStatus.ACTIVE, cardEntity.getStatus());
        verify(cardRepository).saveAndFlush(cardEntity);
    }

    @Test
    void updateCard_Deactivate() {
        when(cardRepository.findByCardLastFourDigits(LAST_FOUR_DIGITS)).thenReturn(Optional.of(cardEntity));
        when(cardRepository.saveAndFlush(cardEntity)).thenReturn(cardEntity);
        when(cardMapper.toDto(cardEntity)).thenReturn(cardDto);

        CardDto result = cardService.updateCard(LAST_FOUR_DIGITS, "deactivate");

        assertEquals(CardStatus.BLOCKED, cardEntity.getStatus());
        verify(cardRepository).saveAndFlush(cardEntity);
    }

    @Test
    void updateCard_Expire_Success() {
        cardEntity.setExpiryDate(LocalDate.now().minusDays(1));
        when(cardRepository.findByCardLastFourDigits(LAST_FOUR_DIGITS)).thenReturn(Optional.of(cardEntity));
        when(cardRepository.saveAndFlush(cardEntity)).thenReturn(cardEntity);
        when(cardMapper.toDto(cardEntity)).thenReturn(cardDto);
        cardService.updateCard(LAST_FOUR_DIGITS, "expired");

        assertEquals(CardStatus.EXPIRED, cardEntity.getStatus());
        verify(cardRepository).saveAndFlush(cardEntity);
    }

    @Test
    void updateCard_Expire_CardNotExpired() {
        when(cardRepository.findByCardLastFourDigits(LAST_FOUR_DIGITS)).thenReturn(Optional.of(cardEntity));

        assertThrows(IllegalArgumentException.class, () ->
                cardService.updateCard(LAST_FOUR_DIGITS, "expired")
        );
        verify(cardRepository, never()).saveAndFlush(any(CardEntity.class));
    }

    @Test
    void updateCard_InvalidAction() {
        when(cardRepository.findByCardLastFourDigits(LAST_FOUR_DIGITS)).thenReturn(Optional.of(cardEntity));

        assertThrows(IllegalArgumentException.class, () ->
                cardService.updateCard(LAST_FOUR_DIGITS, "invalidAction")
        );
        verify(cardRepository, never()).saveAndFlush(any(CardEntity.class));
    }

    @Test
    void updateCard_CardNotFound() {
        when(cardRepository.findByCardLastFourDigits(LAST_FOUR_DIGITS)).thenReturn(Optional.empty());

        assertThrows(CardNotFoundException.class, () ->
                cardService.updateCard(LAST_FOUR_DIGITS, "activate")
        );
        verify(cardRepository, never()).saveAndFlush(any(CardEntity.class));
    }

    @Test
    void deleteCard_Success() {
        when(cardRepository.findByCardLastFourDigits(LAST_FOUR_DIGITS)).thenReturn(Optional.of(cardEntity));

        cardService.deleteCard(LAST_FOUR_DIGITS);

        verify(cardRepository).delete(cardEntity);
    }

    @Test
    void deleteCard_CardNotFound() {
        when(cardRepository.findByCardLastFourDigits(LAST_FOUR_DIGITS)).thenReturn(Optional.empty());

        assertThrows(CardNotFoundException.class, () ->
                cardService.deleteCard(LAST_FOUR_DIGITS)
        );
        verify(cardRepository, never()).delete(any(CardEntity.class));
    }

    @Test
    void getAllCards_Success() {
        CardEntity card2 = new CardEntity();
        card2.setId(UUID.randomUUID());
        card2.setCardLastFourDigits("5678");
        card2.setStatus(CardStatus.BLOCKED);

        CardDto cardDto2 = new CardDto();
        cardDto2.setId(card2.getId());
        cardDto2.setLastFourCardDigits("5678");
        cardDto2.setStatus(CardStatus.BLOCKED);

        List<CardEntity> cards = Arrays.asList(cardEntity, card2);
        when(cardRepository.findAll()).thenReturn(cards);
        when(cardMapper.toDto(cardEntity)).thenReturn(cardDto);
        when(cardMapper.toDto(card2)).thenReturn(cardDto2);

        List<CardDto> result = cardService.getAllCards();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(CARD_ID, result.get(0).getId());
        assertEquals(card2.getId(), result.get(1).getId());
    }

    @Test
    void getAllCards_EmptyList() {
        when(cardRepository.findAll()).thenReturn(List.of());

        List<CardDto> result = cardService.getAllCards();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}
