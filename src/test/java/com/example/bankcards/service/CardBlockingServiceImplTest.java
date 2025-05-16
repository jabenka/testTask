package com.example.bankcards.service;

import com.example.bankcards.dto.CardBlockingRequestDto;
import com.example.bankcards.entity.CardBlockingRequest;
import com.example.bankcards.entity.CardEntity;
import com.example.bankcards.entity.UserEntity;
import com.example.bankcards.entity.enums.BlockRequestStatus;
import com.example.bankcards.entity.enums.CardStatus;
import com.example.bankcards.exception.exceptions.CardBlockingRequestAlreadyExists;
import com.example.bankcards.exception.exceptions.CardBlockingRequestNotFoundException;
import com.example.bankcards.exception.exceptions.CardNotFoundException;
import com.example.bankcards.exception.exceptions.UserNotFoundException;
import com.example.bankcards.repository.CardBlockingRequestRepository;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.service.impl.CardBlockingServiceImpl;
import com.example.bankcards.util.mappers.CardBlockingRequestMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CardBlockingServiceImplTest {

    @Mock
    private CardRepository cardRepository;

    @Mock
    private CardBlockingRequestRepository cardBlockingRequestRepository;

    @Mock
    private CardBlockingRequestMapper cardBlockingRequestMapper;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private CardBlockingServiceImpl cardBlockingService;

    private final String CARD_LAST_FOUR_DIGITS = "1234";
    private final UUID USER_ID = UUID.randomUUID();
    private final UUID ADMIN_ID = UUID.randomUUID();
    private final UUID REQUEST_ID = UUID.randomUUID();

    private CardEntity cardEntity;
    private UserEntity userEntity;
    private UserEntity adminEntity;
    private CardBlockingRequest cardBlockingRequest;
    private CardBlockingRequestDto cardBlockingRequestDto;

    @BeforeEach
    void setUp() {
        userEntity = new UserEntity();
        userEntity.setId(USER_ID);
        userEntity.setUsername("user");

        adminEntity = new UserEntity();
        adminEntity.setId(ADMIN_ID);
        adminEntity.setUsername("admin");

        cardEntity = new CardEntity();
        cardEntity.setId(UUID.randomUUID());
        cardEntity.setCardLastFourDigits(CARD_LAST_FOUR_DIGITS);
        cardEntity.setUserEntity(userEntity);
        cardEntity.setStatus(CardStatus.ACTIVE);

        cardBlockingRequest = new CardBlockingRequest();
        cardBlockingRequest.setId(REQUEST_ID);
        cardBlockingRequest.setCard(cardEntity);
        cardBlockingRequest.setUser(userEntity);
        cardBlockingRequest.setStatus(BlockRequestStatus.PENDING);

        cardBlockingRequestDto = new CardBlockingRequestDto();
        cardBlockingRequestDto.setId(REQUEST_ID);
        cardBlockingRequestDto.setStatus(BlockRequestStatus.PENDING);

        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void createBlockRequest_Success() {
        when(cardBlockingRequestRepository.existsByCard_CardLastFourDigits(CARD_LAST_FOUR_DIGITS)).thenReturn(false);
        when(cardRepository.findByCardLastFourDigits(CARD_LAST_FOUR_DIGITS)).thenReturn(Optional.of(cardEntity));
        when(cardBlockingRequestRepository.saveAndFlush(any(CardBlockingRequest.class))).thenReturn(cardBlockingRequest);
        when(cardBlockingRequestMapper.toDto(cardBlockingRequest)).thenReturn(cardBlockingRequestDto);

        CardBlockingRequestDto result = cardBlockingService.createBlockRequest(CARD_LAST_FOUR_DIGITS);

        assertNotNull(result);
        assertEquals(REQUEST_ID, result.getId());
        assertEquals(BlockRequestStatus.PENDING, result.getStatus());
        verify(cardBlockingRequestRepository).saveAndFlush(any(CardBlockingRequest.class));
    }

    @Test
    void createBlockRequest_RequestAlreadyExists() {
        when(cardBlockingRequestRepository.existsByCard_CardLastFourDigits(CARD_LAST_FOUR_DIGITS)).thenReturn(true);

        assertThrows(CardBlockingRequestAlreadyExists.class, () ->
                cardBlockingService.createBlockRequest(CARD_LAST_FOUR_DIGITS)
        );
        verify(cardBlockingRequestRepository, never()).saveAndFlush(any(CardBlockingRequest.class));
    }

    @Test
    void createBlockRequest_CardNotFound() {
        when(cardBlockingRequestRepository.existsByCard_CardLastFourDigits(CARD_LAST_FOUR_DIGITS)).thenReturn(false);
        when(cardRepository.findByCardLastFourDigits(CARD_LAST_FOUR_DIGITS)).thenReturn(Optional.empty());

        assertThrows(CardNotFoundException.class, () ->
                cardBlockingService.createBlockRequest(CARD_LAST_FOUR_DIGITS)
        );
        verify(cardBlockingRequestRepository, never()).saveAndFlush(any(CardBlockingRequest.class));
    }

    @Test
    void getAllRequests_Success() {
        Pageable pageable = mock(Pageable.class);
        Page<CardBlockingRequest> page = new PageImpl<>(Collections.singletonList(cardBlockingRequest));
        when(cardBlockingRequestRepository.findAll(pageable)).thenReturn(page);
        when(cardBlockingRequestMapper.toDto(cardBlockingRequest)).thenReturn(cardBlockingRequestDto);

        Page<CardBlockingRequestDto> result = cardBlockingService.getAllRequests(pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(cardBlockingRequestDto, result.getContent().get(0));
    }

    @Test
    void resolveRequest_Success() {
        when(cardBlockingRequestRepository.findById(REQUEST_ID)).thenReturn(Optional.of(cardBlockingRequest));
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(adminEntity);
        when(userRepository.findById(ADMIN_ID)).thenReturn(Optional.of(adminEntity));
        when(cardBlockingRequestRepository.saveAndFlush(cardBlockingRequest)).thenReturn(cardBlockingRequest);
        when(cardBlockingRequestMapper.toDto(cardBlockingRequest)).thenReturn(cardBlockingRequestDto);

        CardBlockingRequestDto result = cardBlockingService.resolveRequest(REQUEST_ID);

        assertNotNull(result);
        verify(cardBlockingRequestRepository).saveAndFlush(cardBlockingRequest);
        assertEquals(BlockRequestStatus.APPROVED, cardBlockingRequest.getStatus());
        assertEquals(CardStatus.BLOCKED, cardBlockingRequest.getCard().getStatus());
        assertEquals(adminEntity, cardBlockingRequest.getAdmin());
    }

    @Test
    void resolveRequest_RequestNotFound() {
        when(cardBlockingRequestRepository.findById(REQUEST_ID)).thenReturn(Optional.empty());

        assertThrows(CardBlockingRequestNotFoundException.class, () ->
                cardBlockingService.resolveRequest(REQUEST_ID)
        );
        verify(cardBlockingRequestRepository, never()).saveAndFlush(any(CardBlockingRequest.class));
    }

    @Test
    void resolveRequest_AdminNotFound() {
        when(cardBlockingRequestRepository.findById(REQUEST_ID)).thenReturn(Optional.of(cardBlockingRequest));
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(adminEntity);
        when(userRepository.findById(ADMIN_ID)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () ->
                cardBlockingService.resolveRequest(REQUEST_ID)
        );
        verify(cardBlockingRequestRepository, never()).saveAndFlush(any(CardBlockingRequest.class));
    }
}
