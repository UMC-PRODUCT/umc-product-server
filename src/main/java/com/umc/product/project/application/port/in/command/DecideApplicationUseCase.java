package com.umc.product.project.application.port.in.command;

import com.umc.product.project.application.port.in.command.dto.ApplicationDecisionStatus;
import com.umc.product.project.application.port.in.query.dto.ProjectApplicationInfo;

/**
 * 지원서 합/불 결정 UseCase (APPLY-103).
 * <p>
 * PM 이 매칭 차수 진행 중에 SUBMITTED ↔ APPROVED ↔ REJECTED 사이를 자유롭게 토글한다.
 * 권한은 {@code @CheckAccess(PROJECT_APPLICATION, APPROVE)} 가 책임지고,
 * 차수 잠금 검증은 도메인 메서드({@code ProjectApplication.approve / reject / revertToPending}) 가 책임진다.
 */
public interface DecideApplicationUseCase {

    ProjectApplicationInfo decide(
        Long applicationId,
        ApplicationDecisionStatus targetStatus,
        String reason,
        Long decidedByMemberId
    );
}
