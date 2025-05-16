package com.example.bankcards.util.mappers;

import com.example.bankcards.dto.CardBlockingRequestDto;
import com.example.bankcards.entity.CardBlockingRequest;
import org.springframework.stereotype.Component;

@Component
public class CardBlockingRequestMapper {

    public CardBlockingRequestDto toDto(CardBlockingRequest cardBlockingRequest) {
        return CardBlockingRequestDto.builder()
                .Id(cardBlockingRequest.getId())
                .last_four_card_digits(cardBlockingRequest.getCard().getCardLastFourDigits())
                .userId(cardBlockingRequest.getUser().getId())
                .adminId(cardBlockingRequest.getAdmin()==null?null:cardBlockingRequest.getAdmin().getId())
                .createdAt(cardBlockingRequest.getCreatedAt())
                .status(cardBlockingRequest.getStatus())
                .updatedAt(cardBlockingRequest.getUpdatedAt())
                .build();
    }
}
