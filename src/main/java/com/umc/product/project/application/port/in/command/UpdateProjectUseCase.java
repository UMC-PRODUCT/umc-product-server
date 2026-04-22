package com.umc.product.project.application.port.in.command;

import com.umc.product.project.application.port.in.command.dto.UpdateProjectCommand;

/**
 * 프로젝트 기본 정보 수정 UseCase (PROJECT-102).
 * <p>
 * {@code DRAFT} 상태에서는 전체 필드 수정 가능하며,
 * 공개 이후 상태에서의 제한 정책은 구현 단에서 별도 확인합니다.
 */
public interface UpdateProjectUseCase {

    /**
     * 프로젝트 기본 정보를 부분 업데이트합니다.
     *
     * @param command 수정 Command ({@code null} 필드는 수정 제외)
     */
    void update(UpdateProjectCommand command);
}
