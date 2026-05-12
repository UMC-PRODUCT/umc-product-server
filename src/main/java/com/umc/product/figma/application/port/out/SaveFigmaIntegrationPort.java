package com.umc.product.figma.application.port.out;

import com.umc.product.figma.domain.FigmaIntegration;

public interface SaveFigmaIntegrationPort {

    FigmaIntegration save(FigmaIntegration integration);
}
