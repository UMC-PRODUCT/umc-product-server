package com.umc.product.organization.application.port.in.query.dto;

import java.util.Set;

/**
 * 조직 도메인(Organization)의 *역할 기반 조회 권한 범위*.
 * <p>
 * 사용자가 동시에 여러 역할을 가질 수 있으므로, Service 가 역할별로 Scope 를 조립해 리스트로 전달한다. Consumer 측 Repository 가 각 Scope 를 OR 로 합쳐 적용한다
 * (예: EXISTS 서브쿼리). 다른 도메인(StudyGroup, StudyGroupSchedule 등) 이 공통으로 사용한다.
 * <p>
 * sealed interface + record 패턴: 새 역할 기반 Scope 가 추가되면 switch 분기에서 컴파일 에러가 나서 누락을 방지한다.
 */
public sealed interface OrganizationRoleScope {

    /**
     * 학교 회장단(SCHOOL_PRESIDENT / SCHOOL_VICE_PRESIDENT) 권한 Scope.
     * <p>
     * 해당 학교에 속한 멤버가 *멤버 또는 멘토* 로 한 명이라도 포함된 대상을 모두 볼 수 있다.
     *
     * @param schoolMemberIds 해당 학교에 소속된 멤버 ID 집합 (Member 도메인에서 조회한 결과)
     */
    record AsSchoolCore(Set<Long> schoolMemberIds) implements OrganizationRoleScope {
    }

    /**
     * 파트장(SCHOOL_PART_LEADER) 권한 Scope.
     * <p>
     * 본인이 mentor 로 등록된 대상만 볼 수 있다.
     *
     * @param memberId 조회 요청 주체의 memberId
     */
    record AsPartLeader(Long memberId) implements OrganizationRoleScope {
    }
}
