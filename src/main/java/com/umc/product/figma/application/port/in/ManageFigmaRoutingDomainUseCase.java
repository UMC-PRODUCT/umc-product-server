package com.umc.product.figma.application.port.in;

import com.umc.product.figma.application.port.in.dto.AddFigmaRoutingMentionCommand;
import com.umc.product.figma.application.port.in.dto.RegisterFigmaRoutingDomainCommand;

public interface ManageFigmaRoutingDomainUseCase {

    /**
     * LLM 분류 결과로 매칭될 신규 라우팅 도메인을 등록한다 (domain_key UNIQUE).
     */
    Long registerDomain(RegisterFigmaRoutingDomainCommand command);

    /**
     * 도메인 삭제 시 mention 도 cascade 로 함께 정리된다 (FK ON DELETE CASCADE).
     */
    void deleteDomain(Long domainId);

    /**
     * 도메인에 mention 대상 (role 또는 user) 한 건 추가.
     */
    Long addMention(AddFigmaRoutingMentionCommand command);

    void removeMention(Long mentionId);
}
