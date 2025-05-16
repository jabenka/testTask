package com.example.bankcards.service;

import com.example.bankcards.dto.CardDto;
import com.example.bankcards.dto.requests.CardCreationRequest;

import java.util.List;

public interface CardService {
    CardDto createCard(CardCreationRequest request);

    CardDto updateCard(String cardNumber, String activate);

    void deleteCard(String cardNumber);

    List<CardDto> getAllCards();
}
