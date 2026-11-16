package com.umc.product.challenger.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.umc.product.challenger.application.port.in.command.DeleteChallengerDeactivationDataUseCase;
import com.umc.product.challenger.application.port.out.DeleteChallengerDeactivationPort;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChallengerDeactivationDataDeletionService implements DeleteChallengerDeactivationDataUseCase {

    private final DeleteChallengerDeactivationPort deleteChallengerDeactivationPort;

    @Override
    @Transactional
    public void delete(Long deactivationId) {
        deleteChallengerDeactivationPort.deleteImagesByDeactivationId(deactivationId);
        deleteChallengerDeactivationPort.deleteLinksByDeactivationId(deactivationId);
        deleteChallengerDeactivationPort.deleteHistoriesByDeactivationId(deactivationId);
        deleteChallengerDeactivationPort.deleteById(deactivationId);
    }
}
