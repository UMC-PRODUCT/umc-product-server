package com.umc.product.challenger.application.service;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import com.umc.product.authorization.application.port.in.query.CheckChallengerAuthorityUseCase;
import com.umc.product.authorization.domain.exception.AuthorizationDomainException;
import com.umc.product.authorization.domain.exception.AuthorizationErrorCode;
import com.umc.product.challenger.application.port.in.query.CheckChallengerDeactivationEvidenceUseCase;
import com.umc.product.challenger.application.port.in.query.GetChallengerDeactivationDetailUseCase;
import com.umc.product.challenger.application.port.in.query.ListChallengerDeactivationReasonUseCase;
import com.umc.product.challenger.application.port.in.query.SearchChallengerDeactivationUseCase;
import com.umc.product.challenger.application.port.in.query.dto.ChallengerDeactivationDetailInfo;
import com.umc.product.challenger.application.port.in.query.dto.ChallengerDeactivationDetailInfo.ActorInfo;
import com.umc.product.challenger.application.port.in.query.dto.ChallengerDeactivationDetailInfo.ChallengerInfo;
import com.umc.product.challenger.application.port.in.query.dto.ChallengerDeactivationDetailInfo.EvidenceImageInfo;
import com.umc.product.challenger.application.port.in.query.dto.ChallengerDeactivationDetailInfo.HistoryInfo;
import com.umc.product.challenger.application.port.in.query.dto.ChallengerDeactivationListItemInfo;
import com.umc.product.challenger.application.port.in.query.dto.ChallengerDeactivationReasonInfo;
import com.umc.product.challenger.application.port.in.query.dto.ChallengerDeactivationReasonListInfo;
import com.umc.product.challenger.application.port.in.query.dto.SearchChallengerDeactivationQuery;
import com.umc.product.challenger.application.port.out.LoadChallengerDeactivationPort;
import com.umc.product.challenger.application.port.out.LoadChallengerPort;
import com.umc.product.challenger.application.port.out.SearchChallengerDeactivationPort;
import com.umc.product.challenger.domain.Challenger;
import com.umc.product.challenger.domain.ChallengerDeactivation;
import com.umc.product.challenger.domain.ChallengerDeactivationHistory;
import com.umc.product.challenger.domain.enums.ChallengerDeactivationReason;
import com.umc.product.challenger.domain.exception.ChallengerDomainException;
import com.umc.product.challenger.domain.exception.ChallengerErrorCode;
import com.umc.product.common.domain.enums.ChallengerStatus;
import com.umc.product.member.application.port.in.query.GetMemberUseCase;
import com.umc.product.member.application.port.in.query.dto.MemberBasicInfo;
import com.umc.product.organization.application.port.in.query.GetChapterUseCase;
import com.umc.product.organization.application.port.in.query.GetGisuUseCase;
import com.umc.product.organization.application.port.in.query.GetSchoolUseCase;
import com.umc.product.organization.application.port.in.query.dto.chapter.ChapterInfo;
import com.umc.product.organization.application.port.in.query.dto.gisu.GisuInfo;
import com.umc.product.organization.application.port.in.query.dto.school.SchoolDetailInfo;
import com.umc.product.storage.application.port.in.query.GetFileUseCase;
import com.umc.product.storage.application.port.in.query.dto.FileInfo;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChallengerDeactivationQueryService
    implements CheckChallengerDeactivationEvidenceUseCase, ListChallengerDeactivationReasonUseCase,
    SearchChallengerDeactivationUseCase, GetChallengerDeactivationDetailUseCase {

    private final LoadChallengerDeactivationPort loadChallengerDeactivationPort;
    private final LoadChallengerPort loadChallengerPort;
    private final SearchChallengerDeactivationPort searchChallengerDeactivationPort;
    private final CheckChallengerAuthorityUseCase checkChallengerAuthorityUseCase;
    private final GetMemberUseCase getMemberUseCase;
    private final GetGisuUseCase getGisuUseCase;
    private final GetChapterUseCase getChapterUseCase;
    private final GetSchoolUseCase getSchoolUseCase;
    private final GetFileUseCase getFileUseCase;

    @Override
    public Page<ChallengerDeactivationListItemInfo> searchDeactivations(
        Long requesterMemberId, SearchChallengerDeactivationQuery query, Pageable pageable
    ) {
        return searchChallengerDeactivationPort.search(resolveSearchScope(requesterMemberId, query), pageable);
    }

    @Override
    // 근거 추가나 보관 기간 만료가 동시에 일어나도 근거와 진행 기록을 같은 시점으로 읽는다.
    @Transactional(readOnly = true, isolation = Isolation.REPEATABLE_READ)
    public ChallengerDeactivationDetailInfo getDetail(Long requesterMemberId, Long deactivationId) {
        Long activeGisuId = getGisuUseCase.findActiveGisu().map(GisuInfo::gisuId).orElse(null);
        Long allowedSchoolId = resolveAllowedSchoolId(requesterMemberId, activeGisuId);
        ChallengerDeactivation deactivation = loadChallengerDeactivationPort.findById(deactivationId)
            .orElseThrow(this::deactivationNotFound);
        Challenger challenger = loadChallengerPort.findById(deactivation.getChallengerId())
            .orElseThrow(this::deactivationNotFound);
        Long targetSchoolId = getMemberUseCase.findAllSchoolIdsByIds(Set.of(challenger.getMemberId()))
            .get(challenger.getMemberId());
        if (allowedSchoolId != null
            && (!challenger.getGisuId().equals(activeGisuId) || !allowedSchoolId.equals(targetSchoolId))) {
            throw accessDenied();
        }

        List<ChallengerDeactivationHistory> histories =
            loadChallengerDeactivationPort.listHistoriesByDeactivationId(deactivationId);
        Set<Long> memberIds = new HashSet<>(Set.of(challenger.getMemberId()));
        memberIds.add(deactivation.getRegisteredByMemberId());
        histories.forEach(history -> memberIds.add(history.getActorMemberId()));
        Map<Long, MemberBasicInfo> members = getMemberUseCase.findAllBasicByIds(memberIds);

        return new ChallengerDeactivationDetailInfo(
            deactivationId,
            toChallengerInfo(challenger, members.get(challenger.getMemberId()), targetSchoolId),
            deactivation.getStatus(),
            deactivation.getReason(),
            deactivation.getReason().getDisplayName(),
            deactivation.getReasonDetail(),
            deactivation.getMemo(),
            deactivation.getCreatedAt(),
            toActorInfo(deactivation.getRegisteredByMemberId(), members),
            listEvidenceImages(deactivationId),
            loadChallengerDeactivationPort.listOrderedLinksByDeactivationId(deactivationId),
            histories.stream()
                .map(history -> new HistoryInfo(
                    history.getId(), history.getAction(), history.getCreatedAt(),
                    toActorInfo(history.getActorMemberId(), members), history.getImageCount(), history.getLinkCount()
                ))
                .toList()
        );
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

    SearchChallengerDeactivationQuery resolveSearchScope(
        Long memberId, SearchChallengerDeactivationQuery query
    ) {
        Long activeGisuId = getGisuUseCase.findActiveGisu().map(GisuInfo::gisuId).orElse(null);
        Long targetGisuId = query.gisuId() == null ? activeGisuId : query.gisuId();
        if (targetGisuId == null) {
            throw accessDenied();
        }
        Long schoolId = resolveAllowedSchoolId(memberId, activeGisuId);
        if (schoolId == null) {
            return query.withScope(targetGisuId, query.schoolId());
        }
        if (!targetGisuId.equals(activeGisuId)
            || (query.schoolId() != null && !query.schoolId().equals(schoolId))) {
            throw accessDenied();
        }
        if (query.chapterId() != null && getChapterUseCase.findByGisuAndSchool(targetGisuId, schoolId)
            .filter(chapter -> query.chapterId().equals(chapter.id())).isEmpty()) {
            throw accessDenied();
        }
        return query.withScope(targetGisuId, schoolId);
    }

    // 전체 학교를 조회할 수 있는 역할에는 학교 제한을 적용하지 않는다.
    private Long resolveAllowedSchoolId(Long memberId, Long activeGisuId) {
        if (memberId == null) {
            throw accessDenied();
        }
        if (checkChallengerAuthorityUseCase.isSuperAdmin(memberId)
            || (activeGisuId != null
                && checkChallengerAuthorityUseCase.isCentralMemberInGisu(memberId, activeGisuId))) {
            return null;
        }
        if (activeGisuId == null) {
            throw accessDenied();
        }
        Long schoolId = getMemberUseCase.findAllSchoolIdsByIds(Set.of(memberId)).get(memberId);
        if (schoolId == null || !checkChallengerAuthorityUseCase.isSchoolCoreInGisu(memberId, activeGisuId, schoolId)) {
            throw accessDenied();
        }
        return schoolId;
    }

    private ChallengerInfo toChallengerInfo(Challenger challenger, MemberBasicInfo member, Long schoolId) {
        Long gisuId = challenger.getGisuId();
        Long generation = getGisuUseCase.getByIds(Set.of(gisuId)).stream()
            .map(GisuInfo::generation)
            .findFirst()
            .orElse(null);
        String schoolName = null;
        String chapterName = null;
        if (schoolId != null) {
            schoolName = getSchoolUseCase.listDetailsByIds(Set.of(schoolId)).stream()
                .map(SchoolDetailInfo::schoolName)
                .findFirst()
                .orElse(null);
            chapterName = getChapterUseCase.findByGisuAndSchool(gisuId, schoolId).map(ChapterInfo::name).orElse(null);
        }
        return new ChallengerInfo(
            challenger.getId(), member == null ? null : member.name(), member == null ? null : member.nickname(),
            gisuId, generation, schoolId, schoolName, chapterName, challenger.getPart()
        );
    }

    private ActorInfo toActorInfo(Long memberId, Map<Long, MemberBasicInfo> members) {
        MemberBasicInfo member = members.get(memberId);
        return new ActorInfo(
            memberId, member == null ? null : member.name(), member == null ? null : member.nickname()
        );
    }

    private List<EvidenceImageInfo> listEvidenceImages(Long deactivationId) {
        List<String> imageIds = loadChallengerDeactivationPort.listOrderedImageIdsByDeactivationId(deactivationId);
        if (imageIds.isEmpty()) {
            return List.of();
        }
        Map<String, FileInfo> files = getFileUseCase.findAllByIds(imageIds);
        return imageIds.stream()
            .filter(files::containsKey)
            .map(imageId -> new EvidenceImageInfo(files.get(imageId).fileLink(), files.get(imageId).originalFileName()))
            .toList();
    }

    private ChallengerDomainException deactivationNotFound() {
        return new ChallengerDomainException(ChallengerErrorCode.CHALLENGER_DEACTIVATION_NOT_FOUND);
    }

    private AuthorizationDomainException accessDenied() {
        return new AuthorizationDomainException(AuthorizationErrorCode.PERMISSION_DENIED);
    }
}
