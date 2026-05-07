package com.umc.product.figma.application.port.out;

import com.umc.product.figma.domain.FigmaRoutingDomain;
import com.umc.product.figma.domain.FigmaRoutingDomainMention;
import java.util.List;
import java.util.Optional;

public interface LoadFigmaRoutingDomainPort {

    Optional<FigmaRoutingDomain> findDomainById(Long id);

    Optional<FigmaRoutingDomain> findDomainByKey(String domainKey);

    boolean existsDomainByKey(String domainKey);

    /**
     * 등록된 모든 도메인. LLM 분류 시 candidate 리스트로도 사용된다.
     */
    List<FigmaRoutingDomain> listAllDomains();

    Optional<FigmaRoutingDomain> findFallbackDomain();

    List<FigmaRoutingDomainMention> listMentionsByDomainId(Long domainId);

    Optional<FigmaRoutingDomainMention> findMentionById(Long mentionId);
}
