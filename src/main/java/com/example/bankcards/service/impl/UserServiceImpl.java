package com.example.bankcards.service.impl;

import com.example.bankcards.dto.CardDto;
import com.example.bankcards.dto.UserDto;
import com.example.bankcards.dto.requests.BalanceRequest;
import com.example.bankcards.dto.requests.TransferRequest;
import com.example.bankcards.dto.responses.BalanceResponse;
import com.example.bankcards.dto.responses.TransferResponse;
import com.example.bankcards.entity.CardEntity;
import com.example.bankcards.entity.UserEntity;
import com.example.bankcards.exception.exceptions.CardNotFoundException;
import com.example.bankcards.exception.exceptions.TransferException;
import com.example.bankcards.exception.exceptions.UserNotFoundException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.service.UserService;
import com.example.bankcards.util.mappers.CardMapper;
import com.example.bankcards.util.mappers.UserMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final CardRepository cardRepository;
    private final CardMapper cardMapper;

    public UserServiceImpl(UserRepository userRepository,
                           UserMapper userMapper,
                           CardRepository cardRepository,
                           CardMapper cardMapper) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.cardRepository = cardRepository;
        this.cardMapper = cardMapper;
    }

    @Override
    public List<UserDto> getAllUsers() {
        return userRepository.findAll().stream().map(userMapper::toDto).collect(Collectors.toList());
    }

    @Override
    public void deleteUser(UUID id) {
        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
        } else {
            throw new UserNotFoundException(String.format("User with id %s not found", id));
        }
    }

    @Override
    public Page<CardDto> getCards(Pageable pageable, String searchQuery) {
        UUID userId = getCurrentUserId();
        if (searchQuery != null && !searchQuery.isEmpty()) {
            return cardRepository.findByUserEntityIdAndCardLastFourDigitsContaining(userId, searchQuery, pageable)
                    .map(cardMapper::toDto);
        }
        return cardRepository.findAllByUserEntityId(userId, pageable).map(cardMapper::toDto);
    }

    @Override
    public List<BalanceResponse> getBalance(BalanceRequest request) {
        List<String> lastFourCardDigits = request.getLastFourCardDigits();
        if (lastFourCardDigits.isEmpty()) {
            throw new IllegalArgumentException("Last four card digits must be provided");
        }
        return lastFourCardDigits.stream()
                .map(digit -> {
                    CardEntity card = cardRepository.findByCardLastFourDigits(digit)
                            .orElseThrow(() -> new CardNotFoundException("Card with last four digits " + digit + " not found"));
                    return new BalanceResponse(card.getBalance(), card.getCardLastFourDigits());
                })
                .toList();
    }

    @Override
    public TransferResponse transfer(TransferRequest request) {
        String source = request.getSourceLastFourCardDigits();
        String target = request.getTargetLastFourCardDigits();
        BigDecimal amount = request.getAmount();

        validateTransferAbility(source, target, amount);
        return processTransfer(source,target,amount);
    }

    private TransferResponse processTransfer(String sourceDigits, String targetDigits, BigDecimal amount) {
        List<CardEntity> cards = cardRepository.findByCardLastFourDigitsIn((Arrays.asList(sourceDigits, targetDigits)));

        CardEntity sourceCard = cards.stream()
                .filter(c -> c.getCardLastFourDigits().equals(sourceDigits))
                .findFirst()
                .orElseThrow(() -> new CardNotFoundException("Source card not found"));

        CardEntity targetCard = cards.stream()
                .filter(c -> c.getCardLastFourDigits().equals(targetDigits))
                .findFirst()
                .orElseThrow(() -> new CardNotFoundException("Target card not found"));

        if (sourceCard.getBalance().compareTo(amount) < 0) {
            throw new TransferException("Insufficient funds on card " + cardMapper.toDto(sourceCard).getCardNumber());
        }

        sourceCard.setBalance(sourceCard.getBalance().subtract(amount));
        targetCard.setBalance(targetCard.getBalance().add(amount));

        cardRepository.saveAll(List.of(sourceCard, targetCard));

        return new TransferResponse(
                sourceCard.getCardNumber(),
                targetCard.getCardNumber(),
                amount
        );
    }

    private void validateTransferAbility(String sourceDigits, String targetDigits, BigDecimal amount) {
        if (sourceDigits.equals(targetDigits)) {
            throw new TransferException("Cannot transfer to the same card");
        }

        if (!cardRepository.existsByCardLastFourDigits(targetDigits)) {
            throw new CardNotFoundException("Target card not found");
        }

        // Проверка суммы
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new TransferException("Transfer amount must be positive");
        }
    }


    private UUID getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UserDetails) {
            return ((UserEntity) auth.getPrincipal()).getId();
        }
        throw new AccessDeniedException("User not authenticated");
    }

}
