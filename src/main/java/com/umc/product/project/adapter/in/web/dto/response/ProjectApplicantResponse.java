package com.umc.product.project.adapter.in.web.dto.response;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.project.adapter.in.web.dto.common.MatchingRoundPhaseView;
import com.umc.product.project.adapter.in.web.dto.common.MemberBrief;
import com.umc.product.project.application.port.in.query.dto.ManagedProjectApplicationCardStatus;
import com.umc.product.project.application.port.in.query.dto.ProjectApplicationSummaryInfo;
import com.umc.product.project.application.port.in.query.dto.ProjectMatchingRoundInfo;
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
 *
 * @param status 표시용 지원 상태. {@code SUBMITTED / APPROVED / REJECTED} 만 노출되며 임시저장(DRAFT)은 사전 필터링되어 본 응답에 포함되지 않는다.
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
    /**
     * 자원 정보(지원서)와 합성용 부가 정보(지원자 파트 / 매칭 라운드 / 멤버 brief) 를 받아 화면 fit 한 줄로 만든다.
     * <p>
     * 각 부가 정보는 Web Assembler 가 다른 도메인 UseCase 로 batch 조회해 채워준다. 한 자리라도 null 일 수 있으며 null 인 필드는 응답에서 그대로 null 로 노출된다.
     */
    public static ProjectApplicantResponse from(
        ProjectApplicationSummaryInfo info,
        ChallengerPart applicantPart,
        ProjectMatchingRoundInfo round,
        MemberBrief applicantMember
    ) {
        Applicant applicant = Applicant.builder()
            .memberId(info.applicantMemberId())
            .nickname(applicantMember == null ? null : applicantMember.nickname())
            .name(applicantMember == null ? null : applicantMember.name())
            .schoolName(applicantMember == null ? null : applicantMember.schoolName())
            .part(applicantPart)
            .build();

        MatchingRoundBrief matchingRound = MatchingRoundBrief.builder()
            .id(round == null ? null : round.id())
            .type(round == null ? null : round.type())
            .phase(round == null ? null : MatchingRoundPhaseView.from(round.phase()))
            .build();

        return ProjectApplicantResponse.builder()
            .applicationId(info.id())
            .applicant(applicant)
            .matchingRound(matchingRound)
            .status(ManagedProjectApplicationCardStatus.from(info.status()))
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
     * <p>
     * phase 는 도메인 enum 직노출을 피하기 위해 표시용 enum {@link MatchingRoundPhaseView} 로 노출한다. 본 응답은 실제 라운드 엔티티가 있는 지원서만 다루므로
     * RANDOM_MATCHING 케이스는 실제로 채워지지 않는다.
     */
    @Builder
    public record MatchingRoundBrief(
        Long id,
        MatchingType type,
        MatchingRoundPhaseView phase
    ) {
    }
}
