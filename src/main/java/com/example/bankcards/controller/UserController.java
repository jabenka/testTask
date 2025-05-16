package com.example.bankcards.controller;

import com.example.bankcards.dto.requests.BalanceRequest;
import com.example.bankcards.dto.requests.TransferRequest;
import com.example.bankcards.service.CardBlockingService;
import com.example.bankcards.service.UserService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
public class UserController {
    private final UserService userService;
    private final CardBlockingService cardBlockingService;

    public UserController(UserService userService, CardBlockingService cardBlockingService) {
        this.userService = userService;
        this.cardBlockingService = cardBlockingService;
    }

    @GetMapping("/cards/get")
    public ResponseEntity<?> getCards(
            @PageableDefault(size = 10, sort = "Id", direction = Sort.Direction.DESC) Pageable pageable,
            @RequestParam(name = "search_query", required = false) String searchQuery) {
        return ResponseEntity.ok().body(userService.getCards(pageable, searchQuery));
    }

    @PostMapping("/cards/block")
    public ResponseEntity<?> blockCard(@RequestParam(name = "last_four_card_digits") String lastFourCardDigits) {
        return ResponseEntity.ok().body(cardBlockingService.createBlockRequest(lastFourCardDigits));
    }

    @GetMapping("/cards/balance")
    public ResponseEntity<?> getBalance(@RequestBody(required = true) BalanceRequest request) {
        return ResponseEntity.ok().body(userService.getBalance(request));
    }

    @PostMapping("/cards/transfer")
    public ResponseEntity<?> transfer(@RequestBody @Valid TransferRequest request) {
        return ResponseEntity.ok().body(userService.transfer(request));
    }
}
