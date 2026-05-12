package com.umc.product.figma.application.service;

import com.umc.product.figma.application.port.in.GetFigmaRoutingDomainUseCase;
import com.umc.product.figma.application.port.in.dto.FigmaRoutingDomainMentionInfo;
import com.umc.product.figma.application.port.in.dto.FigmaRoutingDomainSummaryInfo;
import com.umc.product.figma.application.port.out.LoadFigmaRoutingDomainPort;
import com.umc.product.figma.domain.FigmaRoutingDomain;
import com.umc.product.figma.domain.FigmaRoutingDomainMention;
import com.umc.product.figma.domain.exception.FigmaDomainException;
import com.umc.product.figma.domain.exception.FigmaErrorCode;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FigmaRoutingDomainQueryService implements GetFigmaRoutingDomainUseCase {

    private final LoadFigmaRoutingDomainPort loadFigmaRoutingDomainPort;

    @Override
    public FigmaRoutingDomainSummaryInfo getDomainById(Long domainId) {
        FigmaRoutingDomain domain = loadFigmaRoutingDomainPort.findDomainById(domainId)
            .orElseThrow(() -> new FigmaDomainException(FigmaErrorCode.ROUTING_DOMAIN_NOT_FOUND));

        List<FigmaRoutingDomainMentionInfo> mentions = loadFigmaRoutingDomainPort
            .listMentionsByDomainId(domain.getId())
            .stream()
            .map(FigmaRoutingDomainQueryService::toMentionInfo)
            .toList();

        return FigmaRoutingDomainSummaryInfo.detail(
            domain.getId(),
            domain.getDomainKey(),
            domain.getDescription(),
            domain.getDiscordWebhookUrl(),
            domain.isFallback(),
            mentions
        );
    }

    @Override
    public List<FigmaRoutingDomainSummaryInfo> listDomains() {
        return loadFigmaRoutingDomainPort.listAllDomains().stream()
            .map(domain -> FigmaRoutingDomainSummaryInfo.listItem(
                domain.getId(),
                domain.getDomainKey(),
                domain.getDescription(),
                domain.getDiscordWebhookUrl(),
                domain.isFallback(),
                loadFigmaRoutingDomainPort.listMentionsByDomainId(domain.getId()).size()
            ))
            .toList();
    }

    @Override
    public List<FigmaRoutingDomainMentionInfo> listMentionsByDomainId(Long domainId) {
        loadFigmaRoutingDomainPort.findDomainById(domainId)
            .orElseThrow(() -> new FigmaDomainException(FigmaErrorCode.ROUTING_DOMAIN_NOT_FOUND));

        return loadFigmaRoutingDomainPort.listMentionsByDomainId(domainId).stream()
            .map(FigmaRoutingDomainQueryService::toMentionInfo)
            .toList();
    }

    private static FigmaRoutingDomainMentionInfo toMentionInfo(FigmaRoutingDomainMention mention) {
        return new FigmaRoutingDomainMentionInfo(
            mention.getId(),
            mention.getDomainId(),
            mention.getMentionId(),
            mention.getMentionType(),
            mention.getDisplayLabel()
        );
    }
}
