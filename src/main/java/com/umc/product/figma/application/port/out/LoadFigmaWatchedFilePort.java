package com.umc.product.figma.application.port.out;

import com.umc.product.figma.domain.FigmaWatchedFile;
import java.util.List;
import java.util.Optional;

public interface LoadFigmaWatchedFilePort {

    Optional<FigmaWatchedFile> findById(Long id);

    Optional<FigmaWatchedFile> findByFileKey(String fileKey);

    List<FigmaWatchedFile> listEnabled(int limit);

    /**
     * 운영 화면용 전체 조회. {@code enabledFilter} 가 {@code null} 이면 전체, {@code true|false} 면 해당 상태로 필터링한다.
     */
    List<FigmaWatchedFile> listAll(Boolean enabledFilter);
}
