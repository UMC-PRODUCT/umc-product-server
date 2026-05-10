package com.umc.product.project.application.port.in.command;

import com.umc.product.project.application.port.in.command.dto.CreateDraftProjectCommand;

/**
 * 프로젝트 Draft 생성 UseCase (PROJECT-101).
 * <p>
 * (creator, gisu) 당 작성 중인 DRAFT 는 1 개로 제한된다. 이미 작성 중인 DRAFT 가 있으면
 * {@code PROJECT_DRAFT_ALREADY_IN_PROGRESS}. DRAFT 가 PENDING_REVIEW 로 전이되면 슬롯이 풀려
 * 같은 creator 가 새 DRAFT 를 시작할 수 있다. PO 측 유일성 제약은 적용하지 않는다.
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
