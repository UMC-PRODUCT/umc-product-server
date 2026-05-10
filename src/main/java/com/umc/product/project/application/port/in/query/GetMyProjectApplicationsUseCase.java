package com.umc.product.project.application.port.in.query;

import com.umc.product.project.application.port.in.query.dto.GetMyProjectApplicationsQuery;
import com.umc.product.project.application.port.in.query.dto.MyProjectApplicationCardInfo;
import java.util.List;

/**
 * 본인의 프로젝트 지원 내역 목록 조회 UseCase.
 * <p>
 * 사용자의 파트({@link com.umc.product.common.domain.enums.ChallengerPart}) 에 따라
 * {@link com.umc.product.project.domain.enums.MatchingType} 자동 필터링을 적용한다.
 * <p>
 * 정렬: 매칭 라운드 시작일 ASC -> 지원서 갱신일 DESC.
 * <p>
 * 응답에 포함되는 카드는 항상 ProjectApplication 1건에 대응한다 (랜덤 매칭은 본 API 범위 외).
 */
public interface GetMyProjectApplicationsUseCase {

    List<MyProjectApplicationCardInfo> getMyApplications(GetMyProjectApplicationsQuery query);
}
