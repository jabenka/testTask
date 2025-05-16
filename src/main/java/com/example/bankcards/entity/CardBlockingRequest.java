package com.example.bankcards.entity;

import com.example.bankcards.entity.enums.BlockRequestStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "card_block_requests")
@Getter
@Setter
@ToString
@EqualsAndHashCode
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CardBlockingRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID Id;

    @ManyToOne
    @JoinColumn(name = "card_id", nullable = false)
    CardEntity card;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    UserEntity user;

    @ManyToOne
    @JoinColumn(name = "admin_id")
    UserEntity admin;

    @Enumerated(EnumType.STRING)
    BlockRequestStatus status;

    @CreationTimestamp
    LocalDate createdAt;

    @UpdateTimestamp
    LocalDate updatedAt;

}