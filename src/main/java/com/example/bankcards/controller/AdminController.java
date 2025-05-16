package com.example.bankcards.controller;

import com.example.bankcards.dto.requests.CardCreationRequest;
import com.example.bankcards.dto.requests.RegisterRequest;
import com.example.bankcards.service.AuthService;
import com.example.bankcards.service.CardBlockingService;
import com.example.bankcards.service.CardService;
import com.example.bankcards.service.UserService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;


@RestController
@RequestMapping("/admin")
public class AdminController {

    private final CardService cardService;
    private final UserService userService;
    private final AuthService authService;
    private final CardBlockingService cardBlockingService;

    public AdminController(CardService cardService, UserService userService, AuthService authService, CardBlockingService cardBlockingService) {
        this.cardService = cardService;
        this.userService = userService;
        this.authService = authService;
        this.cardBlockingService = cardBlockingService;
    }

    @PostMapping("/cards/create")
    public ResponseEntity<?> createCard(@Valid @RequestBody CardCreationRequest request) {
        return ResponseEntity.ok().body(cardService.createCard(request));
    }

    @PatchMapping("/cards/update")
    public ResponseEntity<?> updateCard(@RequestParam(name = "last_four_card_digits") String lastFourCardDigits,
                                        @RequestParam(name = "activate") String activate) {
        return ResponseEntity.ok().body(cardService.updateCard(lastFourCardDigits, activate));
    }

    @DeleteMapping("/cards/delete")
    public ResponseEntity<?> deleteCard(@RequestParam(name = "last_four_card_digits") String lastFourCardDigits) {
        cardService.deleteCard(lastFourCardDigits);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/cards/")
    public ResponseEntity<?> getAllCards() {
        return ResponseEntity.ok().body(cardService.getAllCards());
    }

    @GetMapping("/cards/block/requests")
    public ResponseEntity<?> getAllCardsBlockRequests(
            @PageableDefault(size = 10, sort = "Id", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok().body(cardBlockingService.getAllRequests(pageable));
    }
    @PatchMapping("/cards/block/resolve")
    public ResponseEntity<?> resolveCard(@RequestParam(name = "request_id") UUID requestId) {
        return ResponseEntity.ok().body(cardBlockingService.resolveRequest(requestId));
    }


    @GetMapping("/users/")
    public ResponseEntity<?> getAllUsers() {
        return ResponseEntity.ok().body(userService.getAllUsers());
    }

    @PostMapping("/users/add")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @DeleteMapping("/users/delete")
    public ResponseEntity<?> deleteUser(@RequestParam(name = "id") UUID id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
