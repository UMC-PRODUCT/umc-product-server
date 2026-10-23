package com.umc.product.project.application.port.in.command;

import com.umc.product.project.application.port.in.command.dto.AbortProjectCommand;

/**
 * 프로젝트 중단(abort) UseCase.
 * <p>
 * IN_PROGRESS 상태 프로젝트를 ABORTED 로 전이합니다. ACTIVE ProjectMember 는 WITHDRAWN,
 * 진행 중(DRAFT/SUBMITTED) ProjectApplication 은 CANCELLED 로 일괄 동기화합니다.
 * 권한 검증은 Controller 단의 {@code @CheckAccess(MANAGE)} 가 담당합니다.
 */
public interface AbortProjectUseCase {

    /**
     * 프로젝트를 중단 처리합니다.
     */
    void abort(AbortProjectCommand command);
}
