package com.umc.product.project.application.port.in.command;

import com.umc.product.project.application.port.in.command.dto.UpdateProjectApplicationDraftCommand;
import com.umc.product.project.application.port.in.query.dto.ProjectApplicationInfo;

/**
 * 챌린저 지원서 답변 수정 및 임시저장 UseCase (APPLY-002).
 * <p>
 * PUT 시멘틱 — 본문이 곧 새 전체 답변 상태가 된다. 본인만 호출 가능.
 * DRAFT는 필수 답변 누락을 허용한다. SUBMITTED는 해당 지원 차수의 지원 기간에만 수정할 수 있으며 제출 검증을 적용한다.
 * 수정 시 상태와 최초 제출 시각을 유지하고, SUBMITTED 수정에는 재제출이 필요하지 않다.
 */
public interface UpdateProjectApplicationDraftUseCase {
    ProjectApplicationInfo update(UpdateProjectApplicationDraftCommand command);
}
