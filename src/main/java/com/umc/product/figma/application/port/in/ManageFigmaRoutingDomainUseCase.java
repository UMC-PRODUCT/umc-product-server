package com.umc.product.figma.application.port.in;

import com.umc.product.figma.application.port.in.dto.AddFigmaRoutingMentionCommand;
import com.umc.product.figma.application.port.in.dto.RegisterFigmaRoutingDomainCommand;
import com.umc.product.figma.application.port.in.dto.UpdateFigmaRoutingDomainCommand;
import com.umc.product.figma.application.port.in.dto.UpdateFigmaRoutingMentionCommand;

public interface ManageFigmaRoutingDomainUseCase {

    /**
     * LLM 분류 결과로 매칭될 신규 라우팅 도메인을 등록한다 (domain_key UNIQUE).
     */
    Long registerDomain(RegisterFigmaRoutingDomainCommand command);

    /**
     * 도메인의 설명(description), Discord webhook URL, fallback 여부를 수정한다. domain_key 는 LLM 분류 키이므로 변경 불가.
     */
    void updateDomain(UpdateFigmaRoutingDomainCommand command);

    /**
     * 도메인 삭제 시 mention 도 cascade 로 함께 정리된다 (FK ON DELETE CASCADE).
     */
    void deleteDomain(Long domainId);

    /**
     * 도메인에 mention 대상 (role 또는 user) 한 건 추가.
     */
    Long addMention(AddFigmaRoutingMentionCommand command);

    /**
     * mention 의 Discord ID(mentionId) 와 표시 라벨(displayLabel) 을 수정한다. mention_type 변경은 삭제 후 재등록으로 처리한다.
     */
    void updateMention(UpdateFigmaRoutingMentionCommand command);

    void removeMention(Long mentionId);
}
