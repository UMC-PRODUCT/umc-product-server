package com.umc.product.project.adapter.in.web.dto.response;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.project.adapter.in.web.dto.common.MemberBrief;
import com.umc.product.project.application.port.in.query.dto.ManagedProjectApplicationCardStatus;
import com.umc.product.project.application.port.in.query.dto.ProjectApplicationCardInfo;
import com.umc.product.project.domain.enums.MatchingPhase;
import com.umc.product.project.domain.enums.MatchingType;
import java.time.Instant;
import lombok.Builder;

/**
 * PM/운영진용 단일 프로젝트의 지원자 1건에 대한 Web Response DTO.
 * <p>
 * 화면 컬럼 매핑: 차수(matchingRound.phase) / 파트(applicant.part) / 챌린저(닉네임/이름/학교) / 상태(status) / 처리시각(statusChangedAt) /
 * 지원시각(submittedAt).
 * <p>
 * formResponseId 는 보안상 노출하지 않는다 -- 단건 답안 조회는 별도 API.
 */
@Builder
public record ProjectApplicantResponse(
    Long applicationId,
    Applicant applicant,
    MatchingRoundBrief matchingRound,
    ManagedProjectApplicationCardStatus status,
    Instant submittedAt,
    Instant statusChangedAt
) {
    public static ProjectApplicantResponse from(ProjectApplicationCardInfo info, MemberBrief applicantMember) {
        Applicant applicant = Applicant.builder()
            .memberId(info.applicantMemberId())
            .nickname(applicantMember == null ? null : applicantMember.nickname())
            .name(applicantMember == null ? null : applicantMember.name())
            .schoolName(applicantMember == null ? null : applicantMember.schoolName())
            .part(info.applicantPart())
            .build();

        MatchingRoundBrief round = MatchingRoundBrief.builder()
            .id(info.matchingRoundId())
            .type(info.matchingRoundType())
            .phase(info.matchingRoundPhase())
            .build();

        return ProjectApplicantResponse.builder()
            .applicationId(info.applicationId())
            .applicant(applicant)
            .matchingRound(round)
            .status(info.status())
            .submittedAt(info.submittedAt())
            .statusChangedAt(info.statusChangedAt())
            .build();
    }

    /**
     * 지원자(챌린저) 정보. 닉네임/이름/학교는 member 도메인, 파트는 challenger 도메인에서 합성된다.
     */
    @Builder
    public record Applicant(
        Long memberId,
        String nickname,
        String name,
        String schoolName,
        ChallengerPart part
    ) {
    }

    /**
     * 매칭 라운드 식별 정보. 라벨 합성("기획-개발자 1차 매칭" 등)은 클라이언트가 type/phase 조합으로 처리한다.
     */
    @Builder
    public record MatchingRoundBrief(
        Long id,
        MatchingType type,
        MatchingPhase phase
    ) {
    }
}
