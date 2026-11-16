package com.umc.product.challenger.adapter.out.persistence;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import com.umc.product.challenger.application.port.out.DeleteChallengerDeactivationPort;
import com.umc.product.challenger.application.port.out.LoadChallengerDeactivationPort;
import com.umc.product.challenger.application.port.out.SaveChallengerDeactivationPort;
import com.umc.product.challenger.application.port.out.dto.ChallengerDeactivationRetentionTarget;
import com.umc.product.challenger.domain.ChallengerDeactivation;
import com.umc.product.challenger.domain.ChallengerDeactivationHistory;
import com.umc.product.challenger.domain.ChallengerDeactivationImage;
import com.umc.product.challenger.domain.ChallengerDeactivationLink;
import com.umc.product.challenger.domain.exception.ChallengerDomainException;
import com.umc.product.challenger.domain.exception.ChallengerErrorCode;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ChallengerDeactivationPersistenceAdapter
    implements LoadChallengerDeactivationPort, SaveChallengerDeactivationPort, DeleteChallengerDeactivationPort {

    private final ChallengerDeactivationJpaRepository deactivationRepository;
    private final ChallengerDeactivationHistoryJpaRepository historyRepository;
    private final ChallengerDeactivationImageJpaRepository imageRepository;
    private final ChallengerDeactivationLinkJpaRepository linkRepository;

    @Override
    public boolean existsByChallengerId(Long challengerId) {
        return deactivationRepository.existsByChallengerId(challengerId);
    }

    @Override
    public ChallengerDeactivation getByChallengerIdForUpdate(Long challengerId) {
        return deactivationRepository.findByChallengerId(challengerId)
            .orElseThrow(() -> new ChallengerDomainException(
                ChallengerErrorCode.CHALLENGER_DEACTIVATION_NOT_FOUND
            ));
    }

    @Override
    public long countImagesByDeactivationId(Long deactivationId) {
        return imageRepository.countByAddedHistoryDeactivationId(deactivationId);
    }

    @Override
    public long countLinksByDeactivationId(Long deactivationId) {
        return linkRepository.countByAddedHistoryDeactivationId(deactivationId);
    }

    @Override
    public boolean existsAnyImageByDeactivationId(Long deactivationId, List<String> imageIds) {
        return !imageIds.isEmpty()
            && imageRepository.existsByImageIdInAndAddedHistoryDeactivationId(imageIds, deactivationId);
    }

    @Override
    public boolean existsAnyLinkByDeactivationId(Long deactivationId, List<String> links) {
        return !links.isEmpty()
            && linkRepository.existsByLinkInAndAddedHistoryDeactivationId(links, deactivationId);
    }

    @Override
    public boolean existsByImageId(String imageId) {
        return imageRepository.existsByImageId(imageId);
    }

    @Override
    public boolean existsImageReferenceOutsideDeactivation(String imageId, Long deactivationId) {
        return imageRepository.existsByImageIdAndAddedHistoryDeactivationIdNot(imageId, deactivationId);
    }

    @Override
    public List<ChallengerDeactivationRetentionTarget> listRetentionTargets(
        Instant createdAtThreshold,
        Long afterId,
        int limit
    ) {
        List<ChallengerDeactivation> deactivations = deactivationRepository
            .findByCreatedAtLessThanEqualAndIdGreaterThanOrderByIdAsc(
                createdAtThreshold,
                afterId,
                PageRequest.of(0, limit)
            );
        if (deactivations.isEmpty()) {
            return List.of();
        }

        Map<Long, List<String>> imageIdsByDeactivationId = new LinkedHashMap<>();
        deactivations.forEach(deactivation -> imageIdsByDeactivationId.put(
            deactivation.getId(),
            new ArrayList<>()
        ));
        imageRepository.findAllWithDeactivationByDeactivationIdIn(
            deactivations.stream().map(ChallengerDeactivation::getId).toList()
        ).forEach(image -> imageIdsByDeactivationId
            .get(image.getAddedHistory().getDeactivation().getId())
            .add(image.getImageId()));

        return deactivations.stream()
            .map(deactivation -> new ChallengerDeactivationRetentionTarget(
                deactivation.getId(),
                List.copyOf(imageIdsByDeactivationId.get(deactivation.getId()))
            ))
            .toList();
    }

    @Override
    public ChallengerDeactivation save(ChallengerDeactivation deactivation) {
        try {
            return deactivationRepository.saveAndFlush(deactivation);
        } catch (DataIntegrityViolationException exception) {
            throw new ChallengerDomainException(
                ChallengerErrorCode.CHALLENGER_DEACTIVATION_ALREADY_EXISTS,
                exception
            );
        }
    }

    @Override
    public ChallengerDeactivationHistory saveHistory(ChallengerDeactivationHistory history) {
        return historyRepository.save(history);
    }

    @Override
    public void saveImages(List<ChallengerDeactivationImage> images) {
        imageRepository.saveAll(images);
    }

    @Override
    public void saveLinks(List<ChallengerDeactivationLink> links) {
        linkRepository.saveAll(links);
    }

    @Override
    public void deleteImagesByDeactivationId(Long deactivationId) {
        imageRepository.deleteByDeactivationId(deactivationId);
    }

    @Override
    public void deleteLinksByDeactivationId(Long deactivationId) {
        linkRepository.deleteByDeactivationId(deactivationId);
    }

    @Override
    public void deleteHistoriesByDeactivationId(Long deactivationId) {
        historyRepository.deleteByDeactivationId(deactivationId);
    }

    @Override
    public void deleteById(Long deactivationId) {
        deactivationRepository.deleteById(deactivationId);
    }
}
