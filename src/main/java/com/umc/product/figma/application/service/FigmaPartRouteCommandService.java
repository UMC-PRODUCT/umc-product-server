package com.umc.product.figma.application.service;

import com.umc.product.figma.application.port.in.ManageFigmaPartRouteUseCase;
import com.umc.product.figma.application.port.in.dto.RegisterFigmaPartRouteCommand;
import com.umc.product.figma.application.port.out.LoadFigmaPartRoutePort;
import com.umc.product.figma.application.port.out.SaveFigmaPartRoutePort;
import com.umc.product.figma.domain.FigmaPartRoute;
import com.umc.product.figma.domain.exception.FigmaDomainException;
import com.umc.product.figma.domain.exception.FigmaErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class FigmaPartRouteCommandService implements ManageFigmaPartRouteUseCase {

    private final LoadFigmaPartRoutePort loadFigmaPartRoutePort;
    private final SaveFigmaPartRoutePort saveFigmaPartRoutePort;

    @Override
    public Long register(RegisterFigmaPartRouteCommand command) {
        if (loadFigmaPartRoutePort.existsByFileKeyAndPageName(command.fileKey(), command.pageName())) {
            throw new FigmaDomainException(FigmaErrorCode.PART_ROUTE_ALREADY_EXISTS);
        }
        FigmaPartRoute route = FigmaPartRoute.of(
            command.fileKey(),
            command.pageName(),
            command.partKey(),
            command.discordRoleId(),
            command.discordWebhookUrl(),
            command.fallback()
        );
        return saveFigmaPartRoutePort.save(route).getId();
    }

    @Override
    public void delete(Long routeId) {
        FigmaPartRoute route = loadFigmaPartRoutePort.findRouteById(routeId)
            .orElseThrow(() -> new FigmaDomainException(FigmaErrorCode.PART_ROUTE_NOT_FOUND));
        saveFigmaPartRoutePort.delete(route);
    }
}
