package com.umc.product.project.application.port.in.query.dto;

import java.util.Objects;
import lombok.Builder;

/**
 * 지원서 단건 상세 조회 Query.
 * <p>
 * path variable 두 개 (projectId, applicationId) + 호출자 식별자(requesterMemberId) 를 묶는다. Service 단에서 application 의
 * form.project.id 와 projectId 정합성을 검증하며, 위반 시 not-found 로 위장하여 다른 프로젝트의 application 존재 여부를 은닉한다.
 * <p>
 * {@code requesterMemberId} 는 도메인 비즈니스 규칙(예: DRAFT 임시저장본은 지원자 본인만 조회 가능) 분기에 사용된다. 4종 호출자(PO/Sub-PO/지부장/CC/지원자 본인) 의
 * 호출 자격 자체는 별도 권한 검사 (@CheckAccess) 책임이다.
 *
 * @param projectId         URL path 의 프로젝트 ID
 * @param applicationId     URL path 의 지원서 ID
 * @param requesterMemberId 호출자 Member ID (현재 인증 컨텍스트)
 */
@Builder
public record GetProjectApplicationDetailQuery(
    Long projectId,
    Long applicationId,
    Long requesterMemberId
) {
    public GetProjectApplicationDetailQuery {
        Objects.requireNonNull(projectId, "projectId must not be null");
        Objects.requireNonNull(applicationId, "applicationId must not be null");
        Objects.requireNonNull(requesterMemberId, "requesterMemberId must not be null");
    }
}
