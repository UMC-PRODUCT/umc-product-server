package com.umc.product.figma.adapter.out.persistence;

import com.umc.product.figma.domain.FigmaIntegration;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

public interface FigmaIntegrationJpaRepository extends JpaRepository<FigmaIntegration, Long> {

    Optional<FigmaIntegration> findFirstByOrderByUpdatedAtDesc();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT f FROM FigmaIntegration f ORDER BY f.updatedAt DESC LIMIT 1")
    Optional<FigmaIntegration> findFirstForUpdate();

    Optional<FigmaIntegration> findByOwnerMemberId(Long ownerMemberId);
}
