package com.umc.product.project.application.port.out;

import com.umc.product.project.application.port.in.query.dto.SearchProjectQuery;
import com.umc.product.project.domain.Project;
import java.util.Optional;
import org.springframework.data.domain.Page;

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
     * 특정 멤버가 특정 기수에 PO 인 프로젝트가 하나라도 존재하는지 확인합니다.
     * Access scope 판정용 (OwnerOnly 부여 여부) — 동일 기수 내 PO 중복은 더 이상 차단되지 않으므로 다중 매치 가능.
     */
    boolean existsByOwnerAndGisu(Long productOwnerMemberId, Long gisuId);

    /**
     * 특정 creator 가 특정 기수에 작성 중인 DRAFT 프로젝트를 조회합니다.
     * (creator, gisu) 당 DRAFT 1 개 UNIQUE 제약이라 단건이 보장됩니다.
     */
    Optional<Project> findDraftByCreatorAndGisu(Long creatorMemberId, Long gisuId);

    /**
     * 특정 creator 가 특정 기수에 작성 중인 DRAFT 를 보유하고 있는지 확인합니다.
     * Draft 동시 생성 방지(PROJECT-101)용 빠른 체크.
     */
    boolean existsDraftByCreatorAndGisu(Long creatorMemberId, Long gisuId);

    /**
     * 동적 조건으로 프로젝트 목록을 페이지 조회합니다.
     */
    Page<Project> search(SearchProjectQuery query);
}
