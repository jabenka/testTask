package com.example.bankcards.repository;

import com.example.bankcards.entity.CardEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CardRepository extends JpaRepository<CardEntity, UUID> {
    Optional<CardEntity> findByCardLastFourDigits(String cardLastFourDigits);

    Page<CardEntity> findByUserEntityIdAndCardLastFourDigitsContaining(
            UUID userId,
            String searchQuery,
            Pageable pageable
    );

    Page<CardEntity> findAllByUserEntityId(UUID userId, Pageable pageable);

    boolean existsByCardLastFourDigits(String cardLastFourDigits);

    List<CardEntity> findByCardLastFourDigitsIn(Collection<String> cardLastFourDigits);
}
