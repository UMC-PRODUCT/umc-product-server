package com.umc.product.figma.application.port.out;

import com.umc.product.figma.domain.FigmaPartRoute;

public interface SaveFigmaPartRoutePort {

    FigmaPartRoute save(FigmaPartRoute partRoute);

    void delete(FigmaPartRoute partRoute);
}
