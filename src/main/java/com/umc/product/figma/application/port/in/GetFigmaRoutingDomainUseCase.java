package com.umc.product.figma.application.port.in;

import com.umc.product.figma.application.port.in.dto.FigmaRoutingDomainMentionInfo;
import com.umc.product.figma.application.port.in.dto.FigmaRoutingDomainSummaryInfo;
import java.util.List;

public interface GetFigmaRoutingDomainUseCase {

    /**
     * 라우팅 도메인 단건 조회. mention 까지 함께 채워서 반환한다. 미존재 시 {@link com.umc.product.figma.domain.exception.FigmaErrorCode#ROUTING_DOMAIN_NOT_FOUND}
     * 예외를 던진다.
     */
    FigmaRoutingDomainSummaryInfo getDomainById(Long domainId);

    /**
     * 등록된 모든 라우팅 도메인. mention 본문은 포함하지 않고 mention 개수만 채운다.
     */
    List<FigmaRoutingDomainSummaryInfo> listDomains();

    /**
     * 도메인의 mention 목록만 단독으로 조회한다. 도메인 미존재 시 예외.
     */
    List<FigmaRoutingDomainMentionInfo> listMentionsByDomainId(Long domainId);
}
