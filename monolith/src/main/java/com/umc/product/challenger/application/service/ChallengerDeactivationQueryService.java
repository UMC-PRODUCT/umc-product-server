package com.umc.product.challenger.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.umc.product.challenger.application.port.in.query.CheckChallengerDeactivationEvidenceUseCase;
import com.umc.product.challenger.application.port.out.LoadChallengerDeactivationPort;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChallengerDeactivationQueryService implements CheckChallengerDeactivationEvidenceUseCase {

    private final LoadChallengerDeactivationPort loadChallengerDeactivationPort;

    @Override
    public boolean isImageInUse(String imageId) {
        return imageId != null && loadChallengerDeactivationPort.existsByImageId(imageId);
    }
}
