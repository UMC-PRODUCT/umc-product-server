package com.umc.product.project.application.port.in.query;

import com.umc.product.project.application.port.in.query.dto.ProjectInfo;
import java.util.Optional;

/**
 * 단건 프로젝트 조회 UseCase.
 * <ul>
 *   <li>{@link #getById(Long)} — 프로젝트 상세 조회 (PROJECT-002). 반드시 존재해야 하며, 없으면 예외.</li>
 *   <li>{@link #findDraftByOwnerAndGisu(Long, Long)} — PM의 내 Draft 조회 (PROJECT-103). 없으면 {@link Optional#empty()}.</li>
 * </ul>
 * <p>
 * 목록/검색 조회는 {@link SearchProjectUseCase}를 참고하세요.
 */
public interface GetProjectUseCase {

    /**
     * 프로젝트 ID로 단건 조회합니다. 해당 ID의 프로젝트가 없으면 도메인 예외를 던집니다.
     *
     * @param projectId 프로젝트 ID
     * @return 프로젝트 Info
     */
    ProjectInfo getById(Long projectId);

    /**
     * 특정 PM이 특정 기수에 작성한 Draft 프로젝트를 조회합니다.
     * PM이 아직 만들지 않았거나 상태가 DRAFT가 아닌 경우 {@link Optional#empty()}.
     *
     * @param productOwnerMemberId PO(작성자) Member ID
     * @param gisuId               기수 ID
     * @return Draft Info, 없으면 empty
     */
    Optional<ProjectInfo> findDraftByOwnerAndGisu(Long productOwnerMemberId, Long gisuId);
}
