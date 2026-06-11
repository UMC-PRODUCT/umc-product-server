package com.umc.product.project.application.port.in.query;

import com.umc.product.project.application.port.in.query.dto.ProjectInfo;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;

/**
 * 단건 프로젝트 조회 UseCase.
 * <ul>
 *   <li>{@link #getById(Long)} — 프로젝트 상세 조회 (PROJECT-002). 반드시 존재해야 하며, 없으면 예외.</li>
 *   <li>{@link #findDraftByCreatorAndGisu(Long, Long)} — 내가 작성 중인 Draft 조회 (PROJECT-103). 없으면 {@link Optional#empty()}.</li>
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
     * 여러 ID 의 프로젝트를 한 번에 Info 로 조회합니다.
     * <p>
     * 각 ProjectInfo 는 thumbnail/logo URL, coPM, partQuota 등 표준 view 의 부가 정보까지 모두 합성된 상태로 반환됩니다. Assembler 가
     * cross-domain 합성을 수행할 때 N+1 을 피하기 위한 batch 진입점입니다.
     *
     * @param projectIds 프로젝트 ID 집합
     * @return projectId -> ProjectInfo 매핑. 누락된 ID 는 결과에서 빠집니다.
     */
    Map<Long, ProjectInfo> findAllByIds(Collection<Long> projectIds);

    /**
     * 특정 creator 가 특정 기수에 작성 중인 Draft 프로젝트를 조회합니다. 등록 화면 재진입용 — creator 본인이 만든 DRAFT 만 노출되며, (creator, gisu) 당 DRAFT 1
     * 개 제약이라 단건이 보장됩니다.
     *
     * @param creatorMemberId 작성자(creator) Member ID
     * @param gisuId          기수 ID
     * @return Draft Info, 없으면 empty
     */
    Optional<ProjectInfo> findDraftByCreatorAndGisu(Long creatorMemberId, Long gisuId);
}
