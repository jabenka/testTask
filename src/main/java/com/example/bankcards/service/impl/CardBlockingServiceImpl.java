package com.example.bankcards.service.impl;

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
import com.example.bankcards.service.CardBlockingService;
import com.example.bankcards.util.mappers.CardBlockingRequestMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class CardBlockingServiceImpl implements CardBlockingService {

    private final CardRepository cardRepository;
    private final CardBlockingRequestRepository cardBlockingRequestRepository;
    private final CardBlockingRequestMapper cardBlockingRequestMapper;
    private final UserRepository userRepository;

    public CardBlockingServiceImpl(CardRepository cardRepository, CardBlockingRequestRepository cardBlockingRequestRepository, CardBlockingRequestMapper cardBlockingRequestMapper, UserRepository userRepository) {
        this.cardRepository = cardRepository;
        this.cardBlockingRequestRepository = cardBlockingRequestRepository;
        this.cardBlockingRequestMapper = cardBlockingRequestMapper;
        this.userRepository = userRepository;
    }

    @Override
    public CardBlockingRequestDto createBlockRequest(String lastFourCardDigits) {
        if(cardBlockingRequestRepository.existsByCard_CardLastFourDigits(lastFourCardDigits)){
            throw new CardBlockingRequestAlreadyExists(String.format("Request with card %s already exists",lastFourCardDigits));
        }
        CardEntity card = cardRepository.findByCardLastFourDigits(lastFourCardDigits).stream().findFirst()
                .orElseThrow(() -> new CardNotFoundException(String.format("Card %s not found", lastFourCardDigits)));
        return createCardBlockRequest(card);
    }

    @Override
    public Page<CardBlockingRequestDto> getAllRequests(Pageable pageable) {
        return cardBlockingRequestRepository.findAll(pageable).map(cardBlockingRequestMapper::toDto);
    }

    @Override
    public CardBlockingRequestDto resolveRequest(UUID requestId) {
        CardBlockingRequest request = cardBlockingRequestRepository.findById(requestId)
                .orElseThrow(()-> new CardBlockingRequestNotFoundException(
                                String.format("Card blocking request with id %s not found", requestId)
                ));
        UserEntity admin = userRepository.findById(getCurrentUserId()).stream().findFirst()
                .orElseThrow(()-> new UserNotFoundException(String.format("User with id %s not found",getCurrentUserId())));
            request.setAdmin(admin);
            request.setStatus(BlockRequestStatus.APPROVED);
            request.getCard().setStatus(CardStatus.BLOCKED);
        request = cardBlockingRequestRepository.saveAndFlush(request);
        return cardBlockingRequestMapper.toDto(request);
    }

    private CardBlockingRequestDto createCardBlockRequest(CardEntity card) {
        CardBlockingRequest cardBlockingRequest = new CardBlockingRequest();
        cardBlockingRequest.setCard(card);
        cardBlockingRequest.setUser(card.getUserEntity());
        cardBlockingRequest.setStatus(BlockRequestStatus.PENDING);
        cardBlockingRequest = cardBlockingRequestRepository.saveAndFlush(cardBlockingRequest);
        return cardBlockingRequestMapper.toDto(cardBlockingRequest);
    }

    private UUID getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UserDetails) {
            return ((UserEntity) auth.getPrincipal()).getId();
        }
        throw new AccessDeniedException("User not authenticated");
    }
}
