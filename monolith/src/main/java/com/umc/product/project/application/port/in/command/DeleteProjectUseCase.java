package com.umc.product.project.application.port.in.command;

import com.umc.product.project.application.port.in.command.dto.DeleteProjectCommand;

/**
 * 프로젝트 hard delete UseCase.
 * <p>
 * DRAFT / PENDING_REVIEW 상태에서만 호출 가능. 자식 row(멤버/정원/지원 폼 매핑/Form/Policy)를
 * 같은 트랜잭션 내에서 명시적으로 cascade 삭제합니다. IN_PROGRESS 이상은
 * {@code AbortProjectUseCase} 로 상태 전이만 허용합니다.
 */
public interface DeleteProjectUseCase {

    /**
     * 프로젝트를 영구 삭제합니다.
     *
     * @param command 삭제 Command
     */
    void delete(DeleteProjectCommand command);
}
