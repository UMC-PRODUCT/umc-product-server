package com.umc.product.project.application.port.in.command;

import com.umc.product.project.application.port.in.command.dto.ApplicationDecisionStatus;
import com.umc.product.project.application.port.in.query.dto.ProjectApplicationInfo;

/**
 * 지원서 합/불 결정 UseCase (APPLY-103).
 * <p>
 * PM 이 지원 마감 후 결정 마감 전까지 APPROVED / REJECTED / SUBMITTED(대기로 되돌리기) 사이에서 결정을 바꾼다.
 * 권한은 {@code @CheckAccess(PROJECT_APPLICATION, APPROVE)} 가 책임지고,
 * 차수 잠금 검증은 도메인 메서드({@code ProjectApplication.approve / reject}) 가 책임진다.
 */
public interface DecideApplicationUseCase {

    ProjectApplicationInfo decide(
        Long applicationId,
        ApplicationDecisionStatus targetStatus,
        String reason,
        Long decidedByMemberId
    );
}
