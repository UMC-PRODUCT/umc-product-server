package com.umc.product.project.application.port.out;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;

import com.umc.product.project.application.port.in.query.dto.SearchProjectQuery;
import com.umc.product.project.domain.Project;

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

    /**
     * 여러 ID 에 대해 Project 를 한 번에 조회합니다. 누락된 ID 는 결과에서 빠집니다 (예외 없음).
     */
    List<Project> listByIds(Collection<Long> ids);

    /**
     * 특정 멤버가 특정 기수에 PO 인 프로젝트가 하나라도 존재하는지 확인합니다. Access scope 판정용 (OwnerOnly 부여 여부) — 동일 기수 내 PO 중복은 더 이상 차단되지 않으므로 다중
     * 매치 가능.
     */
    boolean existsByOwnerAndGisu(Long productOwnerMemberId, Long gisuId);

    /**
     * 특정 creator 가 특정 기수에 작성 중인 DRAFT 프로젝트를 조회합니다. (creator, gisu) 당 DRAFT 1 개 UNIQUE 제약이라 단건이 보장됩니다.
     */
    Optional<Project> findDraftByCreatorAndGisu(Long creatorMemberId, Long gisuId);

    /**
     * 특정 creator 가 특정 기수에 작성 중인 DRAFT 를 보유하고 있는지 확인합니다. Draft 동시 생성 방지(PROJECT-101)용 빠른 체크.
     */
    boolean existsDraftByCreatorAndGisu(Long creatorMemberId, Long gisuId);

    /**
     * 특정 PO 가 특정 기수에 활성 DRAFT 프로젝트를 보유하고 있는지 확인합니다. 운영진이 다른 챌린저를 PO 로 임명하는 경로에서 PO 의 DRAFT 중복을 차단하기 위한 체크. (owner,
     * gisu) DRAFT 1 개 partial unique index 와 짝.
     */
    boolean existsDraftByOwnerAndGisu(Long productOwnerMemberId, Long gisuId);

    /**
     * 동적 조건으로 프로젝트 목록을 페이지 조회합니다.
     */
    Page<Project> search(SearchProjectQuery query);
}
