package com.umc.product.project.application.port.in.command;

import com.umc.product.project.application.port.in.command.dto.CreateDraftProjectCommand;

/**
 * 프로젝트 Draft 생성 UseCase (PROJECT-101).
 * <p>
 * 작성자와 PM의 기존 초안 개수에 제한 없이 새 DRAFT를 생성합니다.
 * 생성 권한과 PM의 기수·파트·지부 검증은 유지합니다.
 */
public interface CreateDraftProjectUseCase {

    /**
     * 빈 DRAFT 프로젝트를 생성합니다.
     *
     * @return 생성된 프로젝트 ID
     */
    Long create(CreateDraftProjectCommand command);
}
