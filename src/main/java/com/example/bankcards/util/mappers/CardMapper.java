package com.example.bankcards.util.mappers;

import com.example.bankcards.dto.CardDto;
import com.example.bankcards.dto.requests.CardCreationRequest;
import com.example.bankcards.entity.CardEntity;
import com.example.bankcards.entity.UserEntity;
import com.example.bankcards.exception.exceptions.UserNotFoundException;
import com.example.bankcards.repository.UserRepository;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class CardMapper {
    private final UserRepository userRepository;
    public CardMapper(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public CardEntity toEntity(CardCreationRequest request) {
        UserEntity owner = userRepository.findById(request.getOwnerId()).orElseThrow(() -> new UserNotFoundException(String.format("User with id %s not found", request.getOwnerId())));

        CardEntity cardEntity = new CardEntity();
        cardEntity.setCardNumber(request.getCardNumber());
        cardEntity.setUserEntity(owner);
        cardEntity.setExpiryDate(request.getExpiresIn());
        cardEntity.setStatus(request.getStatus());
        cardEntity.setBalance(request.getStartBalance() == null ?BigDecimal.ZERO:request.getStartBalance());
        return cardEntity;
    }

    public CardDto toDto(CardEntity newCard) {
        return CardDto.builder()
                .Id(newCard.getId())
                .cardNumber(processCardNumber(newCard.getCardLastFourDigits()))
                .ownerId(newCard.getUserEntity().getId())
                .lastFourCardDigits(newCard.getCardLastFourDigits())
                .expiresIn(newCard.getExpiryDate())
                .status(newCard.getStatus())
                .balance(newCard.getBalance())
                .build();
    }
    private String processCardNumber(String lastFourDigits) {
        return "************" + lastFourDigits;
    }
}
