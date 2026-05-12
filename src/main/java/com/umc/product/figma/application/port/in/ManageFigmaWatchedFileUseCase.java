package com.umc.product.figma.application.port.in;

import com.umc.product.figma.application.port.in.dto.RegisterFigmaWatchedFileCommand;

public interface ManageFigmaWatchedFileUseCase {

    Long register(RegisterFigmaWatchedFileCommand command);

    void enable(Long watchedFileId);

    void disable(Long watchedFileId);
}
