package com.umc.product.project.application.port.in.command;

import com.umc.product.project.application.port.in.command.dto.SubmitProjectCommand;
import com.umc.product.project.domain.enums.ProjectStatus;

/**
 * 프로젝트 제출 UseCase (PROJECT-107).
 * PM 제출은 승인대기로, 해당 프로젝트를 관리할 권한이 있는 운영진 제출은 검증 후 공개 상태로 전이한다.
 */
public interface SubmitProjectUseCase {

    /** 제출 후 실제 프로젝트 상태를 반환한다. */
    ProjectStatus submit(SubmitProjectCommand command);
}
