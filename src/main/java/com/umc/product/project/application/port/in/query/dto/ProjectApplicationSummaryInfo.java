package com.umc.product.project.application.port.in.query.dto;

import com.umc.product.project.domain.ProjectApplication;
import com.umc.product.project.domain.enums.ProjectApplicationStatus;
import java.time.Instant;
import lombok.Builder;

/**
 * ProjectApplication 자원의 표준 view.
 * <p>
 * 자기 도메인 자원 한 종류만 노출한다 -- 다른 자원({@code Project}, {@code ProjectMatchingRound}, {@code Member} 등) 과의 합성은 Web Assembler
 * 에서 cross-domain UseCase 와 함께 수행한다.
 * <p>
 * 이름 메모: 표준 자원 view 의 컨벤션상 {@code ProjectApplicationInfo} 가 자연스러우나, 해당 이름은 현재 Command 결과용 record
 * ({@code ProjectApplicationInfo(applicationId, status)}) 가 점유 중이라 충돌을 피하기 위해 {@code Summary} 접미를 붙였다. Command 쪽 정리 PR
 * 이후 본 이름이 표준으로 승격될 예정이다.
 */
@Builder
public record ProjectApplicationSummaryInfo(
    Long id,
    Long applicantMemberId,
    Long applicationFormId,
    Long projectId,
    Long matchingRoundId,
    ProjectApplicationStatus status,
    Instant submittedAt,
    Instant statusChangedAt,
    Instant createdAt,
    Instant updatedAt
) {
    public static ProjectApplicationSummaryInfo from(ProjectApplication application) {
        return ProjectApplicationSummaryInfo.builder()
            .id(application.getId())
            .applicantMemberId(application.getApplicantMemberId())
            .applicationFormId(application.getApplicationForm().getId())
            .projectId(application.getApplicationForm().getProject().getId())
            .matchingRoundId(application.getAppliedMatchingRound().getId())
            .status(application.getStatus())
            .submittedAt(application.getSubmittedAt())
            .statusChangedAt(application.getStatusChangedAt())
            .createdAt(application.getCreatedAt())
            .updatedAt(application.getUpdatedAt())
            .build();
    }
}
