package com.umc.product.challenger.application.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.umc.product.challenger.application.port.in.command.DeleteChallengerDeactivationDataUseCase;
import com.umc.product.challenger.application.port.out.DeleteChallengerDeactivationPort;
import com.umc.product.challenger.application.port.out.LoadChallengerDeactivationPort;
import com.umc.product.challenger.application.port.out.LoadChallengerPort;
import com.umc.product.challenger.domain.Challenger;
import com.umc.product.challenger.domain.ChallengerDeactivation;
import com.umc.product.member.application.port.in.command.DeleteWithdrawnMemberIdentityUseCase;
import com.umc.product.member.application.port.in.command.LockMemberUseCase;
import com.umc.product.storage.application.port.in.command.DeleteStoredFileUseCase;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChallengerDeactivationDataDeletionService implements DeleteChallengerDeactivationDataUseCase {

    private final DeleteChallengerDeactivationPort deleteChallengerDeactivationPort;
    private final LoadChallengerDeactivationPort loadChallengerDeactivationPort;
    private final LoadChallengerPort loadChallengerPort;
    private final LockMemberUseCase lockMemberUseCase;
    private final DeleteWithdrawnMemberIdentityUseCase deleteWithdrawnMemberIdentityUseCase;
    private final DeleteStoredFileUseCase deleteStoredFileUseCase;

    @Override
    @Transactional
    public void delete(Long deactivationId) {
        ChallengerDeactivation deactivation = loadChallengerDeactivationPort.getByIdForUpdate(deactivationId);
        Long memberId = loadChallengerPort.findById(deactivation.getChallengerId()).map(Challenger::getMemberId)
            .orElse(null);
        if (memberId != null) {
            lockMemberUseCase.lockByIdIfExists(memberId);
            deleteWithdrawnMemberIdentityUseCase.lockByFormerMemberId(memberId);
        }
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
        if (memberId != null && !loadChallengerDeactivationPort.existsByMemberId(memberId)) {
            deleteWithdrawnMemberIdentityUseCase.deleteByFormerMemberId(memberId);
        }
    }
}
