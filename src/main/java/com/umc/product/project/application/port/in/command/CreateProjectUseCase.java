package com.umc.product.project.application.port.in.command;

import com.umc.product.project.application.port.in.command.dto.CreateProjectDraftCommand;

/**
 * 프로젝트 생성 UseCase (PROJECT-101).
 * <p>
 * 생성은 항상 {@code DRAFT} 상태로 시작합니다. 이후 상태 전이는
 * {@link SubmitProjectUseCase}(PM 제출) 및 후속 Publish/Complete/Abort UseCase에서 담당합니다.
 */
public interface CreateProjectUseCase {

    /**
     * DRAFT 상태의 프로젝트를 생성합니다.
     *
     * @param command 생성 Command
     * @return 생성된 프로젝트 ID
     */
    Long create(CreateProjectDraftCommand command);
}
