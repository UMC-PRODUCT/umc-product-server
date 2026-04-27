package com.umc.product.project.application.port.in.command;

import com.umc.product.project.application.port.in.command.dto.SubmitProjectCommand;

/**
 * 프로젝트 제출 UseCase (PROJECT-107).
 * <p>
 * PM이 작성한 Draft를 제출하여 {@code PENDING_REVIEW} 상태로 전이시킵니다.
 * 이후 Admin이 파트/TO 할당(PROJECT-105)과 공개(PROJECT-108) 단계를 수행합니다.
 */
public interface SubmitProjectUseCase {

    /**
     * DRAFT 상태의 프로젝트를 제출하여 PENDING_REVIEW로 전이합니다.
     *
     * @param command 제출 Command
     */
    void submit(SubmitProjectCommand command);
}
