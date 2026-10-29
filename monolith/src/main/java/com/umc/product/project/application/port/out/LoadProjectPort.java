package com.umc.product.project.application.port.out;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.umc.product.project.application.port.in.query.dto.SearchProjectQuery;
import com.umc.product.project.domain.Project;
import com.umc.product.project.domain.enums.ProjectStatus;

/**
 * Project 조회 Port (Driven / Port Out).
 * <p>
 * 메서드 prefix 규칙:
 * <ul>
 *   <li>{@code findBy*} — 없어도 정상 ({@link Optional})</li>
 *   <li>{@code getBy*} — 반드시 있어야 하며 없으면 {@code ProjectDomainException} (PROJECT_NOT_FOUND)</li>
 *   <li>{@code existsBy*} — 존재 여부만 반환</li>
 *   <li>{@code search} — 동적 조건 페이지 검색</li>
 * </ul>
 */
public interface LoadProjectPort {

    Optional<Project> findById(Long id);

    /**
     * ID로 Project를 조회합니다. 존재하지 않으면 도메인 예외를 던집니다.
     */
    Project getById(Long id);

    Project getByIdForUpdate(Long id);

    /**
     * 여러 ID 에 대해 Project 를 한 번에 조회합니다. 누락된 ID 는 결과에서 빠집니다 (예외 없음).
     */
    List<Project> listByIds(Collection<Long> ids);

    /**
     * 지부의 공개 프로젝트(IN_PROGRESS·COMPLETED)를 제출 시각·ID 오름차순으로 조회합니다.
     */
    List<Project> listPublicByChapterId(Long chapterId);

    /**
     * 특정 지부의 특정 상태 프로젝트 목록을 반환합니다.
     */
    List<Project> listByChapterIdAndStatus(Long chapterId, ProjectStatus status);

    /**
     * 특정 멤버가 특정 기수에 PO 인 프로젝트가 하나라도 존재하는지 확인합니다. Access scope 판정용 (OwnerOnly 부여 여부) — 동일 기수 내 PO 중복은 더 이상 차단되지 않으므로 다중
     * 매치 가능.
     */
    boolean existsByOwnerAndGisu(Long productOwnerMemberId, Long gisuId);

    /** 생성 시각 내림차순, ID 내림차순으로 가장 최근 초안 한 건을 조회한다. */
    Optional<Project> findLatestDraftByCreatorAndGisu(Long creatorMemberId, Long gisuId);

    Page<Project> searchDraftsByCreatorAndGisu(Long creatorMemberId, Long gisuId, Pageable pageable);

    /**
     * 동적 조건으로 프로젝트 목록을 페이지 조회합니다.
     */
    Page<Project> search(SearchProjectQuery query);
}
