package com.umc.product.organization.application.port.in.query.dto;

import java.util.Set;

/**
 * 내 스터디 그룹 목록 조회에서 사용자의 역할(Role)이 어떤 범위의 스터디 그룹을 볼 수 있는지 나타내는 조회 범위(Scope).
 * <p>
 * 사용자가 동시에 여러 역할을 가질 수 있으므로, 서비스 레이어에서 역할별로 Scope를 조립해 리스트로 전달하고
 * Repository에서 각 Scope를 OR로 합쳐 EXISTS 서브쿼리로 필터링한다.
 * <p>
 * sealed interface + record 패턴을 사용하여 Repository의 switch 문에서 모든 Scope를 처리하도록 강제한다.
 * 새로운 역할 기반 Scope가 추가되면 switch 분기에서 컴파일 에러가 나므로 누락을 방지할 수 있다.
 */
public sealed interface StudyGroupViewScope {

    /**
     * 학교 회장단(SCHOOL_PRESIDENT / SCHOOL_VICE_PRESIDENT) 권한 Scope.
     * <p>
     * 해당 학교에 속한 멤버가 한 명이라도 포함된 스터디 그룹을 모두 볼 수 있다.
     *
     * @param schoolMemberIds 해당 학교에 소속된 멤버 ID 집합 (Member 도메인에서 조회한 결과)
     */
    record AsSchoolCore(Set<Long> schoolMemberIds) implements StudyGroupViewScope {}

    /**
     * 파트장(SCHOOL_PART_LEADER) 권한 Scope.
     * <p>
     * 본인이 StudyGroupMentor(파트장)로 등록된 스터디 그룹만 볼 수 있다.
     *
     * @param memberId 조회 요청 주체의 memberId
     */
    record AsPartLeader(Long memberId) implements StudyGroupViewScope {}
}
