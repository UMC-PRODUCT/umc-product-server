package com.umc.product.challenger.application.service;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.umc.product.authorization.application.port.in.query.CheckChallengerAuthorityUseCase;
import com.umc.product.authorization.domain.exception.AuthorizationDomainException;
import com.umc.product.authorization.domain.exception.AuthorizationErrorCode;
import com.umc.product.challenger.application.port.in.query.CheckChallengerDeactivationEvidenceUseCase;
import com.umc.product.challenger.application.port.in.query.ListChallengerDeactivationReasonUseCase;
import com.umc.product.challenger.application.port.in.query.SearchChallengerDeactivationUseCase;
import com.umc.product.challenger.application.port.in.query.dto.ChallengerDeactivationListItemInfo;
import com.umc.product.challenger.application.port.in.query.dto.ChallengerDeactivationReasonInfo;
import com.umc.product.challenger.application.port.in.query.dto.ChallengerDeactivationReasonListInfo;
import com.umc.product.challenger.application.port.in.query.dto.SearchChallengerDeactivationQuery;
import com.umc.product.challenger.application.port.out.LoadChallengerDeactivationPort;
import com.umc.product.challenger.application.port.out.SearchChallengerDeactivationPort;
import com.umc.product.challenger.domain.enums.ChallengerDeactivationReason;
import com.umc.product.common.domain.enums.ChallengerStatus;
import com.umc.product.member.application.port.in.query.GetMemberUseCase;
import com.umc.product.organization.application.port.in.query.GetChapterUseCase;
import com.umc.product.organization.application.port.in.query.GetGisuUseCase;
import com.umc.product.organization.application.port.in.query.dto.gisu.GisuInfo;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChallengerDeactivationQueryService
    implements CheckChallengerDeactivationEvidenceUseCase, ListChallengerDeactivationReasonUseCase,
    SearchChallengerDeactivationUseCase {

    private final LoadChallengerDeactivationPort loadChallengerDeactivationPort;
    private final SearchChallengerDeactivationPort searchChallengerDeactivationPort;
    private final CheckChallengerAuthorityUseCase checkChallengerAuthorityUseCase;
    private final GetMemberUseCase getMemberUseCase;
    private final GetGisuUseCase getGisuUseCase;
    private final GetChapterUseCase getChapterUseCase;

    @Override
    public Page<ChallengerDeactivationListItemInfo> searchDeactivations(
        Long requesterMemberId, SearchChallengerDeactivationQuery query, Pageable pageable
    ) {
        return searchChallengerDeactivationPort.search(resolveSearchScope(requesterMemberId, query), pageable);
    }

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

    private SearchChallengerDeactivationQuery resolveSearchScope(
        Long memberId, SearchChallengerDeactivationQuery query
    ) {
        Long activeGisuId = getGisuUseCase.findActiveGisu().map(GisuInfo::gisuId).orElse(null);
        Long targetGisuId = query.gisuId() == null ? activeGisuId : query.gisuId();
        if (memberId == null || targetGisuId == null) {
            throw accessDenied();
        }
        if (checkChallengerAuthorityUseCase.isSuperAdmin(memberId)
            || (activeGisuId != null && checkChallengerAuthorityUseCase.isCentralMemberInGisu(
                memberId, activeGisuId))) {
            return query.withScope(targetGisuId, query.schoolId());
        }
        if (!targetGisuId.equals(activeGisuId)) {
            throw accessDenied();
        }

        Long schoolId = getMemberUseCase.findAllSchoolIdsByIds(Set.of(memberId)).get(memberId);
        if (schoolId == null || !checkChallengerAuthorityUseCase.isSchoolCoreInGisu(memberId, targetGisuId, schoolId)
            || (query.schoolId() != null && !query.schoolId().equals(schoolId))) {
            throw accessDenied();
        }
        if (query.chapterId() != null && getChapterUseCase.findByGisuAndSchool(targetGisuId, schoolId)
            .filter(chapter -> query.chapterId().equals(chapter.id())).isEmpty()) {
            throw accessDenied();
        }
        return query.withScope(targetGisuId, schoolId);
    }

    private AuthorizationDomainException accessDenied() {
        return new AuthorizationDomainException(AuthorizationErrorCode.PERMISSION_DENIED);
    }
}
