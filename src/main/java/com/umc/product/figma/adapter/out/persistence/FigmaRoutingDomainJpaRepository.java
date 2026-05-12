package com.umc.product.figma.adapter.out.persistence;

import com.umc.product.figma.domain.FigmaRoutingDomain;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FigmaRoutingDomainJpaRepository extends JpaRepository<FigmaRoutingDomain, Long> {

    Optional<FigmaRoutingDomain> findByDomainKey(String domainKey);

    boolean existsByDomainKey(String domainKey);

    List<FigmaRoutingDomain> findAllByOrderByDomainKeyAsc();

    Optional<FigmaRoutingDomain> findFirstByFallbackTrue();
}
