package com.example.bankcards.service;

import com.example.bankcards.dto.CardBlockingRequestDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface CardBlockingService {

    CardBlockingRequestDto createBlockRequest(String lastFourCardDigits);

    Page<CardBlockingRequestDto> getAllRequests(Pageable pageable);

    CardBlockingRequestDto resolveRequest(UUID requestId);
}
