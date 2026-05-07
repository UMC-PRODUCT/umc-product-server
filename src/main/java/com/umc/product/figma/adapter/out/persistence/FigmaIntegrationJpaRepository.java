package com.umc.product.figma.adapter.out.persistence;

import com.umc.product.figma.domain.FigmaIntegration;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FigmaIntegrationJpaRepository extends JpaRepository<FigmaIntegration, Long> {

    Optional<FigmaIntegration> findFirstByOrderByUpdatedAtDesc();

    Optional<FigmaIntegration> findByOwnerMemberId(Long ownerMemberId);
}
