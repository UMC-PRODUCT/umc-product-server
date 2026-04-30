package com.umc.product.project.application.port.in.command;

import com.umc.product.project.application.port.in.command.dto.CreateDraftProjectCommand;

/**
 * 프로젝트 Draft 생성 UseCase (PROJECT-101).
 * <p>
 * 한 PM이 한 기수에 가질 수 있는 프로젝트는 1개. 이미 존재하면 {@code PROJECT_DUPLICATE_IN_GISU}.
 * 프론트는 {@code GET /projects/me/draft?gisuId=X}로 사전 확인 후 호출하는 것을 권장.
 */
public interface CreateDraftProjectUseCase {

    /**
     * 빈 DRAFT 프로젝트를 생성합니다.
     *
     * @return 생성된 프로젝트 ID
     */
    Long create(CreateDraftProjectCommand command);
}
