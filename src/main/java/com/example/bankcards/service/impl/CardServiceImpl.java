package com.example.bankcards.service.impl;

import com.example.bankcards.dto.CardDto;
import com.example.bankcards.dto.requests.CardCreationRequest;
import com.example.bankcards.entity.CardEntity;
import com.example.bankcards.entity.enums.CardStatus;
import com.example.bankcards.exception.exceptions.CardAlreadyExistsException;
import com.example.bankcards.exception.exceptions.CardNotFoundException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.service.CardService;
import com.example.bankcards.util.mappers.CardMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class CardServiceImpl implements CardService {

    private final CardRepository cardRepository;
    private final CardMapper cardMapper;

    public CardServiceImpl(CardRepository cardRepository, CardMapper cardMapper) {
        this.cardRepository = cardRepository;
        this.cardMapper = cardMapper;
    }

    @Override
    public CardDto createCard(CardCreationRequest request) {
        String lastFourCardDigits = getLastFourCardDigits(request.getCardNumber());
        if (cardRepository.findByCardLastFourDigits(lastFourCardDigits).isPresent()) {
            throw new CardAlreadyExistsException(String.format("Card with last four digits %s already exists", lastFourCardDigits));
        }
        CardEntity newCard = cardMapper.toEntity(request);
        newCard = cardRepository.saveAndFlush(newCard);
        return cardMapper.toDto(newCard);
    }

    @Override
    public CardDto updateCard(String lastFourCardDigits, String activate) {
        CardEntity existingCard = cardRepository.findByCardLastFourDigits(lastFourCardDigits)
                .orElseThrow(() -> new CardNotFoundException(String.format("Card with last four digits %s not found", lastFourCardDigits)));
        existingCard = switch (activate) {
            case "activate" -> activateCard(existingCard);
            case "deactivate" -> deactivateCard(existingCard);
            case "expired" -> expireCard(existingCard);
            default -> throw new IllegalArgumentException("Param activate an only be activate,deactivate and expired");
        };
        return cardMapper.toDto(existingCard);
    }

    private CardEntity activateCard(CardEntity existingCard) {
        if (existingCard.getStatus() != CardStatus.ACTIVE) {
            existingCard.setStatus(CardStatus.ACTIVE);
            existingCard = cardRepository.saveAndFlush(existingCard);
        }
        return existingCard;
    }

    private CardEntity deactivateCard(CardEntity existingCard) {
        if (existingCard.getStatus() != CardStatus.BLOCKED) {
            existingCard.setStatus(CardStatus.BLOCKED);
            existingCard = cardRepository.saveAndFlush(existingCard);
        }
        return existingCard;
    }

    private CardEntity expireCard(CardEntity existingCard) {
        if (existingCard.getExpiryDate().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Card is available.Please block it");
        }
        if (existingCard.getStatus() != CardStatus.EXPIRED) {
            existingCard.setStatus(CardStatus.EXPIRED);
            existingCard = cardRepository.saveAndFlush(existingCard);
        }
        return existingCard;
    }

    @Override
    public void deleteCard(String lastFourCardDigits) {
        if(cardRepository.findByCardLastFourDigits(lastFourCardDigits).isPresent()) {
            cardRepository.delete(cardRepository.findByCardLastFourDigits(lastFourCardDigits).get());
        }else{
            throw new CardNotFoundException(String.format("Card with last four digits %s not found", lastFourCardDigits));
        }
    }

    @Override
    public List<CardDto> getAllCards() {
        return cardRepository.findAll().stream().map(cardMapper::toDto).collect(Collectors.toList());
    }

    private String getLastFourCardDigits(String cardNumber) {
        return cardNumber.substring(cardNumber.length() - 4);
    }
}
