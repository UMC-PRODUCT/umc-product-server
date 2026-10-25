package com.umc.product.project.application.port.in.command;

import com.umc.product.project.application.port.in.command.dto.DeleteProjectCommand;

/**
 * 프로젝트 hard delete UseCase.
 * <p>
 * 각 차수의 지원기간을 제외한 기간에 연관 지원서·답변·팀 소속을 같은 트랜잭션에서 삭제합니다.
 */
public interface DeleteProjectUseCase {

    /**
     * 프로젝트를 영구 삭제합니다.
     *
     * @param command 삭제 Command
     */
    void delete(DeleteProjectCommand command);
}
