package com.umc.product.project.application.port.in.query;

import com.umc.product.project.application.port.in.query.dto.ProjectApplicationCardInfo;
import com.umc.product.project.application.port.in.query.dto.SearchProjectApplicationsQuery;
import java.util.List;

/**
 * PM/운영진용 단일 프로젝트 지원자 목록 조회 UseCase.
 * <p>
 * 임시저장(DRAFT)을 제외한 SUBMITTED/APPROVED/REJECTED 지원서만 노출한다. 매칭 차수 / 파트 / 상태는 Query 의 옵셔널 필드로 받아 동적 필터를 적용한다.
 * <p>
 * 정렬: 매칭 차수(phase) ASC -> 지원시각(submittedAt) ASC. 같은 파트로 묶기는 클라이언트가 처리한다.
 */
public interface SearchProjectApplicationsUseCase {

    List<ProjectApplicationCardInfo> searchByProject(SearchProjectApplicationsQuery query);
}
