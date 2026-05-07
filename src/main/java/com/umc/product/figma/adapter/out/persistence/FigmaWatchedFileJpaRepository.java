package com.umc.product.figma.adapter.out.persistence;

import com.umc.product.figma.domain.FigmaWatchedFile;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FigmaWatchedFileJpaRepository extends JpaRepository<FigmaWatchedFile, Long> {

    Optional<FigmaWatchedFile> findByFileKey(String fileKey);

    List<FigmaWatchedFile> findAllByEnabledTrueOrderByIdAsc(Pageable pageable);

    boolean existsByFileKey(String fileKey);
}
