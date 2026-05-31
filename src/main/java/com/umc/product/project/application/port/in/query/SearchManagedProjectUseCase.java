package com.umc.product.project.application.port.in.query;

import com.umc.product.project.application.port.in.query.dto.ProjectInfo;
import com.umc.product.project.application.port.in.query.dto.SearchManagedProjectQuery;
import org.springframework.data.domain.Page;

/**
 * 관리 화면용 프로젝트 검색 UseCase (PROJECT-006).
 * <p>
 * 호출자 역할에 따라 자동 scope 적용. 일반 챌린저는 빈 페이지.
 */
public interface SearchManagedProjectUseCase {

    Page<ProjectInfo> searchManaged(SearchManagedProjectQuery query, Long memberId);
}
