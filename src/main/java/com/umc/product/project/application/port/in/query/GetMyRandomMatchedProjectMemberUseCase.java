package com.umc.product.project.application.port.in.query;

import com.umc.product.project.application.port.in.query.dto.ProjectMemberInfo;
import java.util.Optional;

/**
 * 본인의 랜덤 매칭/운영진 강제 배정 프로젝트 멤버 조회 UseCase.
 * <p>
 * 본인 지원 내역 화면(APPLY-004) 합성용으로, {@code application = null} + ACTIVE 상태인 ProjectMember 를 단건 반환한다. 도메인 정책상 한 챌린저는 한 기수에 한
 * 프로젝트에만 합류 가능하므로 0 또는 1 건.
 * <p>
 * 챌린저 파트로부터 매칭 종류(MatchingType) 가 결정되는 비즈니스 룰은 Service 내부에 캡슐화되어 있어 호출자는 (memberId, gisuId) 만 알면 된다. 호출자가 PLAN/ADMIN
 * 챌린저이거나 해당 기수의 챌린저가 아니면 {@link Optional#empty()} 를 반환한다.
 */
public interface GetMyRandomMatchedProjectMemberUseCase {

    Optional<ProjectMemberInfo> findMyRandomMatched(Long memberId, Long gisuId);
}
