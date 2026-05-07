package com.umc.product.figma.application.service;

import com.umc.product.figma.application.port.in.ManageFigmaRoutingDomainUseCase;
import com.umc.product.figma.application.port.in.dto.AddFigmaRoutingMentionCommand;
import com.umc.product.figma.application.port.in.dto.RegisterFigmaRoutingDomainCommand;
import com.umc.product.figma.application.port.out.LoadFigmaRoutingDomainPort;
import com.umc.product.figma.application.port.out.SaveFigmaRoutingDomainPort;
import com.umc.product.figma.domain.FigmaRoutingDomain;
import com.umc.product.figma.domain.FigmaRoutingDomainMention;
import com.umc.product.figma.domain.exception.FigmaDomainException;
import com.umc.product.figma.domain.exception.FigmaErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class FigmaRoutingDomainCommandService implements ManageFigmaRoutingDomainUseCase {

    private final LoadFigmaRoutingDomainPort loadFigmaRoutingDomainPort;
    private final SaveFigmaRoutingDomainPort saveFigmaRoutingDomainPort;

    @Override
    public Long registerDomain(RegisterFigmaRoutingDomainCommand command) {
        if (loadFigmaRoutingDomainPort.existsDomainByKey(command.domainKey())) {
            throw new FigmaDomainException(FigmaErrorCode.ROUTING_DOMAIN_ALREADY_EXISTS);
        }
        FigmaRoutingDomain domain = FigmaRoutingDomain.of(
            command.domainKey(),
            command.description(),
            command.discordWebhookUrl(),
            command.fallback()
        );
        return saveFigmaRoutingDomainPort.saveDomain(domain).getId();
    }

    @Override
    public void deleteDomain(Long domainId) {
        FigmaRoutingDomain domain = loadFigmaRoutingDomainPort.findDomainById(domainId)
            .orElseThrow(() -> new FigmaDomainException(FigmaErrorCode.ROUTING_DOMAIN_NOT_FOUND));
        saveFigmaRoutingDomainPort.deleteDomain(domain);
    }

    @Override
    public Long addMention(AddFigmaRoutingMentionCommand command) {
        loadFigmaRoutingDomainPort.findDomainById(command.domainId())
            .orElseThrow(() -> new FigmaDomainException(FigmaErrorCode.ROUTING_DOMAIN_NOT_FOUND));

        FigmaRoutingDomainMention mention = FigmaRoutingDomainMention.of(
            command.domainId(),
            command.mentionId(),
            command.mentionType(),
            command.displayLabel()
        );
        return saveFigmaRoutingDomainPort.saveMention(mention).getId();
    }

    @Override
    public void removeMention(Long mentionId) {
        FigmaRoutingDomainMention mention = loadFigmaRoutingDomainPort.findMentionById(mentionId)
            .orElseThrow(() -> new FigmaDomainException(FigmaErrorCode.ROUTING_DOMAIN_MENTION_NOT_FOUND));
        saveFigmaRoutingDomainPort.deleteMention(mention);
    }
}
