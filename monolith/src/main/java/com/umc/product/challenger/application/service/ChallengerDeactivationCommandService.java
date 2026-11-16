package com.umc.product.challenger.application.service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.umc.product.authorization.application.port.in.command.EvictAuthoritySnapshotCacheUseCase;
import com.umc.product.challenger.application.port.in.command.AddChallengerDeactivationEvidenceUseCase;
import com.umc.product.challenger.application.port.in.command.RegisterChallengerDeactivationUseCase;
import com.umc.product.challenger.application.port.in.command.dto.AddChallengerDeactivationEvidenceCommand;
import com.umc.product.challenger.application.port.in.command.dto.RegisterChallengerDeactivationCommand;
import com.umc.product.challenger.application.port.out.LoadChallengerDeactivationPort;
import com.umc.product.challenger.application.port.out.LoadChallengerPort;
import com.umc.product.challenger.application.port.out.SaveChallengerDeactivationPort;
import com.umc.product.challenger.application.port.out.SaveChallengerPort;
import com.umc.product.challenger.domain.Challenger;
import com.umc.product.challenger.domain.ChallengerDeactivation;
import com.umc.product.challenger.domain.ChallengerDeactivationHistory;
import com.umc.product.challenger.domain.ChallengerDeactivationImage;
import com.umc.product.challenger.domain.ChallengerDeactivationLink;
import com.umc.product.challenger.domain.enums.ChallengerDeactivationHistoryAction;
import com.umc.product.challenger.domain.exception.ChallengerDomainException;
import com.umc.product.challenger.domain.exception.ChallengerErrorCode;
import com.umc.product.storage.application.port.in.query.GetFileUseCase;
import com.umc.product.storage.application.port.in.query.dto.FileMetadataInfo;
import com.umc.product.storage.domain.enums.FileCategory;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class ChallengerDeactivationCommandService
    implements RegisterChallengerDeactivationUseCase, AddChallengerDeactivationEvidenceUseCase {

    private final LoadChallengerPort loadChallengerPort;
    private final SaveChallengerPort saveChallengerPort;
    private final LoadChallengerDeactivationPort loadChallengerDeactivationPort;
    private final SaveChallengerDeactivationPort saveChallengerDeactivationPort;
    private final GetFileUseCase getFileUseCase;
    private final EvictAuthoritySnapshotCacheUseCase evictAuthoritySnapshotCacheUseCase;

    @Override
    public Long register(RegisterChallengerDeactivationCommand command) {
        Challenger challenger = loadChallengerPort.getById(command.challengerId());
        validateNotRegistered(command.challengerId());
        validateNoDuplicateEvidence(command.imageIds(), command.links());

        List<FileMetadataInfo> files = getFileUseCase.batchGetUsableByIds(
            command.imageIds(),
            command.registeredByMemberId()
        );
        validateEvidenceFileCategory(files);

        ChallengerDeactivation deactivation = ChallengerDeactivation.create(
            challenger.getId(),
            command.reason() == null ? null : command.reason().getStatus(),
            command.reason(),
            command.reasonDetail(),
            command.memo(),
            command.registeredByMemberId()
        );
        challenger.changeStatus(
            deactivation.getStatus(),
            command.registeredByMemberId(),
            deactivation.getReason().name()
        );
        ChallengerDeactivationHistory history = ChallengerDeactivationHistory.create(
            deactivation,
            ChallengerDeactivationHistoryAction.REGISTERED,
            command.imageIds().size(),
            command.links().size(),
            command.registeredByMemberId()
        );
        List<ChallengerDeactivationImage> images = createImages(history, command.imageIds());
        List<ChallengerDeactivationLink> links = createLinks(history, command.links());

        saveChallengerPort.save(challenger);
        ChallengerDeactivation savedDeactivation = saveChallengerDeactivationPort.save(deactivation);
        saveChallengerDeactivationPort.saveHistory(history);
        saveChallengerDeactivationPort.saveImages(images);
        saveChallengerDeactivationPort.saveLinks(links);
        evictAuthoritySnapshotCacheUseCase.evictByMemberId(challenger.getMemberId());

        return savedDeactivation.getId();
    }

    @Override
    public void addEvidence(AddChallengerDeactivationEvidenceCommand command) {
        ChallengerDeactivation deactivation = loadChallengerDeactivationPort
            .getByChallengerIdForUpdate(command.challengerId());
        List<String> imageIds = normalizeValues(command.imageIds());
        List<String> links = normalizeValues(command.links());
        validateEvidenceAdditionRequest(imageIds, links);
        validateNoDuplicateEvidence(imageIds, links);

        long existingImageCount = loadChallengerDeactivationPort
            .countImagesByDeactivationId(deactivation.getId());
        long existingLinkCount = loadChallengerDeactivationPort
            .countLinksByDeactivationId(deactivation.getId());
        validateEvidenceCapacity(existingImageCount, existingLinkCount, imageIds.size(), links.size());
        validateEvidenceNotAlreadyRegistered(deactivation.getId(), imageIds, links);

        List<FileMetadataInfo> files = getFileUseCase.batchGetUsableByIds(
            imageIds,
            command.addedByMemberId()
        );
        validateEvidenceFileCategory(files);

        ChallengerDeactivationHistory history = ChallengerDeactivationHistory.create(
            deactivation,
            ChallengerDeactivationHistoryAction.EVIDENCE_ADDED,
            imageIds.size(),
            links.size(),
            command.addedByMemberId()
        );
        List<ChallengerDeactivationImage> images = createImages(
            history,
            imageIds,
            Math.toIntExact(existingImageCount)
        );
        List<ChallengerDeactivationLink> addedLinks = createLinks(
            history,
            links,
            Math.toIntExact(existingLinkCount)
        );

        saveChallengerDeactivationPort.saveHistory(history);
        saveChallengerDeactivationPort.saveImages(images);
        saveChallengerDeactivationPort.saveLinks(addedLinks);
    }

    private void validateNotRegistered(Long challengerId) {
        if (loadChallengerDeactivationPort.existsByChallengerId(challengerId)) {
            throw new ChallengerDomainException(ChallengerErrorCode.CHALLENGER_DEACTIVATION_ALREADY_EXISTS);
        }
    }

    private void validateNoDuplicateEvidence(List<String> imageIds, List<String> links) {
        if (hasDuplicateNormalizedValue(imageIds) || hasDuplicateNormalizedValue(links)) {
            throw new ChallengerDomainException(
                ChallengerErrorCode.INVALID_CHALLENGER_DEACTIVATION_REQUEST,
                "같은 근거 자료를 중복해서 등록할 수 없습니다."
            );
        }
    }

    private void validateEvidenceAdditionRequest(List<String> imageIds, List<String> links) {
        if (imageIds.isEmpty() && links.isEmpty()) {
            throw new ChallengerDomainException(
                ChallengerErrorCode.INVALID_CHALLENGER_DEACTIVATION_REQUEST,
                "추가할 사진이나 링크가 하나 이상 필요합니다."
            );
        }
    }

    private void validateEvidenceCapacity(
        long existingImageCount,
        long existingLinkCount,
        int addedImageCount,
        int addedLinkCount
    ) {
        if (existingImageCount + addedImageCount > ChallengerDeactivation.MAX_IMAGE_COUNT
            || existingLinkCount + addedLinkCount > ChallengerDeactivation.MAX_LINK_COUNT) {
            throw new ChallengerDomainException(
                ChallengerErrorCode.INVALID_CHALLENGER_DEACTIVATION_REQUEST,
                "근거 자료 등록 가능 개수를 초과했습니다."
            );
        }
    }

    private void validateEvidenceNotAlreadyRegistered(
        Long deactivationId,
        List<String> imageIds,
        List<String> links
    ) {
        if (loadChallengerDeactivationPort.existsAnyImageByDeactivationId(deactivationId, imageIds)
            || loadChallengerDeactivationPort.existsAnyLinkByDeactivationId(deactivationId, links)) {
            throw new ChallengerDomainException(
                ChallengerErrorCode.INVALID_CHALLENGER_DEACTIVATION_REQUEST,
                "이미 등록된 근거 자료를 다시 추가할 수 없습니다."
            );
        }
    }

    private boolean hasDuplicateNormalizedValue(List<String> values) {
        Set<String> normalizedValues = new HashSet<>();
        for (String value : values) {
            String normalized = value == null ? null : value.trim();
            if (!normalizedValues.add(normalized)) {
                return true;
            }
        }
        return false;
    }

    private List<String> normalizeValues(List<String> values) {
        return values.stream()
            .map(value -> value == null ? null : value.trim())
            .toList();
    }

    private void validateEvidenceFileCategory(List<FileMetadataInfo> files) {
        if (files.stream().anyMatch(file -> file.category() != FileCategory.CHALLENGER_DEACTIVATION_EVIDENCE)) {
            throw new ChallengerDomainException(
                ChallengerErrorCode.INVALID_CHALLENGER_DEACTIVATION_REQUEST,
                "탈부 근거 사진으로 업로드한 파일만 등록할 수 있습니다."
            );
        }
    }

    private List<ChallengerDeactivationImage> createImages(
        ChallengerDeactivationHistory history,
        List<String> imageIds
    ) {
        return createImages(history, imageIds, 0);
    }

    private List<ChallengerDeactivationImage> createImages(
        ChallengerDeactivationHistory history,
        List<String> imageIds,
        int startDisplayOrder
    ) {
        return IntStream.range(0, imageIds.size())
            .mapToObj(index -> ChallengerDeactivationImage.create(
                history,
                imageIds.get(index),
                startDisplayOrder + index
            ))
            .toList();
    }

    private List<ChallengerDeactivationLink> createLinks(
        ChallengerDeactivationHistory history,
        List<String> links
    ) {
        return createLinks(history, links, 0);
    }

    private List<ChallengerDeactivationLink> createLinks(
        ChallengerDeactivationHistory history,
        List<String> links,
        int startDisplayOrder
    ) {
        return IntStream.range(0, links.size())
            .mapToObj(index -> ChallengerDeactivationLink.create(
                history,
                links.get(index),
                startDisplayOrder + index
            ))
            .toList();
    }
}
