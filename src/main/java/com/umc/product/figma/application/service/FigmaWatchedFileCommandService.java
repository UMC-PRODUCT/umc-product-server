package com.umc.product.figma.application.service;

import com.umc.product.figma.application.port.in.ManageFigmaWatchedFileUseCase;
import com.umc.product.figma.application.port.in.dto.RegisterFigmaWatchedFileCommand;
import com.umc.product.figma.application.port.out.LoadFigmaWatchedFilePort;
import com.umc.product.figma.application.port.out.SaveFigmaWatchedFilePort;
import com.umc.product.figma.domain.FigmaWatchedFile;
import com.umc.product.figma.domain.exception.FigmaDomainException;
import com.umc.product.figma.domain.exception.FigmaErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class FigmaWatchedFileCommandService implements ManageFigmaWatchedFileUseCase {

    private final LoadFigmaWatchedFilePort loadFigmaWatchedFilePort;
    private final SaveFigmaWatchedFilePort saveFigmaWatchedFilePort;

    @Override
    public Long register(RegisterFigmaWatchedFileCommand command) {
        loadFigmaWatchedFilePort.findByFileKey(command.fileKey())
            .ifPresent(existing -> {
                throw new FigmaDomainException(FigmaErrorCode.WATCHED_FILE_ALREADY_EXISTS);
            });

        FigmaWatchedFile watchedFile = FigmaWatchedFile.of(command.fileKey(), command.displayName());
        return saveFigmaWatchedFilePort.save(watchedFile).getId();
    }

    @Override
    public void enable(Long watchedFileId) {
        FigmaWatchedFile watchedFile = getWatchedFile(watchedFileId);
        watchedFile.enable();
    }

    @Override
    public void disable(Long watchedFileId) {
        FigmaWatchedFile watchedFile = getWatchedFile(watchedFileId);
        watchedFile.disable();
    }

    private FigmaWatchedFile getWatchedFile(Long watchedFileId) {
        return loadFigmaWatchedFilePort.findById(watchedFileId)
            .orElseThrow(() -> new FigmaDomainException(FigmaErrorCode.WATCHED_FILE_NOT_FOUND));
    }
}
