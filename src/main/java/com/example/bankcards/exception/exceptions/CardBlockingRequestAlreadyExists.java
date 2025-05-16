package com.example.bankcards.exception.exceptions;

public class CardBlockingRequestAlreadyExists extends RuntimeException {
    public CardBlockingRequestAlreadyExists(String message) {
        super(message);
    }
}
