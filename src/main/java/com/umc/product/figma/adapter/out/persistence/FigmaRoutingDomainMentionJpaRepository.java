package com.umc.product.figma.adapter.out.persistence;

import com.umc.product.figma.domain.FigmaRoutingDomainMention;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FigmaRoutingDomainMentionJpaRepository extends JpaRepository<FigmaRoutingDomainMention, Long> {

    List<FigmaRoutingDomainMention> findAllByDomainId(Long domainId);

    List<FigmaRoutingDomainMention> findAllByDomainIdIn(Collection<Long> domainIds);
}
