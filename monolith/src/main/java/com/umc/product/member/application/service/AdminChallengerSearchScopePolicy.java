package com.umc.product.member.application.service;

import org.springframework.stereotype.Component;

import com.umc.product.authorization.application.port.in.query.CheckChallengerAuthorityUseCase;
import com.umc.product.member.application.port.in.query.CheckMemberExistenceUseCase;
import com.umc.product.member.application.port.in.query.GetMemberUseCase;
import com.umc.product.member.application.port.in.query.dto.MemberInfo;
import com.umc.product.member.application.port.in.query.dto.SearchMemberQuery;
import com.umc.product.member.domain.exception.MemberDomainException;
import com.umc.product.member.domain.exception.MemberErrorCode;
import com.umc.product.organization.application.port.in.query.GetChapterUseCase;
import com.umc.product.organization.application.port.in.query.GetGisuUseCase;

import lombok.RequiredArgsConstructor;

/** 요청자의 챌린저 검색 권한에 따라 기수·지부·학교 범위를 결정합니다. */
@Component
@RequiredArgsConstructor
public class AdminChallengerSearchScopePolicy {

    private final CheckMemberExistenceUseCase checkMemberExistenceUseCase;
    private final CheckChallengerAuthorityUseCase checkChallengerAuthorityUseCase;
    private final GetMemberUseCase getMemberUseCase;
    private final GetGisuUseCase getGisuUseCase;
    private final GetChapterUseCase getChapterUseCase;

    public SearchMemberQuery scope(SearchMemberQuery query, Long memberId) {
        if (memberId == null || !checkMemberExistenceUseCase.existsById(memberId)) {
            throw accessDenied();
        }
        boolean superAdmin = checkChallengerAuthorityUseCase.isSuperAdmin(memberId);
        Long gisuId = query.gisuId() != null ? query.gisuId()
            : getGisuUseCase.findActiveGisu().map(gisu -> gisu.gisuId()).orElse(null);
        if (gisuId == null) {
            throw accessDenied();
        }
        if (superAdmin) {
            return withScope(query, gisuId, query.chapterId(), query.schoolId());
        }
        if (checkChallengerAuthorityUseCase.isCentralMemberInGisu(memberId, gisuId)) {
            return withScope(query, gisuId, query.chapterId(), query.schoolId());
        }
        MemberInfo member = getMemberUseCase.getById(memberId);
        Long schoolId = member.schoolId();
        if (schoolId == null) {
            throw accessDenied();
        }
        Long chapterId = getChapterUseCase.findByGisuAndSchool(gisuId, schoolId)
            .map(chapter -> chapter.id())
            .orElseThrow(this::accessDenied);

        if (checkChallengerAuthorityUseCase.isChapterPresidentInGisu(memberId, gisuId, chapterId)) {
            validateChapterScope(query, gisuId, chapterId);
            return withScope(query, gisuId, chapterId, query.schoolId());
        }

        if (!checkChallengerAuthorityUseCase.isSchoolAdminInGisu(memberId, gisuId, schoolId)
            || query.schoolId() != null && !query.schoolId().equals(schoolId)) {
            throw accessDenied();
        }
        if (query.chapterId() != null && !query.chapterId().equals(chapterId)) {
            throw accessDenied();
        }
        return withScope(query, gisuId, chapterId, schoolId);
    }

    private void validateChapterScope(SearchMemberQuery query, Long gisuId, Long chapterId) {
        if (query.chapterId() != null && !query.chapterId().equals(chapterId)) {
            throw accessDenied();
        }
        if (query.schoolId() == null) {
            return;
        }
        Long schoolChapterId = getChapterUseCase.findByGisuAndSchool(gisuId, query.schoolId())
            .map(chapter -> chapter.id())
            .orElseThrow(this::accessDenied);
        if (!schoolChapterId.equals(chapterId)) {
            throw accessDenied();
        }
    }

    private SearchMemberQuery withScope(SearchMemberQuery query, Long gisuId, Long chapterId, Long schoolId) {
        return new SearchMemberQuery(query.keyword(), gisuId, query.part(), chapterId, schoolId);
    }

    private MemberDomainException accessDenied() {
        return new MemberDomainException(MemberErrorCode.CHALLENGER_MANAGEMENT_SEARCH_ACCESS_DENIED);
    }
}
