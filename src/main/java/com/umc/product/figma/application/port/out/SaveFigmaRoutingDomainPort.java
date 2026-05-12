package com.umc.product.figma.application.port.out;

import com.umc.product.figma.domain.FigmaRoutingDomain;
import com.umc.product.figma.domain.FigmaRoutingDomainMention;

public interface SaveFigmaRoutingDomainPort {

    FigmaRoutingDomain saveDomain(FigmaRoutingDomain domain);

    void deleteDomain(FigmaRoutingDomain domain);

    FigmaRoutingDomainMention saveMention(FigmaRoutingDomainMention mention);

    void deleteMention(FigmaRoutingDomainMention mention);
}
