package com.umc.product.figma.application.port.in;

import com.umc.product.figma.application.port.in.dto.FigmaWatchedFileInfo;
import java.util.List;

public interface GetFigmaWatchedFileUseCase {

    /**
     * 등록된 watched file 단건 조회. 미존재 시 {@link com.umc.product.figma.domain.exception.FigmaErrorCode#WATCHED_FILE_NOT_FOUND} 예외.
     */
    FigmaWatchedFileInfo getById(Long watchedFileId);

    /**
     * 등록된 watched file 전체. {@code enabledFilter} 가 null 이면 전체, true/false 면 해당 상태로 필터링한다.
     */
    List<FigmaWatchedFileInfo> listAll(Boolean enabledFilter);
}
