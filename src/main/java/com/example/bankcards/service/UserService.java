package com.example.bankcards.service;

import com.example.bankcards.dto.CardDto;
import com.example.bankcards.dto.UserDto;
import com.example.bankcards.dto.requests.BalanceRequest;
import com.example.bankcards.dto.requests.TransferRequest;
import com.example.bankcards.dto.responses.BalanceResponse;
import com.example.bankcards.dto.responses.TransferResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface UserService {
    List<UserDto> getAllUsers();

    void deleteUser(UUID id);

    Page<CardDto> getCards(Pageable pageable, String searchQuery);

    List<BalanceResponse> getBalance(BalanceRequest request);

    TransferResponse transfer(TransferRequest request);
}
