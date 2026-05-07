package com.umc.product.figma.adapter.out.persistence;

import com.umc.product.figma.application.port.out.LoadFigmaIntegrationPort;
import com.umc.product.figma.application.port.out.LoadFigmaPartRoutePort;
import com.umc.product.figma.application.port.out.LoadFigmaRoutingDomainPort;
import com.umc.product.figma.application.port.out.LoadFigmaWatchedFilePort;
import com.umc.product.figma.application.port.out.SaveFigmaIntegrationPort;
import com.umc.product.figma.application.port.out.SaveFigmaPartRoutePort;
import com.umc.product.figma.application.port.out.SaveFigmaRoutingDomainPort;
import com.umc.product.figma.application.port.out.SaveFigmaWatchedFilePort;
import com.umc.product.figma.domain.FigmaIntegration;
import com.umc.product.figma.domain.FigmaPartRoute;
import com.umc.product.figma.domain.FigmaRoutingDomain;
import com.umc.product.figma.domain.FigmaRoutingDomainMention;
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
    LoadFigmaPartRoutePort,
    SaveFigmaPartRoutePort,
    LoadFigmaRoutingDomainPort,
    SaveFigmaRoutingDomainPort {

    private final FigmaIntegrationJpaRepository figmaIntegrationJpaRepository;
    private final FigmaWatchedFileJpaRepository figmaWatchedFileJpaRepository;
    private final FigmaPartRouteJpaRepository figmaPartRouteJpaRepository;
    private final FigmaRoutingDomainJpaRepository figmaRoutingDomainJpaRepository;
    private final FigmaRoutingDomainMentionJpaRepository figmaRoutingDomainMentionJpaRepository;

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
    public Optional<FigmaPartRoute> findRouteById(Long id) {
        return figmaPartRouteJpaRepository.findById(id);
    }

    @Override
    public boolean existsByFileKeyAndPageName(String fileKey, String pageName) {
        return figmaPartRouteJpaRepository.existsByFileKeyAndPageName(fileKey, pageName);
    }

    @Override
    public List<FigmaPartRoute> listByFileKey(String fileKey) {
        return figmaPartRouteJpaRepository.findAllByFileKey(fileKey);
    }

    @Override
    public Optional<FigmaPartRoute> findFallbackByFileKey(String fileKey) {
        return figmaPartRouteJpaRepository.findFirstByFileKeyAndFallbackTrue(fileKey);
    }

    @Override
    public FigmaPartRoute save(FigmaPartRoute partRoute) {
        return figmaPartRouteJpaRepository.save(partRoute);
    }

    @Override
    public void delete(FigmaPartRoute partRoute) {
        figmaPartRouteJpaRepository.delete(partRoute);
    }

    @Override
    public Optional<FigmaRoutingDomain> findDomainById(Long id) {
        return figmaRoutingDomainJpaRepository.findById(id);
    }

    @Override
    public Optional<FigmaRoutingDomain> findDomainByKey(String domainKey) {
        return figmaRoutingDomainJpaRepository.findByDomainKey(domainKey);
    }

    @Override
    public boolean existsDomainByKey(String domainKey) {
        return figmaRoutingDomainJpaRepository.existsByDomainKey(domainKey);
    }

    @Override
    public List<FigmaRoutingDomain> listAllDomains() {
        return figmaRoutingDomainJpaRepository.findAllByOrderByDomainKeyAsc();
    }

    @Override
    public Optional<FigmaRoutingDomain> findFallbackDomain() {
        return figmaRoutingDomainJpaRepository.findFirstByFallbackTrue();
    }

    @Override
    public List<FigmaRoutingDomainMention> listMentionsByDomainId(Long domainId) {
        return figmaRoutingDomainMentionJpaRepository.findAllByDomainId(domainId);
    }

    @Override
    public Optional<FigmaRoutingDomainMention> findMentionById(Long mentionId) {
        return figmaRoutingDomainMentionJpaRepository.findById(mentionId);
    }

    @Override
    public FigmaRoutingDomain saveDomain(FigmaRoutingDomain domain) {
        return figmaRoutingDomainJpaRepository.save(domain);
    }

    @Override
    public void deleteDomain(FigmaRoutingDomain domain) {
        figmaRoutingDomainJpaRepository.delete(domain);
    }

    @Override
    public FigmaRoutingDomainMention saveMention(FigmaRoutingDomainMention mention) {
        return figmaRoutingDomainMentionJpaRepository.save(mention);
    }

    @Override
    public void deleteMention(FigmaRoutingDomainMention mention) {
        figmaRoutingDomainMentionJpaRepository.delete(mention);
    }
}
