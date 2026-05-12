package com.umc.product.figma.application.port.out;

import com.umc.product.figma.domain.FigmaWatchedFile;

public interface SaveFigmaWatchedFilePort {

    FigmaWatchedFile save(FigmaWatchedFile watchedFile);
}
