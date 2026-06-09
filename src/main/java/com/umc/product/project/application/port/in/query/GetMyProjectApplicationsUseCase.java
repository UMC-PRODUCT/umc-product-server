package com.umc.product.project.application.port.in.query;

import com.umc.product.project.application.port.in.query.dto.GetMyProjectApplicationsQuery;
import com.umc.product.project.application.port.in.query.dto.ProjectApplicationSummaryInfo;
import java.util.List;

/**
 * 본인의 프로젝트 지원 내역 목록 조회 UseCase.
 * <p>
 * 호출자의 챌린저 파트로부터 {@link com.umc.product.project.domain.enums.MatchingType} 자동 필터링이 적용된다. 정렬: 매칭 라운드 시작일 ASC -> 지원서 갱신일
 * DESC.
 * <p>
 * 응답은 ProjectApplication 자원 한 종류만 포함한다. 화면 카드 합성(랜덤 매칭 멤버 합성 / Project / MatchingRound / PM 정보 enrich) 은 Web Assembler
 * 가 다른 자원 UseCase 와 함께 수행한다.
 */
public interface GetMyProjectApplicationsUseCase {

    List<ProjectApplicationSummaryInfo> listMyApplications(GetMyProjectApplicationsQuery query);
}
