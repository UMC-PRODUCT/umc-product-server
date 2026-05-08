package com.umc.product.figma.application.port.out;

import com.umc.product.figma.domain.FigmaRoutingDomain;
import com.umc.product.figma.domain.FigmaRoutingDomainMention;
import java.util.Collection;
import java.util.List;
import java.util.Map;
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

    /**
     * 여러 도메인의 mentions 를 한 번의 IN 쿼리로 일괄 조회한다. 도메인 루프 내 N+1 방지용.
     *
     * @return domainId → mentions 맵 (mention 없는 도메인은 빈 리스트로 포함)
     */
    Map<Long, List<FigmaRoutingDomainMention>> listMentionsByDomainIds(Collection<Long> domainIds);

    Optional<FigmaRoutingDomainMention> findMentionById(Long mentionId);
}
