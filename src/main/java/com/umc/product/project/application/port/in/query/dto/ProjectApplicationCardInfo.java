package com.umc.product.project.application.port.in.query.dto;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.project.domain.ProjectApplication;
import com.umc.product.project.domain.ProjectApplicationFormPolicy;
import com.umc.product.project.domain.ProjectMatchingRound;
import com.umc.product.project.domain.enums.MatchingPhase;
import com.umc.product.project.domain.enums.MatchingType;
import java.time.Instant;
import lombok.Builder;

/**
 * PM/운영진용 지원자 목록 카드 1건의 Info DTO.
 * <p>
 * Service 가 ProjectApplication + 지원 파트(applicantPart) 까지 조립한 형태. Web Assembler 가 지원자(applicant) 의 닉네임/실명/학교명을 member
 * 도메인에서 보강해 최종 Response 로 변환한다.
 * <p>
 * 매칭 라운드는 표시용 라벨 대신 {@link MatchingType} / {@link MatchingPhase} 도메인 enum 조합으로 노출한다 -- 라벨 합성은 Web 레이어 책임. 본 응답은 항상 실제
 * 라운드 엔티티가 존재하는 application 만 다루므로 표시용 enum({@link MatchingRoundPhaseView}) 으로 분리할 필요가 없으며, 본인 조회({@code
 * MyProjectApplicationCardInfo}) 와는 데이터원/시맨틱이 달라 phase 표현이 의도적으로 다르다.
 */
@Builder
public record ProjectApplicationCardInfo(
    Long applicationId,
    Long applicantMemberId,
    ChallengerPart applicantPart,
    Long matchingRoundId,
    MatchingType matchingRoundType,
    MatchingPhase matchingRoundPhase,
    ManagedProjectApplicationCardStatus status,
    Instant submittedAt,
    Instant statusChangedAt
) {
    /**
     * ProjectApplication 과 지원 파트(applicantPart) 를 조립해 카드 Info 를 만든다.
     *
     * @param application   지원서 엔티티 (appliedMatchingRound 가 fetch 된 상태)
     * @param applicantPart 지원자가 어떤 파트로 지원했는지 -- {@link ProjectApplicationFormPolicy} 또는
     *                      {@link com.umc.product.survey.domain.FormResponse} 로부터 도출하여 전달
     */
    public static ProjectApplicationCardInfo of(
        ProjectApplication application,
        ChallengerPart applicantPart
    ) {
        ProjectMatchingRound round = application.getAppliedMatchingRound();

        return ProjectApplicationCardInfo.builder()
            .applicationId(application.getId())
            .applicantMemberId(application.getApplicantMemberId())
            .applicantPart(applicantPart)
            .matchingRoundId(round.getId())
            .matchingRoundType(round.getType())
            .matchingRoundPhase(round.getPhase())
            .status(ManagedProjectApplicationCardStatus.from(application.getStatus()))
            .submittedAt(application.getSubmittedAt())
            .statusChangedAt(application.getStatusChangedAt())
            .build();
    }
}
