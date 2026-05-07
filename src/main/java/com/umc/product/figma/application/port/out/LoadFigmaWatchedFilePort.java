package com.umc.product.figma.application.port.out;

import com.umc.product.figma.domain.FigmaWatchedFile;
import java.util.List;
import java.util.Optional;

public interface LoadFigmaWatchedFilePort {

    Optional<FigmaWatchedFile> findById(Long id);

    Optional<FigmaWatchedFile> findByFileKey(String fileKey);

    List<FigmaWatchedFile> listEnabled(int limit);
}
