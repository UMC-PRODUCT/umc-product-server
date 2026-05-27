package com.umc.product.figma.application.service;

import com.umc.product.figma.application.port.in.GetFigmaWatchedFileUseCase;
import com.umc.product.figma.application.port.in.dto.FigmaWatchedFileInfo;
import com.umc.product.figma.application.port.out.LoadFigmaWatchedFilePort;
import com.umc.product.figma.domain.FigmaWatchedFile;
import com.umc.product.figma.domain.exception.FigmaDomainException;
import com.umc.product.figma.domain.exception.FigmaErrorCode;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FigmaWatchedFileQueryService implements GetFigmaWatchedFileUseCase {

    private final LoadFigmaWatchedFilePort loadFigmaWatchedFilePort;

    @Override
    public FigmaWatchedFileInfo getById(Long watchedFileId) {
        FigmaWatchedFile watchedFile = loadFigmaWatchedFilePort.findById(watchedFileId)
            .orElseThrow(() -> new FigmaDomainException(FigmaErrorCode.WATCHED_FILE_NOT_FOUND));
        return FigmaWatchedFileInfo.from(watchedFile);
    }

    @Override
    public List<FigmaWatchedFileInfo> listAll(Boolean enabledFilter) {
        return loadFigmaWatchedFilePort.listAll(enabledFilter).stream()
            .map(FigmaWatchedFileInfo::from)
            .toList();
    }
}
