package com.umc.product.challenger.application.service;

import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.umc.product.challenger.application.port.in.query.CheckChallengerDeactivationEvidenceUseCase;
import com.umc.product.challenger.application.port.in.query.ListChallengerDeactivationReasonUseCase;
import com.umc.product.challenger.application.port.in.query.dto.ChallengerDeactivationReasonInfo;
import com.umc.product.challenger.application.port.in.query.dto.ChallengerDeactivationReasonListInfo;
import com.umc.product.challenger.application.port.out.LoadChallengerDeactivationPort;
import com.umc.product.challenger.domain.enums.ChallengerDeactivationReason;
import com.umc.product.common.domain.enums.ChallengerStatus;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChallengerDeactivationQueryService
    implements CheckChallengerDeactivationEvidenceUseCase, ListChallengerDeactivationReasonUseCase {

    private final LoadChallengerDeactivationPort loadChallengerDeactivationPort;

    @Override
    public boolean isImageInUse(String imageId) {
        return imageId != null && loadChallengerDeactivationPort.existsByImageId(imageId);
    }

    @Override
    public boolean isImageInUseOutsideDeactivation(String imageId, Long deactivationId) {
        return imageId != null
            && loadChallengerDeactivationPort.existsImageReferenceOutsideDeactivation(imageId, deactivationId);
    }

    @Override
    public ChallengerDeactivationReasonListInfo listReasons() {
        return new ChallengerDeactivationReasonListInfo(
            listReasonsByStatus(ChallengerStatus.WITHDRAWN),
            listReasonsByStatus(ChallengerStatus.EXPELLED)
        );
    }

    private List<ChallengerDeactivationReasonInfo> listReasonsByStatus(ChallengerStatus status) {
        return Arrays.stream(ChallengerDeactivationReason.values())
            .filter(reason -> reason.getStatus() == status)
            .map(ChallengerDeactivationReasonInfo::from)
            .toList();
    }
}
