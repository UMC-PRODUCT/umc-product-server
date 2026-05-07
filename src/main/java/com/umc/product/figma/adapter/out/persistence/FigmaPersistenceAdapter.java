package com.umc.product.figma.adapter.out.persistence;

import com.umc.product.figma.application.port.out.LoadFigmaIntegrationPort;
import com.umc.product.figma.application.port.out.LoadFigmaPartRoutePort;
import com.umc.product.figma.application.port.out.LoadFigmaWatchedFilePort;
import com.umc.product.figma.application.port.out.SaveFigmaIntegrationPort;
import com.umc.product.figma.application.port.out.SaveFigmaWatchedFilePort;
import com.umc.product.figma.domain.FigmaIntegration;
import com.umc.product.figma.domain.FigmaPartRoute;
import com.umc.product.figma.domain.FigmaWatchedFile;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FigmaPersistenceAdapter implements
    LoadFigmaIntegrationPort,
    SaveFigmaIntegrationPort,
    LoadFigmaWatchedFilePort,
    SaveFigmaWatchedFilePort,
    LoadFigmaPartRoutePort {

    private final FigmaIntegrationJpaRepository figmaIntegrationJpaRepository;
    private final FigmaWatchedFileJpaRepository figmaWatchedFileJpaRepository;
    private final FigmaPartRouteJpaRepository figmaPartRouteJpaRepository;

    @Override
    public Optional<FigmaIntegration> findActive() {
        return figmaIntegrationJpaRepository.findFirstByOrderByUpdatedAtDesc();
    }

    @Override
    public Optional<FigmaIntegration> findByOwnerMemberId(Long ownerMemberId) {
        return figmaIntegrationJpaRepository.findByOwnerMemberId(ownerMemberId);
    }

    @Override
    public FigmaIntegration save(FigmaIntegration integration) {
        return figmaIntegrationJpaRepository.save(integration);
    }

    @Override
    public Optional<FigmaWatchedFile> findById(Long id) {
        return figmaWatchedFileJpaRepository.findById(id);
    }

    @Override
    public Optional<FigmaWatchedFile> findByFileKey(String fileKey) {
        return figmaWatchedFileJpaRepository.findByFileKey(fileKey);
    }

    @Override
    public List<FigmaWatchedFile> listEnabled(int limit) {
        return figmaWatchedFileJpaRepository.findAllByEnabledTrueOrderByIdAsc(PageRequest.of(0, limit));
    }

    @Override
    public FigmaWatchedFile save(FigmaWatchedFile watchedFile) {
        return figmaWatchedFileJpaRepository.save(watchedFile);
    }

    @Override
    public List<FigmaPartRoute> listByFileKey(String fileKey) {
        return figmaPartRouteJpaRepository.findAllByFileKey(fileKey);
    }

    @Override
    public Optional<FigmaPartRoute> findFallbackByFileKey(String fileKey) {
        return figmaPartRouteJpaRepository.findFirstByFileKeyAndFallbackTrue(fileKey);
    }
}
