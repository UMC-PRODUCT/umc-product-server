package com.umc.product.challenger.application.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.umc.product.challenger.application.port.in.command.DeleteChallengerDeactivationDataUseCase;
import com.umc.product.challenger.application.port.out.DeleteChallengerDeactivationPort;
import com.umc.product.challenger.application.port.out.LoadChallengerDeactivationPort;
import com.umc.product.storage.application.port.in.command.DeleteStoredFileUseCase;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChallengerDeactivationDataDeletionService implements DeleteChallengerDeactivationDataUseCase {

    private final DeleteChallengerDeactivationPort deleteChallengerDeactivationPort;
    private final LoadChallengerDeactivationPort loadChallengerDeactivationPort;
    private final DeleteStoredFileUseCase deleteStoredFileUseCase;

    @Override
    @Transactional
    public void delete(Long deactivationId) {
        loadChallengerDeactivationPort.getByIdForUpdate(deactivationId);
        List<String> imageIds = loadChallengerDeactivationPort.listImageIdsByDeactivationId(deactivationId).stream()
            .distinct()
            .sorted()
            .toList();

        deleteChallengerDeactivationPort.deleteImagesByDeactivationId(deactivationId);
        deleteChallengerDeactivationPort.deleteLinksByDeactivationId(deactivationId);
        imageIds.forEach(imageId -> deleteStoredFileUseCase.deleteIfNotReferencedOutsideDeactivation(
                imageId,
                deactivationId
            ));
        deleteChallengerDeactivationPort.deleteHistoriesByDeactivationId(deactivationId);
        deleteChallengerDeactivationPort.deleteById(deactivationId);
    }
}
