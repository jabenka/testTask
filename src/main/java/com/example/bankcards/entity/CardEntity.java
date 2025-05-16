package com.example.bankcards.entity;

import com.example.bankcards.entity.enums.CardStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "cards")
@Setter
@Getter
@ToString
@EqualsAndHashCode
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CardEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID Id;

    @Column(unique = true, nullable = false)
    String cardNumber;

    @ManyToOne
    @JoinColumn(name = "owner_id", nullable = false)
    UserEntity userEntity;

    LocalDate expiryDate;

    @Column(name = "card_last_four_digits",unique = true, nullable = false)
    String cardLastFourDigits;

    @Enumerated(EnumType.STRING)
    CardStatus status;

    BigDecimal balance;

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
        this.cardLastFourDigits = cardNumber.substring(cardNumber.length() - 4);
    }
}
