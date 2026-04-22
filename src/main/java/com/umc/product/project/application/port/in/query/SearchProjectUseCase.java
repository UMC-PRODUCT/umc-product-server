package com.umc.product.project.application.port.in.query;

import com.umc.product.project.application.port.in.query.dto.ProjectInfo;
import com.umc.product.project.application.port.in.query.dto.SearchProjectQuery;
import org.springframework.data.domain.Page;

/**
 * 프로젝트 목록 검색 UseCase (PROJECT-001).
 * <p>
 * 동적 필터와 페이지네이션을 지원합니다. 상태 필터는 {@link SearchProjectQuery}의
 * {@code forChallenger}/{@code forAdmin} 팩토리에서 권한에 따라 자동 조립됩니다.
 */
public interface SearchProjectUseCase {

    /**
     * 조건에 맞는 프로젝트 목록을 페이징하여 반환합니다.
     *
     * @param query 검색 조건 ({@code Pageable} 포함)
     * @return ProjectInfo 페이지
     */
    Page<ProjectInfo> search(SearchProjectQuery query);
}
